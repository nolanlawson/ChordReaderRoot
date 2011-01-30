package com.nolanlawson.chordreader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.nolanlawson.chordreader.R;
import com.nolanlawson.chordreader.util.UtilLogger;

public class PreferenceHelper {
	
	private static float textSize = -1;
	
	private static UtilLogger log = new UtilLogger(PreferenceHelper.class);
		
	public static float getTextSizePreference(Context context) {
		
		if (textSize == -1) {
		
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			String textSizePref = sharedPrefs.getString(
					context.getText(R.string.pref_text_size).toString(), 
					context.getText(R.string.text_size_medium_value).toString());

			if (textSizePref.equals(context.getText(R.string.text_size_xsmall_value))) {
				cacheTextsize(context, R.dimen.text_size_xsmall);
			} else if (textSizePref.equals(context.getText(R.string.text_size_small_value))) {
				cacheTextsize(context, R.dimen.text_size_small);
			} else if (textSizePref.equals(context.getText(R.string.text_size_medium_value))) {
				cacheTextsize(context, R.dimen.text_size_medium);
			} else if (textSizePref.equals(context.getText(R.string.text_size_large_value))) {
				cacheTextsize(context, R.dimen.text_size_large);
			} else { // xlarge
				cacheTextsize(context, R.dimen.text_size_xlarge);
			}
		}
		
		return textSize;
		
	}
	
	public static void clearCache() {
		textSize = -1;
	}
	
	private static void cacheTextsize(Context context, int dimenId) {
		
		float unscaledSize = context.getResources().getDimension(dimenId);
		
		log.d("unscaledSize is %g", unscaledSize);
		
		textSize = unscaledSize;
	}
	
	public static boolean getShowAds(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		return sharedPrefs.getBoolean(context.getText(R.string.pref_show_ads).toString(), true);
	}
	

	public static void setFirstRunPreference(Context context, boolean bool) {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPrefs.edit();
		
		editor.putBoolean(context.getString(R.string.pref_first_run), bool);
		
		editor.commit();

	}
	public static boolean getFirstRunPreference(Context context) {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPrefs.getBoolean(context.getString(R.string.pref_first_run), true);

	}

	
}
