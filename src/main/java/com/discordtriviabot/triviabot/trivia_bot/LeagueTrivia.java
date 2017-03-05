package com.discordtriviabot.triviabot.trivia_bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.io.File;
import java.io.FileNotFoundException;

import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

public class LeagueTrivia {
	
	private int scoreToWin;
	private boolean inQuestion = false;
	private MessageReceivedEvent triviaTrigger;
	private ArrayList<Pair<IUser, Integer>> players = new ArrayList<Pair<IUser, Integer>>();
	private ArrayList<Pair<IUser, String>> playerAnswers = new ArrayList<Pair<IUser, String>>();
	private ArrayList<String[]> questions;
	private Timer timer;
	
	private static String QUESTIONS_FILE_NAME = "leagueQuestions.csv";
	
	public LeagueTrivia() {
		
	}
	
	public LeagueTrivia(MessageReceivedEvent event, int score) {
		triviaTrigger = event;
		scoreToWin = score;
		this.loadQuestions();
		
		players.add(new Pair<IUser, Integer>(triviaTrigger.getMessage().getAuthor(), 0));
		
		IDiscordClient client = TriviaBot.client;
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(this);
	}
	
	public void run() throws DiscordException, MissingPermissionsException, RateLimitException, InterruptedException {
		int emptyQuestions = 0;
		sendMessage("A trivia round has started! Gain " + scoreToWin + " points to win!", triviaTrigger);
		
		do {
			inQuestion = true;
			int questionNum = (int)(Math.random() * questions.size());
			playerAnswers = new ArrayList<Pair<IUser, String>>();
			
			String[] question = questions.get(questionNum);
			String[] answers = new String[] {question[1], question[2], question[3], question[4]};
			Collections.shuffle(Arrays.asList(answers));
			String correctAns = LeagueTrivia.convertIndextoAnswer(Arrays.asList(answers).indexOf(question[1]));
			
			sendMessage(":question: __**Question**__:\n"
					+ question[0] + "\n"
					+ ":regional_indicator_a: " + answers[0] + "\n"
					+ ":regional_indicator_b: " + answers[1] + "\n"
					+ ":regional_indicator_c: " + answers[2] + "\n"
					+ ":regional_indicator_d: " + answers[3] + "\n"
					+ "You have 15 seconds. Reply with a letter, you may only answer once!", triviaTrigger);
			
			CountDownLatch startSignal = new CountDownLatch(1);
			CountDownLatch doneSignal = new CountDownLatch(1);
			
			new Thread(new Wait(startSignal, doneSignal, 15)).start();
			
			startSignal.countDown();
			doneSignal.await();
			
			inQuestion = false;
			boolean first = true;
			
			sendMessage("Time's up! The correct answer was " + correctAns + "!", triviaTrigger);
			if (playerAnswers.size() > 0) {
				emptyQuestions = 0;
				for (Pair<IUser, String> answer : playerAnswers) {
					if (Arrays.asList(question).indexOf(answers[LeagueTrivia.convertAnswertoIndex(answer.getValue())]) == 1) {
						if (first) {
							if (!findPlayerInScores(answer.getKey())) {
								players.add(new Pair<IUser, Integer>(answer.getKey(), 2));
							} else {
								for (int i = 0; i < players.size(); i++) {
									if (players.get(i).getKey().equals(answer.getKey())) {
										Pair<IUser, Integer> player = players.get(i);
										int score = player.getValue() + 2;
										Pair<IUser, Integer> temp = new Pair<IUser, Integer>(player.getKey(), score);
										players.set(i, temp);
										break;
									}
								}
							}
							first = false;
							sendMessage(answer.getKey() + " answered first and got 2 points!", triviaTrigger);
						} else {
							if (!findPlayerInScores(answer.getKey())) {
								players.add(new Pair<IUser, Integer>(answer.getKey(), 1));
							} else {
								for (int i = 0; i < players.size(); i++) {
									if (players.get(i).getKey().equals(answer.getKey())) {
										Pair<IUser, Integer> player = players.get(i);
										int score = player.getValue() + 1;
										Pair<IUser, Integer> temp = new Pair<IUser, Integer>(player.getKey(), score);
										players.set(i, temp);
										break;
									}
								}
							}
							sendMessage(answer.getKey() + " answered correctly and got 1 point!", triviaTrigger);
						}
					}
				}
			} else {
				emptyQuestions++;
				sendMessage("Nobody answered this round. What a shame.", triviaTrigger);
			}
		} while (findHighestScore().getValue() < scoreToWin || emptyQuestions > 5);
	}
	
