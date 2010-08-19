package com.nolanlawson.chordfinder.chords.regex;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.nolanlawson.chordfinder.chords.Chord;
import com.nolanlawson.chordfinder.chords.ChordAdded;
import com.nolanlawson.chordfinder.chords.ChordExtended;
import com.nolanlawson.chordfinder.chords.ChordQuality;
import com.nolanlawson.chordfinder.chords.ChordRoot;
import com.nolanlawson.chordfinder.chords.ChordSuspended;
import com.nolanlawson.chordfinder.util.StringUtil;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class ChordParser {

	private static UtilLogger log = new UtilLogger(ChordParser.class);
	private static Pattern whitespacePattern = Pattern.compile("\\s+");
	
	/**
	 * Attempts to parse a string representation of a chord into a Chord object.  Returns null if it fails to match.
	 * @param chordString
	 * @return
	 */
	public static Chord parseChord(CharSequence chordString) {
		
		Pattern pattern = ChordRegex.getChordPattern();
		Matcher matcher = pattern.matcher(chordString);
		
		if (matcher.matches()) {
			
			String root = matcher.group(1);
			String qualityOrSeventh = matcher.group(2);
			String add = matcher.group(3);
			String sus = matcher.group(4);
			String overridingRoot = matcher.group(5);
			
			ChordRoot chordRoot = ChordRoot.findByAlias(root);
			if (chordRoot == null) {
				return null;
			}
			
			
			// seventh and quality are typically written in the same place in the chord name's lexical structure
			ChordQuality chordQuality = ChordQuality.Major;
			ChordExtended chordSeventh = ChordExtended.findByAlias(qualityOrSeventh);
			
			if (chordSeventh == null) {
				chordQuality = ChordQuality.findByAlias(qualityOrSeventh);
			} else {
				chordQuality = chordSeventh.getChordQuality();
			}
			
			ChordAdded chordAdded = ChordAdded.findByAlias(add);
			ChordSuspended chordSuspsended = ChordSuspended.findByAlias(sus);
			
			ChordRoot overridingChordRoot = null;
			
			if (!TextUtils.isEmpty(overridingRoot)) {
				overridingChordRoot = ChordRoot.findByAlias(overridingRoot.substring(1)); // cut off initial "/"
			}
			
			Chord chord = Chord.newChord(
					chordRoot, chordQuality, chordSeventh, chordAdded, chordSuspsended, overridingChordRoot);
			
			return chord;
			
		}
		
		return null;
		
	}
	
	/**
	 * Return true if it looks like there are "chord lines" in this text -e.g.  C   D  G  C
	 * @param text
	 * @return
	 */
	public static boolean containsLineWithChords(String text) {
		
		if (TextUtils.isEmpty(text == null ? null : text.trim())) {
			return false;
		}
		
		String[] lines = StringUtil.split(text, "\n");
		
		Pattern chordPattern = ChordRegex.getChordPattern();
		Pattern chordWithParensPattern = ChordRegex.getChordWithParensPattern();
		
		for (String line : lines) {
			
			line = line.trim();
			
			if (TextUtils.isEmpty(line)) {
				continue;
			}
			
			boolean foundNonChordToken = false;
			String[] tokens = whitespacePattern.split(line);
			//log.d("tokens are %s", Arrays.asList(tokens));
			
			for (String token : tokens) {
				if (!chordPattern.matcher(token).matches()) {
					foundNonChordToken = true;
				}
				
				// if there's a single token like (F#), then assume this is a chord line
				if (foundNonChordToken && chordWithParensPattern.matcher(token).matches()) {
					return true;
				}
			}
			
			if (!foundNonChordToken) {
				return true;
			}
			//log.d("line is %s", line);
			//log.d("foundNonChordToken is %s", foundNonChordToken);
		}
		return false;
		
	}
	
}
