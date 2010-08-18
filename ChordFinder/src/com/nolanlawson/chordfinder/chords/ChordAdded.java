package com.nolanlawson.chordfinder.chords;

import com.nolanlawson.chordfinder.util.ArrayUtil;

/**
 * Enum for add9 and add11.
 * @author nolan
 *
 */
public enum ChordAdded {

	Add9 (new String[]{"add9"}),
	Add11 (new String[]{"add11"});
	
	private String[] aliases;
	
	ChordAdded (String[] aliases) {
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	public static String[] getAllAliases() {
		String[] result = new String[0];
		
		for (ChordAdded chordAdded : values()) {
			result = ArrayUtil.concatenate(result, chordAdded.aliases);
		}
		
		return result;
	}	
}
