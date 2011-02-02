package com.nolanlawson.chordreader;

import java.util.Arrays;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import com.nolanlawson.chordreader.helper.PreferenceHelper;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener{
	
	private ListPreference textSizePreference;
	private CheckBoxPreference showAdsPreference;
	private ListPreference themePreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.settings);
		
		setUpPreferences();
	}
	
	private void setUpPreferences() {
		
		
		textSizePreference = (ListPreference) findPreference(getText(R.string.pref_text_size));
		
		textSizePreference.setSummary(textSizePreference.getEntry());
		
		textSizePreference.setOnPreferenceChangeListener(this);
		
		showAdsPreference = (CheckBoxPreference) findPreference(getText(R.string.pref_show_ads));
		
		showAdsPreference.setOnPreferenceChangeListener(this);
		
		themePreference = (ListPreference) findPreference(getText(R.string.pref_scheme));
		
		themePreference.setOnPreferenceChangeListener(this);
		
		CharSequence themeSummary = getText(PreferenceHelper.getColorScheme(this).getNameResource());
		
		themePreference.setSummary(themeSummary);
		
	}
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		
			
		if (preference.getKey().equals(getText(R.string.pref_text_size))) { // text size pref
			
			int index = Arrays.asList(textSizePreference.getEntryValues()).indexOf(newValue);
			CharSequence newEntry = textSizePreference.getEntries()[index];
			
			textSizePreference.setSummary(newEntry);
			return true;
		} else if (preference.getKey().equals(getText(R.string.pref_scheme))) {
			themePreference.setSummary(newValue.toString());
			return true;	
		} else { // show ads
			// TODO
			return true;
		}
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
	    	
	    	// set result and finish
	    	setResult(RESULT_OK);
	    	finish();
	    	return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreferenceHelper.clearCache();
	}
	
	
	
}
