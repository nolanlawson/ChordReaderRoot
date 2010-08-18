package com.nolanlawson.chordfinder.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.test.ActivityInstrumentationTestCase2;

import com.nolanlawson.chordfinder.FindChordsActivity;
import com.nolanlawson.chordfinder.regex.ChordRegex;

public class RegexTest extends
		ActivityInstrumentationTestCase2<FindChordsActivity> {

	public RegexTest() {
		super("com.nolanlawson.chordfinder", FindChordsActivity.class);
	}

	public void test1() {
		testRegex("Cmaj7", new String[]{"C","maj","7"});
		testRegex("C#maj7", new String[]{"C#","maj","7"});
		testRegex("Dbmaj7", new String[]{"Db","maj","7"});
		testRegex("A#maj7", new String[]{"A#","maj","7"});
		testRegex("Gmaj7", new String[]{"G","maj","7"});
	}
	
	public void test2() {
		testRegex("Gadd9", new String[]{"G","","", "add9"});
		testRegex("Gadd11", new String[]{"G","","", "add11"});
		testRegex("C#add9", new String[]{"C#","","", "add9"});
		
	}
	
	public void test3() {
		testRegex("Gsus4", new String[]{"G","","", "", "sus4"});
		testRegex("G#sus", new String[]{"G#","","", "", "sus"});
		testRegex("Absus2", new String[]{"Ab","","", "", "sus2"});
		testRegex("Asus4", new String[]{"A","","", "", "sus4"});
	}	
	
	public void test4() {
		testRegex("G/F", new String[]{"G","","", "", "", "/F"});
		testRegex("G#/Bb", new String[]{"G#","","", "", "", "/Bb"});
		testRegex("C/D", new String[]{"C","","", "", "", "/D"});
	}
	
	public void test5() {
		testRegex("C7", new String[]{"C","","7"});
		testRegex("D#7", new String[]{"D#","","7"});
	}
	
	public void testRegex(String chord, String[] split) {
		
		Pattern pattern = ChordRegex.getPattern();

		Matcher matcher = pattern.matcher(chord);
		
		assertTrue(matcher.matches());
		
		for (int i = 0; i < matcher.groupCount() - 1; i++) {
			String expected = (i < split.length) ? split[i] : "";
			assertEquals(expected, matcher.group(i + 1));
		}

	}
}
