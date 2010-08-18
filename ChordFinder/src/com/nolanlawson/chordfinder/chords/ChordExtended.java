package com.nolanlawson.chordfinder.chords;


import static com.nolanlawson.chordfinder.chords.ChordQuality.*;

import java.util.HashMap;
import java.util.Map;

import com.nolanlawson.chordfinder.util.ArrayUtil;

/**
 * e.g. seventh, ninth, elevenths
 * @author nolan
 *
 */
public enum ChordExtended {

	
	// sevenths
	Major7 (Major, new String[]{"maj7", "M7", "+7"}),
	Minor7 (Minor, new String[]{"m7", "min7", "minor7"}),
	Dominant7 (Major, new String[]{"7", "dom7", "dominant7"}),
	Diminished7 (Diminished, new String[]{"dim7", "diminished7"}),
	
	// true extended
	Major9 (Major, new String[]{"maj9", "M9", "9"}),
	Major11 (Major, new String[]{"maj11", "M11", "11"}),
	Major13 (Major, new String[]{"maj13", "M13", "13"}),
	
	// weird ones
	
	AugmentedDominant7 (Major, new String[]{"7#5", "7(#5)"}),
	AugmentedMajor7 (Major, new String[]{"maj7#5", "maj7(#5)"}),
	
	;
	/**
	 * TODO: add additional seventh chords
	 */
	
	private String[] aliases;
	private ChordQuality chordQuality;
	
	ChordExtended(ChordQuality chordQuality, String[] aliases) {
		this.chordQuality = chordQuality;
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return aliases;
	}

	/**
	 * A chord quality is inherent to every type of seventh.  See the wikipedia page for more info.
	 * http://en.wikipedia.org/wiki/Seventh_chord#Types_of_seventh_chords
	 * @return
	 */
	public ChordQuality getChordQuality() {
		return chordQuality;
	}
	
	
	public static String[] getAllAliases() {
		String[] result = new String[0];
		
		for (ChordExtended chordSeventh : values()) {
			result = ArrayUtil.concatenate(result, chordSeventh.aliases);
		}
		
		return result;
	}		
	
	
	private static Map<String,ChordExtended> lookupMap = new HashMap<String, ChordExtended>();
	
	static {
		for (ChordExtended value : values()) {
			for (String alias : value.aliases) {
				lookupMap.put(alias.toLowerCase(), value);
			}
		}
	}
	
	public static ChordExtended findByAlias(String alias) {
		
		// special case for M7 and m7
		if (alias.equals("M7")) {
			return Major7;
		} else if (alias.equals("m7")) {
			return Minor7;
		}
		
		return lookupMap.get(alias.toLowerCase());
	}	
	
}
