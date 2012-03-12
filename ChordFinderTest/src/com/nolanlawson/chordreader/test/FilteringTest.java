package com.nolanlawson.chordreader.test;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.chordreader.FindChordsActivity;
import com.nolanlawson.chordreader.chords.NoteNaming;
import com.nolanlawson.chordreader.chords.regex.ChordParser;

public class FilteringTest extends ActivityInstrumentationTestCase2<FindChordsActivity> {

	public FilteringTest() {
		super("com.nolanlawson.chordreader", FindChordsActivity.class);
	}
	
	public void testBasicFiltering() {
		// "Am I"
		testNumChords("Am I a fool?", 0);
		testNumChords("[Am] Oh, I'm such a fool", 1);
		
		// "A dog/cat/rat/foo"
		testNumChords("It's-A me, Mario!", 0);
		testNumChords("A G Em C", 4);
		testNumChords("A lady stepping from the songs we loved until this day", 0);
		testNumChords("[D] I see a[A] bad [G]moon [D]rising", 4);
		
		// letter plus period
		testNumChords("This is a song by R. E. M., aka R.E.M.", 0);
		
		
	}
	
	private void testNumChords(String text, int expected) {
		assertEquals(expected, ChordParser.findChordsInText(text, NoteNaming.English).size());
	}

}
