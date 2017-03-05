package com.discordtriviabot.triviabot.trivia_bot;

import java.util.Arrays;
import java.util.Collections;

public class TriviaQuestion {
	
	private String question;
	private String cAns;
	private String wAns1;
	private String wAns2;
	private String wAns3;
	private String[] answerOrder;
	
	public TriviaQuestion(String q, String cA, String wA1, String wA2, String wA3) {
		this.question = q;
		this.cAns = cA;
		this.wAns1 = wA1;
		this.wAns2 = wA2;
		this.wAns3 = wA3;
		this.answerOrder = new String[] {cAns, wAns1, wAns2, wAns3};
		Collections.shuffle(Arrays.asList(this.answerOrder));
	}
	
	public String toString() {
		return ":question: __**Question**__:\n"
				+ this.question + "\n"
				+ ":regional_indicator_a: " + answerOrder[0] + "\n"
				+ ":regional_indicator_b: " + answerOrder[1] + "\n"
				+ ":regional_indicator_c: " + answerOrder[2] + "\n"
				+ ":regional_indicator_d: " + answerOrder[3] + "\n"
				+ "You have 15 seconds. Reply with a letter, you may only answer once!";
	}
	
	public String[] getAnswers() {
		return this.answerOrder;
	}
	
	public char getCorrectAnsLet() {
		return TriviaQuestion.numToAnsLet(Arrays.asList(answerOrder).indexOf(cAns));
	}
	
	public boolean isCorrect(String answer) {
		if (answer.equals(cAns)) {
			return true;
		} return false;
	}
	
	public boolean isCorrect(char ansLet) {
		if (answerOrder[TriviaQuestion.ansLetToNum(ansLet)].equals(cAns)) {
			return true;
		} return false;
	}
	
	public static int ansLetToNum(char ansLet) {
		if (ansLet == 'a' || ansLet == 'A')
			return 0;
		if (ansLet == 'b' || ansLet == 'B')
			return 1;
		if (ansLet == 'c' || ansLet == 'C')
			return 2;
		if (ansLet == 'd' || ansLet == 'D')
			return 3;
		return -1;
	}
	
	public static char numToAnsLet(int ind) {
		if (ind == 0)
			return 'A';
		if (ind == 1)
			return 'B';
		if (ind == 2)
			return 'C';
		if (ind == 3)
			return 'D';
		return '-';
	}
}
