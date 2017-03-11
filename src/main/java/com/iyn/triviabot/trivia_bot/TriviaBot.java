package com.iyn.triviabot.trivia_bot;

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.DiscordException;

public class TriviaBot implements IModule{
	
	private String moduleName = "TriviaBot";
	private String moduleVersion = "1.0";
	private String moduleMinimumVersion = "2.3.0";
	private String author = "IYN";
	
	public static TriviaBot INSTANCE; // Singleton instance of the bot.
	public static IDiscordClient client; // The instance of the discord client.
	
	public static TriviaGame game;
	
	public TriviaBot(IDiscordClient client) {
		this.client = client;
	}
	
	public static void main(String[] args) { // Main method
		Scanner s;
		try {
			s = new Scanner(new File("token.txt"));
			INSTANCE = login(s.nextLine()); // Creates the bot instance and logs it in.
			INSTANCE.enable(client);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static TriviaBot login(String token) {
		TriviaBot bot = null; // Initializing the bot variable

		ClientBuilder builder = new ClientBuilder(); // Creates a new client builder instance
		builder.withToken(token); // Sets the bot token for the client
		try {
			IDiscordClient client = builder.login(); // Builds the IDiscordClient instance and logs it in
			bot = new TriviaBot(client); // Creating the bot instance
		} catch (DiscordException e) { // Error occurred logging in
			System.err.println("Error occurred while logging in!");
			e.printStackTrace();
		}

		return bot;
	}
	
	public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			if (login) {
				return clientBuilder.login(); // Creates the client instance and logs the client in
			} else {
				return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
			}
		} catch(DiscordException e) { // This is thrown if there was a problem building the client
			e.printStackTrace();
		}
		return null;
	}
    
    public boolean enable(IDiscordClient dclient) {
    	client = dclient;
		EventDispatcher dispatcher = client.getDispatcher();
		dispatcher.registerListener(new MessageListener());
		game = new TriviaGame();
		dispatcher.registerListener(game);
		return true;
    }
    
    public void disable() {
    	
    }
    
    public String getAuthor() {
        return author;
    }
    public String getMinimumDiscord4JVersion() {
        return moduleMinimumVersion;
    }
    public String getName() {
        return moduleName;
    }
    public String getVersion() {
        return moduleVersion;
    }
    
}
