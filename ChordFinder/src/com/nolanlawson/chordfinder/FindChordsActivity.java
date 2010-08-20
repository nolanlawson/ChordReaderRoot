package com.nolanlawson.chordfinder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.nolanlawson.chordfinder.adapter.FileAdapter;
import com.nolanlawson.chordfinder.chords.regex.ChordParser;
import com.nolanlawson.chordfinder.helper.SaveFileHelper;
import com.nolanlawson.chordfinder.helper.WebPageExtractionHelper;
import com.nolanlawson.chordfinder.util.UtilLogger;

public class FindChordsActivity extends Activity implements OnEditorActionListener, OnClickListener, TextWatcher {

	private static UtilLogger log = new UtilLogger(FindChordsActivity.class);
	
	private EditText searchEditText;
	private WebView webView;
	private View messageMainView, messageSecondaryView;
	private TextView messageTextView;
	private ProgressBar progressBar;
	private ImageView infoIconImageView;
	private Button searchButton;
	
	private CustomWebViewClient client = new CustomWebViewClient();
	
	private Handler handler = new Handler(Looper.getMainLooper());
	
	private ChordWebpage chordWebpage;
	private String html = null;
	private String url = null;
	private String currentlyOpenFile = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        setContentView(R.layout.find_chords);
        
