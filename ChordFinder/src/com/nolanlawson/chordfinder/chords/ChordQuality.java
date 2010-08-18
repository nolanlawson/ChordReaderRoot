package com.nolanlawson.chordfinder.chords;

import java.util.HashMap;
import java.util.Map;

import com.nolanlawson.chordfinder.util.ArrayUtil;

/**
 * Class represeting the quality of a chord, e.g. maj/min/aug/dim.
 * @author nolan
 *
 */
public enum ChordQuality {

	Major (new String[]{"", "major", "maj", "M"}),
	Minor (new String[]{"m", "minor", "min"}),
	Augmented (new String[]{"aug","augmented","+"}),
	Diminished (new String[]{"dim","diminished"});
	
	private String[] aliases;
	
	ChordQuality (String[] aliases) {
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	public static String[] getAllAliases() {
		String[] result = new String[0];
		
		for (ChordQuality chordQuality : values()) {
			result = ArrayUtil.concatenate(result, chordQuality.aliases);
		}
		
		return result;
	}	
	
	
	private static Map<String,ChordQuality> lookupMap = new HashMap<String, ChordQuality>();
	
	static {
		for (ChordQuality value : values()) {
			for (String alias : value.aliases) {
				lookupMap.put(alias.toLowerCase(), value);
			}
		}
	}
	
	public static ChordQuality findByAlias(String alias) {
		
		// special case for 'm'
		if (alias.equals("m")) {
			return Minor;
		} else if (alias.equals("M")) {
			return Major;
		}
		
		return lookupMap.get(alias.toLowerCase());
	}	
}
