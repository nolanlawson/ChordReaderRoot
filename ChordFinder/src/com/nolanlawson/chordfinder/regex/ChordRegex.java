package com.nolanlawson.chordfinder.regex;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.nolanlawson.chordfinder.chords.ChordAdded;
import com.nolanlawson.chordfinder.chords.ChordQuality;
import com.nolanlawson.chordfinder.chords.ChordRoot;
import com.nolanlawson.chordfinder.chords.ChordSuspended;
import com.nolanlawson.chordfinder.util.ArrayUtil;

public class ChordRegex {

	private static final String REGEX =
		
			greedyDisjunction(ChordRoot.getAllAliases(), true) + // root note
			optional(greedyDisjunction(ChordQuality.getAllAliases())) + // quality
			optional("7") + // seven
			optional(greedyDisjunction(ChordAdded.getAllAliases())) + // add
			optional(greedyDisjunction(ChordSuspended.getAllAliases())) + // sus
			optional("(?:/" + greedyDisjunction(ChordRoot.getAllAliases()) + ")") + // overridden root note ("over")
			"";
	
	private static Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);

	
	
	
	public static Pattern getPattern() {
		return pattern;
	}

	public static String optional(String pattern) {
		return "(" + pattern + "?)";
	}
	
	private static String greedyDisjunction(String[] aliases) {
		return greedyDisjunction(aliases, false);
	}
	
	/**
	 * Take an array of strings and make a greedy disjunction regex pattern out of it,
	 * with the longest strings first, e.g. ["sus4","sus","sus2"] -->
	 * 
	 * (sus4|sus2|sus)
	 * @param allAliases
	 * @return
	 */
	private static String greedyDisjunction(String[] aliases, boolean matchingGroup) {
		
		aliases = ArrayUtil.copyOf(aliases, aliases.length);
		
		// sort by longest string first
		Arrays.sort(aliases, new Comparator<Object>(){
			
			@Override
			public int compare(Object o1, Object o2) {
				return ((String)o2).length() - ((String)o1).length();
			}
			
			
		});
		
		StringBuilder stringBuilder = new StringBuilder("(");

		if (!matchingGroup) {
			stringBuilder.append("?:"); // non-matching group
		}
		
		
		for (int i = 0; i < aliases.length; i++) {
			
			String alias = aliases[i];
			
			if (TextUtils.isEmpty(alias)) {
				continue; // e.g. the "major" quality can be expressed as an empty string, so skip in the regex
			}
			
			stringBuilder.append(Pattern.quote(alias)).append("|");
		}
		
		// get rid of final dangling pipe |
		return stringBuilder.substring(0,stringBuilder.length() - 1) + ")";
		
	}
	
}
