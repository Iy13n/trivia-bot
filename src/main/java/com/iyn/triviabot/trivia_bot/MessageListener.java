package com.iyn.triviabot.trivia_bot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

public class MessageListener {
	
	@EventSubscriber
	public void OnMessageEvent(MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException, InterruptedException {
		IMessage message = event.getMessage();
		if (message.getContent().startsWith("tb_help")){
			sendMessage("```tb_greet       send a greeting\n"
						 + "tb_trivia      opens the trivia menu\n"
						 + "tb_help        display this menu```", event);
		}
		if (message.getContent().startsWith("tb_greet")) {
			sendMessage("Greetings, " + message.getAuthor() + "!", event);
		}
		if (message.getContent().startsWith("tb_trivia")) {
			if (message.getContent().equals("tb_trivia") || message.getContent().equals("tb_trivia ")) {
				sendMessage("```tb_trivia league #   starts a LoL-themed trivia with # points to win\n"
						 	 + "tb_trivia            open this menu```", event);
			} else if (message.getContent().startsWith("league", 10)) {
				try {
					int numOfQ = Integer.parseInt(message.getContent().substring(17));
					if (3 <= numOfQ && numOfQ <= 50) {
						TriviaBot.game.createNewSession(event, numOfQ, "league");
						TriviaBot.game.run();
					} else {
						sendMessage("Points required to win must be between 3 and 50!", event);
					}
				} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
					sendMessage("Use tb_trivia league [number] to start a League-themed trivia game!", event);
				}
			}
		}
	}
	
	public void sendMessage(String message, MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException{
		new MessageBuilder(TriviaBot.client).appendContent(message).withChannel(event.getMessage().getChannel()).build();
	}
	
}