        setUpWidgets();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
	    case R.id.menu_about:
	    	break;
	    case R.id.menu_manage_files:
	    	break;
	    case R.id.menu_open_file:
	    	showOpenFileDialog();
	    	break;
	    	
	    }
	    return false;
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}


	private void setUpWidgets() {
		
		searchEditText = (EditText) findViewById(R.id.find_chords_edit_text);
		searchEditText.setOnEditorActionListener(this);
		searchEditText.addTextChangedListener(this);
		
		webView = (WebView) findViewById(R.id.find_chords_web_view);
		webView.setWebViewClient(client);
		
		/* JavaScript must be enabled if you want it to work, obviously */  
		webView.getSettings().setJavaScriptEnabled(true);  
		  
		/* Register a new JavaScript interface called HTMLOUT */  
		webView.addJavascriptInterface(this, "HTMLOUT");  

		progressBar = (ProgressBar) findViewById(R.id.find_chords_progress_bar);
		infoIconImageView = (ImageView) findViewById(R.id.find_chords_image_view);
		searchButton = (Button) findViewById(R.id.find_chords_search_button);
		searchButton.setOnClickListener(this);
		
		messageMainView = findViewById(R.id.find_chords_message_main_view);
		messageSecondaryView = findViewById(R.id.find_chords_message_secondary_view);
		messageSecondaryView.setOnClickListener(this);
		
		messageTextView = (TextView) findViewById(R.id.find_chords_message_text_view);
		
	}

	private void showOpenFileDialog() {
		
		if (!checkSdCard()) {
			return;
		}
		
		final List<CharSequence> filenames = new ArrayList<CharSequence>(SaveFileHelper.getSavedFilenames());
		
		if (filenames.isEmpty()) {
			Toast.makeText(this, R.string.no_saved_files, Toast.LENGTH_SHORT).show();
			return;
		}
		

		
		int fileToSelect = currentlyOpenFile != null ? filenames.indexOf(currentlyOpenFile) : -1;
		
		ArrayAdapter<CharSequence> dropdownAdapter = new FileAdapter(
				this, filenames, fileToSelect, false);
		
		Builder builder = new Builder(this);
		
		builder.setTitle(R.string.open_file)
			.setCancelable(true)
			.setSingleChoiceItems(dropdownAdapter, fileToSelect == -1 ? 0 : fileToSelect, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					String filename = filenames.get(which).toString();
					openFile(filename);
					
				}
			});
		
		builder.show();
		
	}	
	private void openFile(String filename) {
		
		//TODO
		
	}
	
	
    public void showHTML(String html) { 
    	
    	log.d("html is %s", html);
    	
		this.html = html;

		handler.post(new Runnable() {
			
			@Override
			public void run() {
				urlAndHtmlLoaded();
				
			}
		});
		
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
	    	searchEditText.requestFocus();
	    	
	    	// show keyboard
	    	
			imm.showSoftInput(searchEditText, 0);	    		
	    	
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
		imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
		
		CharSequence searchText = searchEditText.getText();
		
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



	public void urlLoading(String url) {
		progressBar.setVisibility(View.VISIBLE);
		infoIconImageView.setVisibility(View.GONE);
		messageTextView.setText(R.string.loading);
		messageSecondaryView.setEnabled(false);
		
	}

	public void urlLoaded(String url) {
		
		this.url = url;
		this.chordWebpage = findKnownWebpage(url);
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				getHtmlFromWebView();
				
			}
		});

	}
	
	private void urlAndHtmlLoaded() {
		
		progressBar.setVisibility(View.GONE);
		infoIconImageView.setVisibility(View.VISIBLE);
		webView.setVisibility(View.VISIBLE);
		
		log.d("chordWebpage is: %s", chordWebpage);
		
		
		if ((chordWebpage != null && checkHtmlOfKnownWebpage())
				|| chordWebpage == null && checkHtmlOfUnknownWebpage()) {
			messageTextView.setText(R.string.chords_found);
			messageSecondaryView.setEnabled(true);			

		} else {
			messageTextView.setText(R.string.find_chords_second_message);
			messageSecondaryView.setEnabled(false);	
		}
	}
	
	private boolean checkHtmlOfUnknownWebpage() {
		
		if (url.contains("www.google.com")) {
			return false; // skip google - we're on the search results page
		}
		
		String txt = WebPageExtractionHelper.convertHtmlToText(html);
		return ChordParser.containsLineWithChords(txt);
		
	}

	private boolean checkHtmlOfKnownWebpage() {
		
		// check to make sure that, if this is a page from a known website, we can
		// be sure that there are chords on this page
		
		String chordChart = WebPageExtractionHelper.extractChordChart(
				chordWebpage, html);
		
		log.d("chordChart is: %s", chordChart);
		
		boolean result = ChordParser.containsLineWithChords(chordChart);
		
		log.d("checkHtmlOfKnownWebpage is: %s", result);
		
		return result;

	}

	private ChordWebpage findKnownWebpage(String url) {
		
		if (url.contains("www.chordie.com")) {
			return ChordWebpage.Chordie;
		}
		return null;
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.find_chords_search_button:
			performSearch();
			break;
		case R.id.find_chords_message_secondary_view:
			analyzeHtml();
			break;
		}
		
	}	

	private void analyzeHtml() {
		
		if (chordWebpage != null) {
			analyzeHtmlForKnownWebpage();
		} else {
			analyzeHtmlForUnknownWebpage();
		}
		

		
	}

	private void analyzeHtmlForUnknownWebpage() {
		
		
		
		String chordChart = WebPageExtractionHelper.extractLikelyChordChart(html);
			
		if (chordChart == null) { // didn't find a good one
			chordChart = WebPageExtractionHelper.convertHtmlToText(html);
		}
		
		showSaveChordchartDialog(chordChart);
		
	}

	private void showSaveChordchartDialog(String chordChart) {
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final EditText editText = (EditText) inflater.inflate(R.layout.confirm_chords_edit_text, null);
		editText.setText(chordChart);
		
		new AlertDialog.Builder(FindChordsActivity.this)  
		             .setTitle(R.string.confirm_chordchart)  
		             .setView(editText)
		             .setCancelable(true)
		             .setNegativeButton(android.R.string.cancel, null)
		             .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							saveChordChart(editText.getText().toString());
							
						}
					})  
		             .create()  
		             .show(); 
		
		log.d(chordChart);
		
	}

	protected void saveChordChart(final String chordChart) {
		
		if (!checkSdCard()) {
			return;
		}
		
		final EditText editText = createEditTextForFilenameSuggestingDialog();
		
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				
				if (isInvalidFilename(editText.getText())) {
					Toast.makeText(FindChordsActivity.this, R.string.enter_good_filename, Toast.LENGTH_SHORT).show();
				} else {
					
					saveFile(editText.getText().toString(), chordChart);
				}
				
				
				dialog.dismiss();
				
			}
		};
		
		showFilenameSuggestingDialog(editText, onClickListener, R.string.save_file);		
		
	}
	
	private boolean isInvalidFilename(CharSequence filename) {
		
		String filenameAsString = null;
		
		return TextUtils.isEmpty(filename)
				|| (filenameAsString = filename.toString()).contains("/")
				|| filenameAsString.contains(":")
				|| filenameAsString.contains(" ")
				|| !filenameAsString.endsWith(".txt");
				
	}	

	private void saveFile(final String filename, final String filetext) {
		
		// do in background to avoid jankiness
		
		AsyncTask<Void,Void,Boolean> saveTask = new AsyncTask<Void, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(Void... params) {
				return SaveFileHelper.saveFile(filetext, filename);
				
			}

			@Override
			protected void onPostExecute(Boolean successfullySavedLog) {
				
				super.onPostExecute(successfullySavedLog);
				
				if (successfullySavedLog) {
					Toast.makeText(getApplicationContext(), R.string.file_saved, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), R.string.unable_to_save_file, Toast.LENGTH_LONG).show();
				}
			}
			
			
		};
		
		saveTask.execute((Void)null);
		
	}	
	private EditText createEditTextForFilenameSuggestingDialog() {
		
		final EditText editText = new EditText(this);
		editText.setSingleLine();
		editText.setSingleLine(true);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
		editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
					// dismiss soft keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
					return true;
				}
				
				
				return false;
			}
		});
		
		// create an initial filename to suggest to the user
		String filename;
		if (!TextUtils.isEmpty(searchEditText.getText())) {
			filename = searchEditText.getText().toString().trim().replace(' ', '_') + ".txt";
		} else {
			filename = "filename.txt";
		}
		editText.setText(filename);
		
		// highlight everything but the .txt at the end
		editText.setSelection(0, filename.length() - 4);
		
		return editText;
	}
		
	private void showFilenameSuggestingDialog(EditText editText, 
			DialogInterface.OnClickListener onClickListener, int titleResId) {
		
		Builder builder = new Builder(this);
		
		builder.setTitle(titleResId)
			.setCancelable(true)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, onClickListener)
			.setMessage(R.string.enter_filename)
			.setView(editText);
		
		builder.show();
		
	}	
	private boolean checkSdCard() {
		
		boolean result = SaveFileHelper.checkIfSdCardExists();
		
		if (!result) {
			Toast.makeText(getApplicationContext(), R.string.sd_card_not_found, Toast.LENGTH_LONG).show();
		}
		return result;
	}
	private void analyzeHtmlForKnownWebpage() {
		String chordChart = WebPageExtractionHelper.extractChordChart(
				chordWebpage, html);
		
		showSaveChordchartDialog(chordChart);
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		searchButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// do nothing
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// do nothing
		
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

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			
			urlLoading(url);
			
		}
		
		
		
		
		
	}

}