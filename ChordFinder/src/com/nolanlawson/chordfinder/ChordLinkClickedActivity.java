package com.nolanlawson.chordfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.nolanlawson.chordfinder.util.UtilLogger;

public class ChordLinkClickedActivity extends Activity {

	private static UtilLogger log = new UtilLogger(ChordLinkClickedActivity.class);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Window window = getWindow();
		
		WindowManager.LayoutParams windowLayoutParams = window.getAttributes();
		
		windowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
		
		window.requestFeature(Window.FEATURE_NO_TITLE);
		
		Intent oldIntent = getIntent();
		
		Intent intent = new Intent();
		intent.setData(oldIntent.getData());
		
		log.d("Broadcasting intent %s",intent);
		sendBroadcast(intent);
		finish();
	}
	
}
