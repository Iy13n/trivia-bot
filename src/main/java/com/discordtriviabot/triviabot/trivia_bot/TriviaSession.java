package com.discordtriviabot.triviabot.trivia_bot;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import sx.blah.discord.handle.obj.IUser;

public class TriviaSession {
	
	private HashMap<IUser, Integer> players;
	private IUser firstCorrect;
	private HashMap<IUser, Character> currentAnswers;
	private int numOfQuestionsEmpty = 0;
	private ArrayList<TriviaQuestion> questions;
	private TriviaQuestion currentQuestion;
	
	private final String QUESTIONS_FILE_NAME;
	
	public TriviaSession(String questionFile) {
		this.players = new HashMap<IUser, Integer>();
		this.QUESTIONS_FILE_NAME = questionFile;
		this.currentAnswers = new HashMap<IUser, Character>();
		this.firstCorrect = null;
		loadQuestions();
	}
	
	private void loadQuestions() {
		this.questions = new ArrayList<TriviaQuestion>();
		
        try
        {
            File qFile = new File( QUESTIONS_FILE_NAME );
            Scanner in = new Scanner( qFile );
            in.useDelimiter( "[,\r\n]"  );
            
            while( in.hasNext())
            {
            	this.questions.add(new TriviaQuestion(in.next(), in.next(), in.next(), in.next(), in.next()));
            	if (in.hasNext())
                	in.nextLine();
            }
            
            in.close();
        }
        catch( FileNotFoundException e )
        {
            System.out.println( "Question file: " + QUESTIONS_FILE_NAME + " not found." );
        }
	}
	
	public TriviaQuestion getNewQuestion() {
		int index = (int) (Math.random() * questions.size());
		this.currentQuestion = questions.get(index);
		return currentQuestion;
	}
	
	public TriviaQuestion getCurrentQuestion() {
		return currentQuestion;
	}
	
	public int getEmptyQuestions() {
		return numOfQuestionsEmpty;
	}
	
	public void addNewAnswer(IUser player, char answer) {
		this.numOfQuestionsEmpty = 0;
		this.currentAnswers.put(player, answer);
		if (firstCorrect == null && currentQuestion.isCorrect(answer)) {
			this.firstCorrect = player;
		}
	}
	
	public IUser getFirstAnswerer() {
		return this.firstCorrect;
	}
	
	public HashMap<IUser, Character> getCurrentAnswers() {
		return this.currentAnswers;
	}
	
	public void clearCurrentAnswers() {
		if (currentAnswers.isEmpty())
			this.numOfQuestionsEmpty++;
		this.currentAnswers = new HashMap<IUser, Character>();
		this.firstCorrect = null;
	}

	public HashMap<IUser, Integer> getAllPlayers() {
		return this.players;
	}
	
	public boolean findPlayerInAnswers(IUser player) {
		for (IUser tempPlayer : currentAnswers.keySet()) {
			if (player.equals(tempPlayer)) {
				return true;
			}
		}
		return false;
	}
	
	public void incrementScore(IUser player) {
		try {
			this.players.put(player, this.players.get(player) + 1);
		} catch (NullPointerException e) {
			this.players.put(player, 1);
		}
	}
	
	public Pair<IUser, Integer> getMaxScoringPlayer() {
		Pair<IUser, Integer> ret = new Pair<IUser, Integer>(null, 0);
		for (HashMap.Entry<IUser, Integer> entry : players.entrySet()) {
			if (ret.getValue() < entry.getValue()) {
				ret = new Pair<IUser, Integer>(entry.getKey(), entry.getValue());
			}
		}
		return ret;
	}
}
