package com.nolanlawson.chordreader.chords.regex;

public class TokenInText {

	private String token;
	private int startIndex;
	private int endIndex;
	public String getToken() {
		return token;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public int getEndIndex() {
		return endIndex;
	}

	public static TokenInText newTokenInText(String token, int startIndex, int endIndex) {
		
		TokenInText tokenInText = new TokenInText();
		
		tokenInText.token = token;
		tokenInText.startIndex = startIndex;
		tokenInText.endIndex = endIndex;
		
		return tokenInText;
	}
	
	
	
}
