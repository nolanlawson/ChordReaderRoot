package com.nolanlawson.chordreader.chords;

import java.util.HashMap;
import java.util.Map;

import com.nolanlawson.chordreader.util.ArrayUtil;

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
	
	
	private static Map<String,ChordSuspended> lookupMap = new HashMap<String, ChordSuspended>();
	
	static {
		for (ChordSuspended value : values()) {
			for (String alias : value.aliases) {
				lookupMap.put(alias.toLowerCase(), value);
			}
		}
	}
	
	public static ChordSuspended findByAlias(String alias) {
		return lookupMap.get(alias.toLowerCase());
	}	
}
