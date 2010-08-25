package com.nolanlawson.chordfinder.chords.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private static Pattern nonWhitespacePattern = Pattern.compile("\\S+");
	
	/**
	 * Attempts to parse a string representation of a chord into a Chord object.  Returns null if it fails to match.
	 * @param chordString
	 * @return
	 */
	public static Chord parseChord(CharSequence chordString) {
		
		Pattern pattern = ChordRegex.getChordPattern();
		Matcher matcher = pattern.matcher(chordString);
		
		if (matcher.matches()) {
			return convertMatchedPatternToChord(matcher);
			
		}
		
		return null;
		
	}
	
	private static Chord convertMatchedPatternToChord(Matcher matcher) {

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
		
		for (String line : lines) {
			if (isLineContainingChords(line)) {
				return true;
			}
		}
		return false;
		
	}

	private static boolean isLineContainingChords(String line) {
		return !findChordsInTextInLine(line, 0).isEmpty();

	}

	public static List<ChordInText> findChordsInText(String text) {
		
		List<ChordInText> result = new ArrayList<ChordInText>();
		
		String[] lines = StringUtil.split(text, "\n");
		
		int offset = 0;
		
		for (String line : lines) {
			
			result.addAll(findChordsInTextInLine(line, offset));
			
			offset += line.length() + 1; // plus one for the \n
			
		}
		
		return result;
		
	}
	
	private static List<ChordInText> findChordsInTextInLine(String line, int offset) {
		
		if (TextUtils.isEmpty(line.trim())) {
			return Collections.emptyList();
		}
		
		// heuristic description:
		// find all chord tokens that are adjacent - those are pretty sure to be chords unless it's a common bigram like "am a"
		// also, any chords in parens are sure-fire chords as well
		
		List<ChordInText> result = new ArrayList<ChordInText>();
		
		TokenInText[] tokens = getTokensInTextFromLine(line);
		
		ChordInText[] candidateChordsInText = null;
		
		Pattern chordPattern = ChordRegex.getChordPattern();
		Pattern chordWithParensPattern = ChordRegex.getChordWithParensPattern();
		
		for (int i = 0; i < tokens.length; i++) {
			TokenInText tokenInText = tokens[i];
			String token = tokenInText.getToken();
			Matcher matcher = chordWithParensPattern.matcher(token);
			if (matcher.matches()) {
				// add a sure-fire chord and continue
				Chord chord = convertMatchedPatternToChord(matcher);
				ChordInText chordInText = ChordInText.newChordInText(chord, tokenInText.getStartIndex() + offset, tokenInText.getEndIndex() + offset);
				result.add(chordInText);
			} else {
				// add some candidate chordsInText to the array
				matcher = chordPattern.matcher(token);
				if (matcher.matches()) {
					Chord chord = convertMatchedPatternToChord(matcher);
					ChordInText chordInText = ChordInText.newChordInText(chord, tokenInText.getStartIndex() + offset, tokenInText.getEndIndex() + offset);
					
					if (candidateChordsInText == null) {
						candidateChordsInText = new ChordInText[tokens.length];
					}
					
					candidateChordsInText[i] = chordInText;
				}
				
			}
		}
		
		// check all the candidates to see which ones are adjacent.  All adjacents ones (i.e. tokens that aren't just
		// some instance of the word 'a' or 'am' or something) are sure-fire bets
		
		if (candidateChordsInText != null) {
			
			for (int i = 0; i < candidateChordsInText.length; i++) {
				
				ChordInText candidateChordInText = candidateChordsInText[i];
				
				if (candidateChordInText == null) {
					continue;
				}
				
				if (candidateChordInText.getEndIndex() - candidateChordInText.getStartIndex() > 3) {
					// very likely to be a chord, if the length of the token is >3
					result.add(candidateChordInText);
					continue;
				}
				
				if (candidateChordsInText.length == 1) { // also likely to be a chord, if it's the only one
					result.add(candidateChordInText);
					continue;
				}
				
				boolean hasPreviousChord = i > 0 && candidateChordsInText[i - 1] != null;
				boolean hasNextChord = i < candidateChordsInText.length - 1 && candidateChordsInText[i + 1] != null;
				
				// probably a valid chord
				if (hasPreviousChord || hasNextChord) {
					
					result.add(candidateChordsInText[i]);
				}
			}
		}
		
		
		return result;		
	}

	private static TokenInText[] getTokensInTextFromLine(String line) {
		
		List<TokenInText> result = new ArrayList<TokenInText>();
		
		Matcher matcher = nonWhitespacePattern.matcher(line);
		
		while (matcher.find()) {
			result.add(TokenInText.newTokenInText(matcher.group(), matcher.start(), matcher.end()));
		}
		
		return result.toArray(new TokenInText[result.size()]);
		
	}
	
}
