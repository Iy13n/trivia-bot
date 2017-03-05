package com.discordtriviabot.triviabot.trivia_bot;

import java.util.Map;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class TriviaGame {
	
	private MessageReceivedEvent triviaTrigger;
	private TriviaSession currentSession;
	private int currentSessionMaxScore;
	private boolean inQuestion = false;
	
	public TriviaGame() {
	}
	
	public void run() throws DiscordException, MissingPermissionsException, RateLimitException, InterruptedException {
		do {
			
			currentSession.getNewQuestion();
			sendMessage(currentSession.getCurrentQuestion().toString(), triviaTrigger);
			
			inQuestion = true;
			
			Thread.sleep(15000);
			
			inQuestion = false;
			
			String timesUpMessage = ":alarm_clock: **Time's up! The correct answer was " + currentSession.getCurrentQuestion().getCorrectAnsLet() + "!**\n";

			String correctPlayers = "";
			
			if (!currentSession.getCurrentAnswers().isEmpty()) {
				for (Map.Entry<IUser, Character> player : currentSession.getCurrentAnswers().entrySet()) {
					if (currentSession.getCurrentQuestion().isCorrect(player.getValue())) {
						correctPlayers += player.getKey() + ", ";
						currentSession.incrementScore(player.getKey());
					}
				}
				if (!correctPlayers.equals("")) {
					timesUpMessage += correctPlayers.substring(0, correctPlayers.length() - 2) + " got the question correct and won 1 point!\n";
					currentSession.incrementScore(currentSession.getFirstAnswerer());
					sendMessage(timesUpMessage + currentSession.getFirstAnswerer() + " answered first and got an extra point!", triviaTrigger);
				}
			} else {
				sendMessage(timesUpMessage + "Nobody answered this round. What a shame.", triviaTrigger);
			}
			
			currentSession.clearCurrentAnswers();
			
			Thread.sleep(3000);
			
		} while (currentSession != null && currentSession.getMaxScoringPlayer().getValue() < currentSessionMaxScore && currentSession.getEmptyQuestions() < 5);
		
		String endGameMessage = "";
		
		if (currentSession.getMaxScoringPlayer().getValue() >= currentSessionMaxScore) {
			endGameMessage = "**__Game has ended!__ " + currentSession.getMaxScoringPlayer().getKey() + " won!**\n";
		} else if (currentSession.getEmptyQuestions() >= 5) {
			endGameMessage = "The game was automatically terminated after 5 nonanswered questions.\n";
		}
		
		for (Map.Entry<IUser, Integer> player : currentSession.getAllPlayers().entrySet()) {
			endGameMessage += player.getKey() + ": **" + player.getValue() + "** points\n";
		}
		
		sendMessage(endGameMessage, triviaTrigger);
		endCurrentSession();
	}
	
	@EventSubscriber
	public void OnMessageEvent(MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException{
		IMessage message = event.getMessage();
		if (inQuestion && message.getChannel().equals(triviaTrigger.getMessage().getChannel())) {
			if (message.getContent().length() == 1 && TriviaQuestion.ansLetToNum(message.getContent().charAt(0)) != -1) {
				if (!currentSession.findPlayerInAnswers(message.getAuthor())) {
					currentSession.addNewAnswer(message.getAuthor(), message.getContent().charAt(0));
					sendMessage(message.getAuthor() + " has locked in answer " + message.getContent().toUpperCase().charAt(0) + "!", event);
				} else {
					sendMessage("You may only answer once, " + message.getAuthor() + "!", event);
				}
			}
		}
		if (currentSession != null && message.getContent().startsWith("tb_trivia end") && message.getChannel().equals(triviaTrigger.getMessage().getChannel())) {
			if (message.getAuthor().equals(triviaTrigger.getMessage().getAuthor())) {
				sendMessage("Trivia will end after this question.", event);
				endCurrentSession();
			} else {
				sendMessage("Only the trivia starter can end trivias! The game will also end after 5 unanswered questions.", event);
			}
		}
	}
	
	public void sendMessage(String message, MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException{
		new MessageBuilder(TriviaBot.client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
	}
	
	public void createNewSession(MessageReceivedEvent trigger, int maxScore, String questionGroup) {
		this.currentSession = new TriviaSession(questionGroup + "Questions.csv");
		this.triviaTrigger = trigger;
		this.currentSessionMaxScore = maxScore;
	}
	
	public void endCurrentSession() {
		this.currentSession = null;
		this.triviaTrigger = null;
		this.currentSessionMaxScore = 0;
	}
}
