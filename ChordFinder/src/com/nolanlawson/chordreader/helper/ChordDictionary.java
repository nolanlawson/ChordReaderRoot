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
		Map<Chord, List<String>> dictionary = new HashMap<Chord, List<String>>();
		try {
			loadIntoChordDictionary(context, R.raw.chords1, NoteNaming.English, dictionary);
			loadIntoChordDictionary(context, R.raw.chords2, NoteNaming.NorthernEuropean, dictionary);
			
			log.i("Chord Dictionary initialized");
			chordsToGuitarChords = dictionary;
		} catch (IOException e) {
			log.e(e, "unexpected exception, couldn't initialize ChordDictionary");
		}
		
	}
	
	private static void loadIntoChordDictionary(Context context, int resId, NoteNaming noteNaming, Map<Chord, List<String>> dictionary) throws IOException {

		InputStream inputStream = context.getResources().openRawResource(resId);
		
		BufferedReader bufferedReader = null;
		
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			while (bufferedReader.ready()) {
				String line = bufferedReader.readLine();
				line = line.trim();
				String[] tokens = StringUtil.split(line, ":");
				
				String chordText = tokens[0].trim();
				String guitarChord = tokens[1].trim();
				
				Chord chord = ChordParser.parseChord(chordText, noteNaming);
				
				if (chord == null) {
					log.w("Unable to parse chord text '%s'; skipping", chordText);
					continue;
				}
				
				// map chords to their string guitar chord representations
				// note that there may be multiples - e.g. there are several ways
				// to play a G chord
				List<String> existingValue = dictionary.get(chord);
				if (existingValue == null) {
					dictionary.put(chord, new ArrayList<String>(Collections.singleton(guitarChord)));
				} else if (!existingValue.contains(guitarChord)) {
					existingValue.add(guitarChord);
				}
				
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
	}
	
	public static boolean isInitialized() {
		return chordsToGuitarChords != null;
	}

	public static List<String> getGuitarChordsForChord(Chord chord) {
		List<String> result = chordsToGuitarChords.get(chord);
		return result != null ? result : Collections.<String>emptyList();
	}
}