	class Wait implements Runnable {
		
		private final CountDownLatch startSignal;
		private final CountDownLatch doneSignal;
		private int secs;
		
		public Wait(CountDownLatch startSignal, CountDownLatch doneSignal, int secs) {
			this.startSignal = startSignal;
			this.doneSignal = doneSignal;
			this.secs = secs;
		}
		
		public void run() {
			try {
			       startSignal.await();
			       timer = new Timer();
			       timer.schedule(new SendSignal(this.doneSignal), secs * 1000);
			} catch (InterruptedException ex) {}
		}
	}
	
	class SendSignal extends TimerTask {
		
		private final CountDownLatch doneSignal;
		
		public SendSignal(CountDownLatch doneSignal) {
			this.doneSignal = doneSignal;
		}
		
		public void run() {
			doneSignal.countDown();
			timer.cancel();
		}
		
	}
	
	@EventSubscriber
	public void OnMessageEvent(MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException{
		IMessage message = event.getMessage();
		if (inQuestion) {
			if (LeagueTrivia.convertAnswertoIndex(message.getContent()) != -1) {
				if (!findPlayerInAnswers(message.getAuthor())) {
					playerAnswers.add(new Pair<IUser, String>(message.getAuthor(), message.getContent()));
					sendMessage(message.getAuthor() + " has locked in answer " + message.getContent().toUpperCase() + "!", event);
				} else {
					sendMessage("You may only answer once, " + message.getAuthor() + "!", event);
				}
			}
		}
	}

	public void sendMessage(String message, MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException{
		new MessageBuilder(TriviaBot.client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
	}
	
	private Pair<IUser, Integer> findHighestScore() {
		Pair<IUser, Integer> max = players.get(0);
		for (Pair<IUser, Integer>  pair : players) {
			if (pair.getValue() > max.getValue()) {
				max = pair;
			}
		}
		return max;
	}
	
	private boolean findPlayerInAnswers(IUser user) {
		for (Pair<IUser, String> player : playerAnswers) {
			if (player.getKey().equals(user)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean findPlayerInScores(IUser user) {
		for (Pair<IUser, Integer> player : players) {
			if (player.getKey().equals(user)) {
				return true;
			}
		}
		return false;
	}
	
	private static int convertAnswertoIndex(String ans) {
		if (ans.equals("a") || ans.equals("A"))
			return 0;
		if (ans.equals("b") || ans.equals("B"))
			return 1;
		if (ans.equals("c") || ans.equals("C"))
			return 2;
		if (ans.equals("d") || ans.equals("D"))
			return 3;
		return -1;
	}
	
	private static String convertIndextoAnswer(int ind) {
		if (ind == 0)
			return "A";
		if (ind == 1)
			return "B";
		if (ind == 2)
			return "C";
		if (ind == 3)
			return "D";
		return "*";
	}
	
	private void loadQuestions() {
		this.questions = new ArrayList<String[]>();
		
        try
        {
            File qFile = new File( QUESTIONS_FILE_NAME );
            Scanner in = new Scanner( qFile );
            in.useDelimiter( "[,\r\n]"  );
            
            while( in.hasNext())
            {
            	String[] temp = new String[5];
            	
                temp[0] = in.next();
                temp[1] = in.next();
                temp[2] = in.next();
                temp[3] = in.next();
                temp[4] = in.next();
                in.nextLine();
                this.questions.add(temp);
               
            }
            
            in.close();
        }
        catch( FileNotFoundException e )
        {
            System.out.println( "Question file: " + QUESTIONS_FILE_NAME + " not found." );
        }
    }
}
