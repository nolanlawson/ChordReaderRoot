package com.nolanlawson.chordfinder.chords;

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
}
