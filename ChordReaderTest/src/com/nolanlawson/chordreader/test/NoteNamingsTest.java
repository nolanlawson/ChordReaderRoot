package com.nolanlawson.chordreader.test;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.chordreader.FindChordsActivity;
import com.nolanlawson.chordreader.chords.Chord;
import com.nolanlawson.chordreader.chords.ChordQuality;
import com.nolanlawson.chordreader.chords.ChordRoot;
import com.nolanlawson.chordreader.chords.NoteNaming;
import com.nolanlawson.chordreader.chords.regex.ChordParser;

public class NoteNamingsTest  extends
	ActivityInstrumentationTestCase2<FindChordsActivity> {
	
	public NoteNamingsTest() {
		super("com.nolanlawson.chordreader", FindChordsActivity.class);
	}
	
	public void testEnglish() {
		
		testRegex("C", Chord.newChord(ChordRoot.C, ChordQuality.Major, null, null, null, null), NoteNaming.English);
		testRegex("D", Chord.newChord(ChordRoot.D, ChordQuality.Major, null, null, null, null), NoteNaming.English);
		testRegex("E", Chord.newChord(ChordRoot.E, ChordQuality.Major, null, null, null, null), NoteNaming.English);
		testRegex("F", Chord.newChord(ChordRoot.F, ChordQuality.Major, null, null, null, null), NoteNaming.English);
	}
	
	public void testNorthernEuropean() {
		
		testRegex("B", Chord.newChord(ChordRoot.Bb, ChordQuality.Major, null, null, null, null), NoteNaming.NorthernEuropean);
		testRegex("C", Chord.newChord(ChordRoot.C, ChordQuality.Major, null, null, null, null), NoteNaming.NorthernEuropean);
		testRegex("H", Chord.newChord(ChordRoot.B, ChordQuality.Major, null, null, null, null), NoteNaming.NorthernEuropean);
		testRegex("F", Chord.newChord(ChordRoot.F, ChordQuality.Major, null, null, null, null), NoteNaming.NorthernEuropean);
	}	

	public void testSouthernEuropean() {
		
		testRegex("Do", Chord.newChord(ChordRoot.C, ChordQuality.Major, null, null, null, null), NoteNaming.SouthernEuropean);
		testRegex("Re", Chord.newChord(ChordRoot.D, ChordQuality.Major, null, null, null, null), NoteNaming.SouthernEuropean);
		testRegex("Mi", Chord.newChord(ChordRoot.E, ChordQuality.Major, null, null, null, null), NoteNaming.SouthernEuropean);
		testRegex("Fa", Chord.newChord(ChordRoot.F, ChordQuality.Major, null, null, null, null), NoteNaming.SouthernEuropean);
	}	
	
	public void testRegex(String chordString, Chord expected, NoteNaming noteNaming) {
		
		Chord chord = ChordParser.parseChord(chordString, noteNaming);
		assertEquals(expected, chord);

	}
}
