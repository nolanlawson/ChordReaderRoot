package com.nolanlawson.chordfinder.chords;

import com.nolanlawson.chordfinder.util.ArrayUtil;

/**
 * Class to indicate suspended fourth or suspended 2nd
 * @author nolan
 *
 */
public enum ChordSuspended {

	Sus4 (new String[]{"sus4", "suspended", "sus"}),
	Sus2 (new String[]{"sus2", "suspended2"});
	
	private String[] aliases;
	
	ChordSuspended (String[] aliases) {
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	public static String[] getAllAliases() {
		String[] result = new String[0];
		
		for (ChordSuspended chordSuspended : values()) {
			result = ArrayUtil.concatenate(result, chordSuspended.aliases);
		}
		
		return result;
	}	
}
