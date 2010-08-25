package com.nolanlawson.chordfinder.chords.regex;

import java.util.Comparator;

import com.nolanlawson.chordfinder.chords.Chord;

public class ChordInText {

	private Chord chord;
	private int startIndex;
	private int endIndex;
	
	public Chord getChord() {
		return chord;
	}
	
	public void setChord(Chord chord) {
		this.chord = chord;
	}
	public int getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	public int getEndIndex() {
		return endIndex;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	
	public static ChordInText newChordInText(Chord chord, int startIndex, int endIndex) {
		
		ChordInText result = new ChordInText();
		
		result.chord = chord;
		result.startIndex = startIndex;
		result.endIndex = endIndex;
		
		return result;
	}

	public static Comparator<ChordInText> sortByStartIndex() {
		return new Comparator<ChordInText>() {

			@Override
			public int compare(ChordInText object1, ChordInText object2) {
				return object1.getStartIndex() - object2.getStartIndex();
			}};
	}
	
	@Override
	public String toString() {
		return "ChordInText [chord=" + chord + ", endIndex=" + endIndex
				+ ", startIndex=" + startIndex + "]";
	}
	
}
