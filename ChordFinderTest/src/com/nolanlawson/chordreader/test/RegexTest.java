package com.nolanlawson.chordreader.test;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.chordreader.FindChordsActivity;
import com.nolanlawson.chordreader.chords.Chord;
import com.nolanlawson.chordreader.chords.NoteNaming;
import com.nolanlawson.chordreader.chords.regex.ChordParser;

import static com.nolanlawson.chordreader.chords.ChordAdded.*;
import static com.nolanlawson.chordreader.chords.ChordExtended.*;
import static com.nolanlawson.chordreader.chords.ChordQuality.*;
import static com.nolanlawson.chordreader.chords.ChordRoot.*;
import static com.nolanlawson.chordreader.chords.ChordSuspended.*;

public class RegexTest extends
		ActivityInstrumentationTestCase2<FindChordsActivity> {

	public RegexTest() {
		super("com.nolanlawson.chordreader", FindChordsActivity.class);
	}

	public void test1() {
		testRegex("Cmaj7", Chord.newChord(C, Major, Major7, null, null, null));
		testRegex("C#maj7", Chord.newChord(Db, Major, Major7, null, null, null));
		testRegex("Amaj7", Chord.newChord(A, Major, Major7, null, null, null));
		testRegex("A#maj7", Chord.newChord(Bb, Major, Major7, null, null, null));
		testRegex("Dmaj7", Chord.newChord(D, Major, Major7, null, null, null));
		testRegex("Gmaj7", Chord.newChord(G, Major, Major7, null, null, null));

	}
	
	public void test2() {
		testRegex("Gadd9", Chord.newChord(G, Major, null, Add9, null, null));
		testRegex("Gadd11", Chord.newChord(G, Major, null, Add11, null, null));
		testRegex("Abadd9", Chord.newChord(Ab, Major, null, Add9, null, null));
		testRegex("Abminadd9", Chord.newChord(Ab, Minor, null, Add9, null, null));
		

		
	}
	
	public void test3() {
		testRegex("Gsus4", Chord.newChord(G, Major, null, null, Sus4, null));
		testRegex("G#sus", Chord.newChord(Ab, Major, null, null, Sus4, null));
		testRegex("Absus2", Chord.newChord(Ab, Major, null, null, Sus2, null));
		testRegex("Asus4", Chord.newChord(A, Major, null, null, Sus4, null));

	}	
	
	public void test4() {
		testRegex("G/F", Chord.newChord(G, Major, null, null, null, F));
		testRegex("G#/Bb", Chord.newChord(Ab, Major, null, null, null, Bb));
		testRegex("C/D", Chord.newChord(C, Major, null, null, null, D));
	}
	
	public void test5() {
		testRegex("C7", Chord.newChord(C, Major, Dominant7, null, null, null));
		testRegex("D#7", Chord.newChord(Eb, Major, Dominant7, null, null, null));
		testRegex("D#maj7", Chord.newChord(Eb, Major, Major7, null, null, null));
		testRegex("D#m7", Chord.newChord(Eb, Minor, Minor7, null, null, null));
		testRegex("D#min7", Chord.newChord(Eb, Minor, Minor7, null, null, null));
		testRegex("D#M7", Chord.newChord(Eb, Major, Major7, null, null, null));
	}
	
	public void test6() {
		testRegex("C", Chord.newChord(C, Major, null, null, null, null));
		testRegex("CM", Chord.newChord(C, Major, null, null, null, null));
		testRegex("Cm", Chord.newChord(C, Minor, null, null, null, null));
		
	}
	
	public void test7() {
		testRegex("C2", Chord.newChord(C, Major, null, Add9, null, null));
		testRegex("C4", Chord.newChord(C, Major, null, Add11, null, null));
	}
	
	public void test8() {
		testRegex("C9", Chord.newChord(C, Major, Major9, null, null, null));
		testRegex("C11", Chord.newChord(C, Major, Major11, null, null, null));
		testRegex("C13", Chord.newChord(C, Major, Major13, null, null, null));
	}
	
	public void test9() {
		testRegex("Foobar", null);
	}
	
	public void test10() {
		testRegex("Am9", Chord.newChord(A, Minor, Minor9, null, null, null));
		testRegex("C6", Chord.newChord(C, Major, null, Major6, null, null));
		
	}
	
	public void testContainsLineWithChords() {
		assertFalse(ChordParser.containsLineWithChords("", NoteNaming.English));
		assertFalse(ChordParser.containsLineWithChords("Hello world", NoteNaming.English));
		assertFalse(ChordParser.containsLineWithChords(
				"Hello my Coney Island baby\nHello my Coney Island gal", NoteNaming.English));
		
		assertTrue(ChordParser.containsLineWithChords("C", NoteNaming.English));
		assertTrue(ChordParser.containsLineWithChords("Hello world (C)", NoteNaming.English));
		assertTrue(ChordParser.containsLineWithChords("Hello World\nC  F  G  Am", NoteNaming.English));
		assertTrue(ChordParser.containsLineWithChords("C F G Am\nHello World", NoteNaming.English));
		assertTrue(ChordParser.containsLineWithChords("(F#m) Hello world", NoteNaming.English));
		assertTrue(ChordParser.containsLineWithChords("Another Century of Fakers\nIntro: D\nD       A    " +
				"\nThere are people going hungry every day", NoteNaming.English));
		
	}

	
	
	public void testRegex(String chordString, Chord expected) {
		
		Chord chord = ChordParser.parseChord(chordString, NoteNaming.English);
		
		assertEquals(expected, chord);

	}
}
