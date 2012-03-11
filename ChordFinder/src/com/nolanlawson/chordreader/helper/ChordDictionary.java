package com.nolanlawson.chordreader.helper;

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

import com.nolanlawson.chordreader.R;
import com.nolanlawson.chordreader.chords.Chord;
import com.nolanlawson.chordreader.chords.NoteNaming;
import com.nolanlawson.chordreader.chords.regex.ChordParser;
import com.nolanlawson.chordreader.util.StringUtil;
import com.nolanlawson.chordreader.util.UtilLogger;

public class ChordDictionary {

	private static UtilLogger log = new UtilLogger(ChordDictionary.class);
	
	// maps chords to finger positions on guitar frets, e.g. 133211
	private static Map<Chord, List<String>> chordsToGuitarChords = null;
	
	public static void initialize(Context context) {
		Map<Chord, List<String>> result = new HashMap<Chord, List<String>>();
		
		InputStream inputStream = context.getResources().openRawResource(R.raw.chords1);
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		try {
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				line = line.trim();
				String[] tokens = StringUtil.split(line, ":");
				
				String chordText = tokens[0].trim();
				String guitarChord = tokens[1].trim();
				
				// chord dictionary is currently just in English
				Chord chord = ChordParser.parseChord(chordText, NoteNaming.English);
				
				if (chord == null) {
					log.w("Unable to parse chord text '%s'; skipping", chordText);
					continue;
				}
				
				// map chords to their string guitar chord representations
				// note that there may be multiples - e.g. there are several ways
				// to play a G chord
				List<String> existingValue = result.get(chord);
				if (existingValue == null) {
					result.put(chord, new ArrayList<String>(Collections.singleton(guitarChord)));
				} else if (!existingValue.contains(guitarChord)) {
					existingValue.add(guitarChord);
				}
				
			}
			log.i("Chord Dictionary initialized");
			chordsToGuitarChords = result;
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
