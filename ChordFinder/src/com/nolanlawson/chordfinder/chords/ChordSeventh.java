package com.nolanlawson.chordfinder.chords;


import static com.nolanlawson.chordfinder.chords.ChordQuality.*;

public enum ChordSeventh {

	
	Major7 (Major, new String[]{"maj7", "M7"}),
	Minor7 (Minor, new String[]{"m7", "min7", "minor7"}),
	Dominant7 (Major, new String[]{"7", "dom7", "dominant7"}),
	Diminished7 (Diminished, new String[]{"dim7", "diminished7"});
	/**
	 * TODO: add additional seventh chords
	 */
	
	private String[] aliases;
	private ChordQuality chordQuality;
	
	ChordSeventh(ChordQuality chordQuality, String[] aliases) {
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
	
}
