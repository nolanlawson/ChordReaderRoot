package com.nolanlawson.chordfinder;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.nolanlawson.chordfinder.chords.Chord;
import com.nolanlawson.chordfinder.chords.regex.ChordParser;
import com.nolanlawson.chordfinder.helper.ChordDictionary;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class ChordLinkClickedActivity extends Activity {

	public static double lastXRelativeCoordinate;
	public static double lastYRelativeCoordinate;
	
	private static UtilLogger log = new UtilLogger(ChordLinkClickedActivity.class);
	
	private TextView mainTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setWindowPlacement();
		
		
		setContentView(R.layout.guitar_chord_dialog);
		
		setUpWidgets();
		
		fillInGuitarChord();
		
	}
	
	private void setUpWidgets() {
		mainTextView = (TextView) findViewById(R.id.guitar_chord_text_view);
	}

	private void fillInGuitarChord() {

		Intent intent = getIntent();
		
		log.d("data string is %s", intent.getDataString());
		
		// chord as string
		String chordString = intent.getData().getQueryParameter("chord");
		
		Chord chord = ChordParser.parseChord(chordString);
		
		log.d("chord is %s", chord);
		
		// TODO: flesh this out
		List<String> guitarChords = ChordDictionary.getGuitarChordsForChord(chord);
		
		String guitarChord = guitarChords == null ? "unknown" : guitarChords.get(0);
		
		mainTextView.setText(guitarChord);
		
	}

	private void setWindowPlacement() {

		log.d("relative x is %g", lastXRelativeCoordinate);
		log.d("relative y is %g", lastYRelativeCoordinate);
		
		Window window = getWindow();
		
		WindowManager.LayoutParams windowLayoutParams = window.getAttributes();
		
		// put the window out of the way of where the user just clicked
		
		if (lastYRelativeCoordinate > 0.5) { // user clicked on bottom half of screen
			windowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
		} else { // user clicked on top half of screen
			windowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
		}
		
		window.requestFeature(Window.FEATURE_NO_TITLE);

		
	}
	
}
