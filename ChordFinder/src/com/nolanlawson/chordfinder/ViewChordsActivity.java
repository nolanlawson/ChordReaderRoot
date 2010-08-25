package com.nolanlawson.chordfinder;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.nolanlawson.chordfinder.chords.regex.ChordInText;
import com.nolanlawson.chordfinder.chords.regex.ChordParser;
import com.nolanlawson.chordfinder.helper.SaveFileHelper;
import com.nolanlawson.chordfinder.util.StringUtil;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class ViewChordsActivity extends Activity {

	private static UtilLogger log = new UtilLogger(ViewChordsActivity.class);
	
	public static final String EXTRA_FILENAME = "filename";
	public static final String EXTRA_CHORDTEXT = "chordText";
	
	private String filename;
	private String chordText;
	private List<ChordInText> chordsInText;
	
	private TextView mainTextView;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_chords);
		
		initializeContentFromIntent();
		
		setUpWidgets();
		
		analyzeChords();
		
	}



	private void analyzeChords() {
	
		chordsInText = ChordParser.findChordsInText(chordText);
		
		log.d("found %d chords", chordsInText.size());
		
		// walk backwards through each chord from finish to start
		Collections.sort(chordsInText, Collections.reverseOrder(ChordInText.sortByStartIndex()));
		
		StringBuilder stringBuilder = new StringBuilder();
		
		int lastStartIndex = chordText.length();
		
		// add a hyperlink to each chord
		for (ChordInText chordInText : chordsInText) {
			
			stringBuilder.insert(0, htmlEscape(chordText.substring(chordInText.getEndIndex(), lastStartIndex)));
			
			stringBuilder.insert(0, 
					"<a href=\"http://www.google.com\">" + chordInText.getChord().toPrintableString() + "</a>");
			
			lastStartIndex = chordInText.getStartIndex();
		}
		
		
		
		// insert the beginning of the text last
		stringBuilder.insert(0, htmlEscape(chordText.substring(0, lastStartIndex)));

		mainTextView.setText(Html.fromHtml(stringBuilder.toString()));
		mainTextView.setLinkTextColor(ColorStateList.valueOf(getResources().getColor(R.color.linkColorBlue)));
		
		
	}



	private Object htmlEscape(String str) {
		return StringUtil.replace(StringUtil.replace(TextUtils.htmlEncode(str), "\n", "<br/>")," ","&nbsp;");
	}



	private void setUpWidgets() {
		
		mainTextView = (TextView) findViewById(R.id.view_chords_text_view);
		mainTextView.setMovementMethod(LinkMovementMethod.getInstance());
		mainTextView.setText(chordText);
		
		
	}



	private void initializeContentFromIntent() {
		
		Intent intent = getIntent();
		

		if (intent.hasExtra(EXTRA_FILENAME)) {
			filename = intent.getStringExtra(EXTRA_FILENAME);
		}
		if (intent.hasExtra(EXTRA_CHORDTEXT)) {
			chordText = intent.getStringExtra(EXTRA_CHORDTEXT);
		}
		
		if (chordText == null) {
			chordText = SaveFileHelper.openFile(filename);
		}
		
	}
	
}
