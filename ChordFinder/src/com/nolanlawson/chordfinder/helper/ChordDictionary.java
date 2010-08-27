package com.nolanlawson.chordfinder.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.nolanlawson.chordfinder.R;
import com.nolanlawson.chordfinder.chords.Chord;
import com.nolanlawson.chordfinder.chords.regex.ChordParser;
import com.nolanlawson.chordfinder.util.StringUtil;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class ChordDictionary {

	private static UtilLogger log = new UtilLogger(ChordDictionary.class);
	
	// maps chords to finger positions on guitar frets, e.g. 133211
	private static Map<Chord, List<String>> chordsToGuitarChords = null;
	
	public static void initialize(Context context) {
		chordsToGuitarChords = new HashMap<Chord, List<String>>();
		
		InputStream inputStream = context.getResources().openRawResource(R.raw.chords1);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		try {
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				line = line.trim();
				String[] tokens = StringUtil.split(line, ":");
				
				String chordText = tokens[0].trim();
				String guitarChord = tokens[1].trim();
				
				Chord chord = ChordParser.parseChord(chordText);
				
				if (chord == null) {
					log.w("Unable to parse chord text '%s'; skipping", chordText);
					continue;
				}
				
				// map chords to their string guitar chord representations
				// note that there may be multiples - e.g. there are several ways
				// to play a G chord
				List<String> existingValue = chordsToGuitarChords.get(chord);
				if (existingValue == null) {
					chordsToGuitarChords.put(chord, new ArrayList<String>(Collections.singleton(guitarChord)));
				} else if (!existingValue.contains(guitarChord)) {
					existingValue.add(guitarChord);
				}
				
			}
		} catch (IOException e) {
			log.e(e, "unexpected exception");
		}
		
	}
	
	public static boolean isInitialized() {
		return chordsToGuitarChords != null;
	}

	public static List<String> getGuitarChordsForChord(Chord chord) {
		return chordsToGuitarChords.get(chord);
	}
	
	
}
