package com.nolanlawson.chordreader.chords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class represeting the quality of a chord, e.g. maj/min/aug/dim.
 * @author nolan
 *
 */
public enum ChordQuality {

	Major (Arrays.asList("", "major", "maj", "M")),
	Minor (Arrays.asList("m", "minor", "min")),
	Augmented (Arrays.asList("aug","augmented","+")),
	Diminished (Arrays.asList("dim","diminished"));
	
	private List<String> aliases;
	
	ChordQuality (List<String> aliases) {
		this.aliases = aliases;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	public static List<String> getAllAliases() {
		List<String> result = new ArrayList<String>();
		
		for (ChordQuality chordQuality : values()) {
			result.addAll(chordQuality.aliases);
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
