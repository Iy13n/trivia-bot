package com.discordtriviabot.triviabot.trivia_bot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.io.FileNotFoundException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.api.IDiscordClient;

public class TriviaUser {
	
	private String discordID;
	private String currentName;
	private int experience;
	private int numOfWins;
	
	public static void main(String[] args) {
		TriviaUser x = new TriviaUser("testUser", "username");
		System.out.println(x.toString());
		x.addWin();
		x.addExp(15);
		System.out.println(x.toString());
	}
	
	public TriviaUser(String discordID, String name) {
		this.discordID = discordID;
		this.currentName = name;
		this.loadStats();
	}
	
	private void loadStats() {
		Properties p = new Properties();
		InputStream input = null;
		
		try {

			input = new FileInputStream("playerData/" + this.discordID + ".properties");

			// load a properties file
			p.load(input);

			// get the property value and save it
			this.currentName = p.getProperty("name");
			this.experience = Integer.parseInt(p.getProperty("exp"));
			this.numOfWins = Integer.parseInt(p.getProperty("wins"));

			
		} catch (IOException e) {
			
			p.setProperty("name", this.currentName);
			p.setProperty("exp", "0");
			p.setProperty("wins", "0");
			
			OutputStream out = null;
			try {
				out = new FileOutputStream("playerData/" + this.discordID + ".properties");
				p.store(out, null);
			} catch (IOException io) {
				io.printStackTrace();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			
			this.experience = 0;
			this.numOfWins = 0;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void addWin() {
		this.numOfWins += 1;
		this.updateFile();
	}
	
	public void addExp(int exp) {
		this.experience += exp;
		this.updateFile();
	}
	
	public void updateFile() {
		Properties p = new Properties();
		OutputStream out = null;

		p.setProperty("name", this.currentName);
		p.setProperty("exp", Integer.toString(this.experience));
		p.setProperty("wins", Integer.toString(this.numOfWins));

		try {
			out = new FileOutputStream("playerData/" + this.discordID + ".properties");
			p.store(out, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public String getName() {
		return this.currentName;
	}
	
	public int getExp() {
		return this.experience;
	}
	
	public int getWins() {
		return this.numOfWins;
	}
	
	public String toString() {
		return getName() + " has " + getExp() + " experience points and has " + getWins() + " wins.";
	}
}
