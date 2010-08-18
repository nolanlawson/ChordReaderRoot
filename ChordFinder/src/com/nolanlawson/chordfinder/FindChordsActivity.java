package com.nolanlawson.chordfinder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.nolanlawson.chordfinder.helper.WebPageExtractionHelper;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class FindChordsActivity extends Activity implements OnEditorActionListener {

	private static UtilLogger log = new UtilLogger(FindChordsActivity.class);
	
	private EditText editText;
	private WebView webView;
	private View messageMainView, messageSecondaryView;
	private TextView messageTextView;
	
	private CustomWebViewClient client = new CustomWebViewClient();
	
	private Handler handler = new Handler(Looper.getMainLooper());
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        setContentView(R.layout.find_chords);
        
        setUpWidgets();
    }

	private void setUpWidgets() {
		
		editText = (EditText) findViewById(R.id.find_chords_edit_text);
		editText.setOnEditorActionListener(this);
		
		webView = (WebView) findViewById(R.id.find_chords_web_view);
		webView.setWebViewClient(client);
		
		/* JavaScript must be enabled if you want it to work, obviously */  
		webView.getSettings().setJavaScriptEnabled(true);  
		  
		/* Register a new JavaScript interface called HTMLOUT */  
		webView.addJavascriptInterface(this, "HTMLOUT");  

		
		
		messageMainView = findViewById(R.id.find_chords_message_main_view);
		messageSecondaryView = findViewById(R.id.find_chords_message_secondary_view);
		
		messageTextView = (TextView) findViewById(R.id.find_chords_message_text_view);
		
	}
	
    public void showHTML(String html) { 
    	
		String chordChart = WebPageExtractionHelper.extractChordChart(
				WebPageExtractionHelper.ChordWebpage.UltimateGuitar, html);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		EditText editText = (EditText) inflater.inflate(R.layout.confirm_chords_edit_text, null);
		editText.setText(chordChart);
		
		new AlertDialog.Builder(FindChordsActivity.this)  
		             .setTitle(R.string.confirm_chordchart)  
		             .setView(editText)
		             .setCancelable(true)
		             .setNegativeButton(android.R.string.cancel, null)
		             .setPositiveButton(android.R.string.ok, null)  
		             .create()  
		             .show(); 
		
		log.d(chordChart);

     } 


	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
			performSearch();
			return true;
		}
		
		
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	if (webView.canGoBack()) {
	    		webView.goBack();
	    		return true;
	    	}
	    } else if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {

	    	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	    	editText.requestFocus();
	    	
	    	// show keyboard
	    	
			imm.showSoftInput(editText, 0);	    		
	    	
	    	return true;
	    	

	    }

	    return super.onKeyDown(keyCode, event);
	}	
	
	/**
     * Select Text in the webview and automatically sends the selected text to the clipboard
     */
    private void selectAndCopyText() {
        try {
         KeyEvent shiftPressEvent = new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_SHIFT_LEFT,0,0);
         shiftPressEvent.dispatch(webView);
         
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    } 

	private void performSearch() {
		
		// dismiss soft keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		
		CharSequence searchText = editText.getText();
		
		if (TextUtils.isEmpty(searchText)) {
			return;
		}
		
		searchText = searchText + " " + getText(R.string.chords_keyword);
		
		String urlEncoded = null;
		try {
			urlEncoded = URLEncoder.encode(searchText.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.e(e, "this should never happen");
		}
		
		loadUrl("http://www.google.com/search?q=" + urlEncoded);
		
	}

	private void loadUrl(String url) {
		
		log.d("url is: %s", url);
		
		webView.loadUrl(url);
		
	}


	private void getHtmlFromWebView() {
        webView.loadUrl("" +
         		"javascript:window.HTMLOUT.showHTML(" +
         		"'<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');"); 
		
	}



	public void urlLoaded(String url) {
		
		if (url.contains("www.ultimate-guitar.com")) {
			messageTextView.setText(R.string.chords_found);
			messageSecondaryView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					getHtmlFromWebView();
					
				}
			});

		} else {
			messageTextView.setText(R.string.find_chords_intro_message);
			messageSecondaryView.setOnClickListener(null);
		}
		
	}
	
	
	private class CustomWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, final String url) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					loadUrl(url);
					
				}
			});
			
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			urlLoaded(url);
		}
		
		
		
		
	}


}