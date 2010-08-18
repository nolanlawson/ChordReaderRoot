package com.nolanlawson.chordfinder.chords.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.nolanlawson.chordfinder.chords.Chord;
import com.nolanlawson.chordfinder.chords.ChordAdded;
import com.nolanlawson.chordfinder.chords.ChordQuality;
import com.nolanlawson.chordfinder.chords.ChordRoot;
import com.nolanlawson.chordfinder.chords.ChordSeventh;
import com.nolanlawson.chordfinder.chords.ChordSuspended;

public class ChordParser {

	/**
	 * Attempts to parse a string representation of a chord into a Chord object.  Returns null if it fails to match.
	 * @param chordString
	 * @return
	 */
	public static Chord parseChord(CharSequence chordString) {
		
		Pattern pattern = ChordRegex.getPattern();
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
			ChordSeventh chordSeventh = ChordSeventh.findByAlias(qualityOrSeventh);
			
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
	
}
