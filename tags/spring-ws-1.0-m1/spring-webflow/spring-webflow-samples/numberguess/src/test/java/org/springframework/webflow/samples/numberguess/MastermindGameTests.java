package org.springframework.webflow.samples.numberguess;

import junit.framework.TestCase;

import org.springframework.webflow.samples.numberguess.MastermindGame.GameData;
import org.springframework.webflow.samples.numberguess.MastermindGame.GuessResult;

public class MastermindGameTests extends TestCase {

	MastermindGame action = new MastermindGame();
	
	protected void setUp() {
		action = new MastermindGame();
	}
	
	public void testGuessNoInputProvided() throws Exception {
		GuessResult result = action.makeGuess(null);
		assertEquals(GuessResult.INVALID, result);
	}

	public void testGuessInputInvalidLength() throws Exception {
		GuessResult result = action.makeGuess("123");
		assertEquals(GuessResult.INVALID, result);
	}

	public void testGuessInputNotAllDigits() throws Exception {
		GuessResult result = action.makeGuess("12AB");
		assertEquals(GuessResult.INVALID, result);
	}

	public void testGuessInputNotUniqueDigits() throws Exception {
		GuessResult result = action.makeGuess("1111");
		assertEquals(GuessResult.INVALID, result);
	}

	public void testGuessRetry() throws Exception {
		GuessResult result = action.makeGuess("1234");
		assertEquals(GuessResult.WRONG, result);
	}

	public void testGuessCorrect() throws Exception {
		GuessResult result = action.makeGuess(null);
		assertEquals(GuessResult.INVALID, result);
		GameData data = action.getData();
		result = action.makeGuess(data.getAnswer());
		assertEquals(GuessResult.CORRECT, result);
	}
}