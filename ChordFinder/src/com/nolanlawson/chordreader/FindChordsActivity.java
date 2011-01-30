package com.nolanlawson.chordreader;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

import com.nolanlawson.chordreader.adapter.FileAdapter;
import com.nolanlawson.chordreader.chords.regex.ChordInText;
import com.nolanlawson.chordreader.chords.regex.ChordParser;
import com.nolanlawson.chordreader.helper.ChordDictionary;
import com.nolanlawson.chordreader.helper.DialogHelper;
import com.nolanlawson.chordreader.helper.PreferenceHelper;
import com.nolanlawson.chordreader.helper.SaveFileHelper;
import com.nolanlawson.chordreader.helper.TransposeHelper;
import com.nolanlawson.chordreader.helper.WebPageExtractionHelper;
import com.nolanlawson.chordreader.util.Pair;
import com.nolanlawson.chordreader.util.UtilLogger;

public class FindChordsActivity extends Activity implements OnEditorActionListener, OnClickListener, TextWatcher, OnTouchListener {

	private static final int PROGRESS_DIALOG_MIN_TIME = 600;
	
	private static UtilLogger log = new UtilLogger(FindChordsActivity.class);
	
	private EditText searchEditText;
	private WebView webView;
	private View messageMainView, messageSecondaryView, searchingView;
	private TextView messageTextView;
	private ProgressBar progressBar;
	private ImageView infoIconImageView;
	private Button searchButton;
	private PowerManager.WakeLock wakeLock;
	
	private CustomWebViewClient client = new CustomWebViewClient();
	
	private Handler handler = new Handler(Looper.getMainLooper());
	
	private ChordWebpage chordWebpage;
	private String html = null;
	private String url = null;
	
	private String filename;
	private String chordText;
	private List<ChordInText> chordsInText;
	private int capoFret = 0;
	private int transposeHalfSteps = 0;
	
	private TextView viewingTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        setContentView(R.layout.find_chords);
        
        setUpWidgets();
        
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, getPackageName());
        
        // initially, search rather than view chords
        switchToSearchingMode();
        
        ChordDictionary.initialize(this);
    }
    
    @Override
    public void onDestroy() {
    	
    	super.onDestroy();
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
	    	startAboutActivity();
	    	break;
	    case R.id.menu_manage_files:
	    	startDeleteSavedFilesDialog();
	    	break;
	    case R.id.menu_search_chords:
	    	switchToSearchingMode();
	    	break;
	    case R.id.menu_open_file:
	    	showOpenFileDialog();
	    	break;
	    case R.id.menu_save_chords:
	    	showSaveChordchartDialog();
	    	break;
	    case R.id.menu_transpose:
	    	createTransposeDialog();
	    	break;
	    case R.id.menu_stop:
	    	stopWebView();
	    	break;
	    case R.id.menu_refresh:
	    	refreshWebView();
	    	break;
	    case R.id.menu_settings:
	    	startSettingsActivity();
	    	break;
	    	
	    }
	    return false;
	}


	private void startSettingsActivity() {

		startActivityForResult(new Intent(this, SettingsActivity.class), 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// came back from the settings activity; need to update the text size
		PreferenceHelper.clearCache();
		viewingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferenceHelper.getTextSizePreference(this));
		
	}

	private void startAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		
		startActivity(intent);
		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		MenuItem searchChordsMenuItem = menu.findItem(R.id.menu_search_chords);
		
		boolean searchMode = searchingView.getVisibility() == View.VISIBLE;
		
		// if we're already in search mode, no need to show this menu item
		searchChordsMenuItem.setVisible(!searchMode);
		searchChordsMenuItem.setEnabled(!searchMode);
		
		// if we're not in viewing mode, there's no need to show the 'save chords' menu item
		
		MenuItem saveChordsMenuItem = menu.findItem(R.id.menu_save_chords);
		
		saveChordsMenuItem.setVisible(!searchMode);
		saveChordsMenuItem.setEnabled(!searchMode);
		
		// only show transpose in viewing mode
		
		MenuItem transposeMenuItem = menu.findItem(R.id.menu_transpose);
		
		transposeMenuItem.setVisible(!searchMode);
		transposeMenuItem.setEnabled(!searchMode);
		
		// stop and refresh only apply to searching mode
		
		MenuItem stopMenuItem = menu.findItem(R.id.menu_stop);
		MenuItem refreshMenuItem = menu.findItem(R.id.menu_refresh);
		
		boolean webViewVisible = webView.getVisibility() == View.VISIBLE;
		boolean pageLoading = progressBar.getVisibility() == View.VISIBLE; // page still loading
			
		stopMenuItem.setEnabled(searchMode && pageLoading);
		stopMenuItem.setVisible(searchMode && pageLoading);
		
		refreshMenuItem.setEnabled(searchMode && webViewVisible && !pageLoading);
		refreshMenuItem.setVisible(searchMode && webViewVisible && !pageLoading);
		
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
		
		viewingTextView = (TextView) findViewById(R.id.find_chords_viewing_text_view);
		viewingTextView.setOnTouchListener(this);
		viewingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferenceHelper.getTextSizePreference(this));
		
		searchingView = findViewById(R.id.find_chords_finding_view);
		
	}

	


	@Override
	protected void onResume() {
		super.onResume();
		
		// just in case the text size has changed
		viewingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, PreferenceHelper.getTextSizePreference(this));
		
	}

	private void refreshWebView() {
		webView.reload();
		
	}

	private void stopWebView() {
		
		Toast.makeText(this, R.string.stopping, Toast.LENGTH_SHORT).show();
		webView.stopLoading();
		
	}
	
	private void createTransposeDialog() {
		
		final View view = DialogHelper.createTransposeDialogView(this, capoFret, transposeHalfSteps);
		new Builder(this)
			.setTitle(R.string.transpose)
			.setCancelable(true)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					// grab the user's chosen values for the capo and the transposition

					View transposeView = view.findViewById(R.id.transpose_include);
					View capoView = view.findViewById(R.id.capo_include);
					
					int newTransposeHalfSteps = DialogHelper.getSeekBarValue(transposeView) + DialogHelper.TRANSPOSE_MIN;
					int newCapoFret = DialogHelper.getSeekBarValue(capoView) + DialogHelper.CAPO_MIN;
					
					log.d("transposeHalfSteps is now %d", newTransposeHalfSteps);
					log.d("capoFret is now %d", newCapoFret);
					
					changeTransposeOrCapo(newTransposeHalfSteps, newCapoFret);
					
					dialog.dismiss();
					
				}
			})
			.setView(view)
			.show();
		
	}
	
	protected void changeTransposeOrCapo(final int newTransposeHalfSteps, final int newCapoFret) {
		
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(R.string.transposing);
		progressDialog.setMessage(getText(R.string.please_wait));
		progressDialog.setIndeterminate(true);
		
		// transpose in background to avoid jankiness
		AsyncTask<Void,Void,Spannable> task = new AsyncTask<Void, Void, Spannable>(){
			
			
			
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.show();
			}

			@Override
			protected Spannable doInBackground(Void... params) {
				
				long start = System.currentTimeMillis();
				
				for (ChordInText chordInText : chordsInText) {
					
					chordInText.setChord(TransposeHelper.transposeChord(
							chordInText.getChord(), capoFret - newCapoFret, transposeHalfSteps - newTransposeHalfSteps));
				}

				capoFret = newCapoFret;
				transposeHalfSteps = newTransposeHalfSteps;
				
				Spannable chordText = buildUpChordTextToDisplay();
				

				long elapsed = System.currentTimeMillis() - start;
				
				if (elapsed < PROGRESS_DIALOG_MIN_TIME) {
					// show progressdialog for at least 1 second, or else it goes by too fast
					// XXX: this is a weird UI hack, but I don't know what else to do
					try {
						Thread.sleep(PROGRESS_DIALOG_MIN_TIME - elapsed);
					} catch (InterruptedException e) {
						log.e(e,"unexpected exception");
					}
				}
				
				
				return chordText;
				
			}

			@Override
			protected void onPostExecute(Spannable chordText) {
				super.onPostExecute(chordText);
				
				applyLinkifiedChordsTextToTextView(chordText);
				progressDialog.dismiss();
			}
			
			
		};
		
		task.execute((Void)null);

		
	}

	private void startDeleteSavedFilesDialog() {
		
		if (!checkSdCard()) {
			return;
		}
		
		List<CharSequence> filenames = new ArrayList<CharSequence>(SaveFileHelper.getSavedFilenames());
		
		if (filenames.isEmpty()) {
			Toast.makeText(this, R.string.no_saved_files, Toast.LENGTH_SHORT).show();
			return;			
		}
		
		final CharSequence[] filenameArray = filenames.toArray(new CharSequence[filenames.size()]);
		
		final FileAdapter dropdownAdapter = new FileAdapter(
				this, filenames, -1, true);
		
		final TextView messageTextView = new TextView(this);
		messageTextView.setText(R.string.select_files_to_delete);
		messageTextView.setPadding(3, 3, 3, 3);
		
		Builder builder = new Builder(this);
		
		builder.setTitle(R.string.manage_saved_files)
			.setCancelable(true)
			.setNegativeButton(android.R.string.cancel, null)
			.setNeutralButton(R.string.delete_all, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean[] allChecked = new boolean[dropdownAdapter.getCount()];
					
					for (int i = 0; i < allChecked.length; i++) {
						allChecked[i] = true;
					}
					verifyDelete(filenameArray, allChecked, dialog);
					
				}
			})
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					verifyDelete(filenameArray, dropdownAdapter.getCheckedItems(), dialog);
					
				}
			})
			.setView(messageTextView)
			.setSingleChoiceItems(dropdownAdapter, 0, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dropdownAdapter.checkOrUncheck(which);
					
				}
			});
		
		builder.show();
		
	}

	protected void verifyDelete(final CharSequence[] filenameArray,
			final boolean[] checkedItems, final DialogInterface parentDialog) {
		
		Builder builder = new Builder(this);
		
		int deleteCount = 0;
		
		for (int i = 0; i < checkedItems.length; i++) {
			if (checkedItems[i]) {
				deleteCount++;
			}
		}
		
		
		final int finalDeleteCount = deleteCount;
		
		if (finalDeleteCount > 0) {
			
			builder.setTitle(R.string.delete_saved_file)
				.setCancelable(true)
				.setMessage(String.format(getText(R.string.are_you_sure).toString(), finalDeleteCount))
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// ok, delete
					
					for (int i = 0; i < checkedItems.length; i++) {
						if (checkedItems[i]) {
							SaveFileHelper.deleteFile(filenameArray[i].toString());
						}
					}
					
					String toastText = String.format(getText(R.string.files_deleted).toString(), finalDeleteCount);
					Toast.makeText(FindChordsActivity.this, toastText, Toast.LENGTH_SHORT).show();
					
					dialog.dismiss();
					parentDialog.dismiss();
					
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.show();
		}
		
		
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
		
		int fileToSelect = filename != null ? filenames.indexOf(filename) : -1;
		
		ArrayAdapter<CharSequence> dropdownAdapter = new FileAdapter(
				this, filenames, fileToSelect, false);
		
		Builder builder = new Builder(this);
		
		builder.setTitle(R.string.open_file)
			.setCancelable(true)
			.setSingleChoiceItems(dropdownAdapter, fileToSelect, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					String filename = filenames.get(which).toString();
					openFile(filename);
					
				}
			});
		
		builder.show();
		
	}	
	
	private void openFile(String filenameToOpen) {
		
		filename = filenameToOpen;
		
		chordText = SaveFileHelper.openFile(filename);
		
		switchToViewingMode();
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
			// known webpage
			
			log.d("known web page: %s", chordWebpage);
			
			chordText = WebPageExtractionHelper.extractChordChart(
					chordWebpage, html);
		} else {
			// unknown webpage
			
			log.d("unknown webpage");
			
			chordText = WebPageExtractionHelper.extractLikelyChordChart(html);
			
			
			if (chordText == null) { // didn't find a good extraction, so use the entire html

				log.d("didn't find a good chord chart using the <pre> tag");
				
				chordText = WebPageExtractionHelper.convertHtmlToText(html);
			}
		}
		
		showConfirmChordchartDialog();
		
	}

	private void showConfirmChordchartDialog() {
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final EditText editText = (EditText) inflater.inflate(R.layout.confirm_chords_edit_text, null);
		editText.setText(chordText);
		
		new AlertDialog.Builder(FindChordsActivity.this)  
		             .setTitle(R.string.confirm_chordchart)  
		             .setView(editText)
		             .setCancelable(true)
		             .setNegativeButton(android.R.string.cancel, null)
		             .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							chordText = editText.getText().toString();
							switchToViewingMode();
							
						}
					})  
		             .create()  
		             .show(); 
		
		//log.d(chordText);
		
	}

	protected void showSaveChordchartDialog() {
		
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
					
					if (SaveFileHelper.fileExists(editText.getText().toString())) {

						new Builder(FindChordsActivity.this)
							.setCancelable(true)
							.setTitle(R.string.overwrite_file_title)
							.setMessage(R.string.overwrite_file)
							.setNegativeButton(android.R.string.cancel, null)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									saveFile(editText.getText().toString(), chordText);
									
								}
							})
							.show();
						
						
							
					} else {
						saveFile(editText.getText().toString(), chordText);
					}
					
					
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
		
		String newFilename;
		
		if (filename != null) {
			//just suggest the same filename as before
			newFilename = filename;
		} else {
			// create an initial filename to suggest to the user
			if (!TextUtils.isEmpty(searchEditText.getText())) {
				newFilename = searchEditText.getText().toString().trim().replace(' ', '_') + ".txt";
			} else {
				newFilename = "filename.txt";
			}
		}
				
		editText.setText(newFilename);
		
		// highlight everything but the .txt at the end
		editText.setSelection(0, newFilename.length() - 4);
		
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
	

	private void analyzeChordsAndShowInitialChordView() {
	
		chordsInText = ChordParser.findChordsInText(chordText);
		
		log.d("found %d chords", chordsInText.size());
		
		showInitialChordView();
		
	}



	private void showInitialChordView() {
		
		// do in the background to avoid jankiness
		
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(R.string.loading_title);
		progressDialog.setMessage(getText(R.string.please_wait));
		progressDialog.setIndeterminate(true);
		
		AsyncTask<Void,Void,Spannable> task = new AsyncTask<Void, Void, Spannable>(){

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				viewingTextView.setText(chordText); // just to display something while the user waits
				progressDialog.show();
			}

			@Override
			protected Spannable doInBackground(Void... params) {
				
				long start = System.currentTimeMillis();
				Spannable newText = buildUpChordTextToDisplay();
				
				long elapsed = System.currentTimeMillis() - start;
				
				if (elapsed < PROGRESS_DIALOG_MIN_TIME) {
					// show progressdialog for at least 1 second, or else it goes by too fast
					// XXX: this is a weird UI hack, but I don't know what else to do
					try {
						Thread.sleep(PROGRESS_DIALOG_MIN_TIME - elapsed);
					} catch (InterruptedException e) {
						log.e(e,"unexpected exception");
					}
				}
				return newText;
			}

			@Override
			protected void onPostExecute(Spannable newText) {
				super.onPostExecute(newText);
				
				applyLinkifiedChordsTextToTextView(newText);
				
				progressDialog.dismiss();
				
			}
			
		};
		
		task.execute((Void)null);
			
		
	}

	private void applyLinkifiedChordsTextToTextView(Spannable newText) {
		
		viewingTextView.setMovementMethod(LinkMovementMethod.getInstance());
		viewingTextView.setText(newText);
		viewingTextView.setLinkTextColor(ColorStateList.valueOf(getResources().getColor(R.color.linkColorBlue)));
		
		
	}

	private Spannable buildUpChordTextToDisplay() {
		
		// have to build up a new string, because some of the chords may have different string lengths
		// than in the original text (e.g. if they are transposed)
		int lastEndIndex = 0;
		
		StringBuilder sb = new StringBuilder();
		
		List<Pair<Integer,Integer>> newStartAndEndPositions = 
			new ArrayList<Pair<Integer,Integer>>(chordsInText.size());
		
		for (ChordInText chordInText : chordsInText) {
			
			//log.d("chordInText is %s", chordInText);
			
			sb.append(chordText.substring(lastEndIndex, chordInText.getStartIndex()));
			
			String chordAsString = chordInText.getChord().toPrintableString();
			
			sb.append(chordAsString);
			
			newStartAndEndPositions.add(new Pair<Integer, Integer>(
					sb.length() - chordAsString.length(), sb.length()));
			
			lastEndIndex = chordInText.getEndIndex();
		}
		
		// append the last bit of text after the last chord
		sb.append(chordText.substring(lastEndIndex, chordText.length()));
		
		Spannable spannable = new Spannable.Factory().newSpannable(sb.toString());
		
		//log.d("new start and end positions are: %s", newStartAndEndPositions);
		
		// add a hyperlink to each chord
		for (int i = 0; i < newStartAndEndPositions.size(); i++) {
			
			Pair<Integer,Integer> newStartAndEndPosition = newStartAndEndPositions.get(i);
			
			//log.d("pair is %s", newStartAndEndPosition);
			//log.d("substr is '%s'", sb.substring(
			//		newStartAndEndPosition.getFirst(), newStartAndEndPosition.getSecond()));
			
			String chordAsString = chordsInText.get(i).getChord().toPrintableString();
			
			// uri to point back to our broadcast receiver
			Uri uri = new Uri.Builder()
					.scheme(getPackageName())
					.appendPath("click_chord")
					.appendQueryParameter("chord", chordAsString)
					.build();
			
			URLSpan urlSpan = new URLSpan(uri.toString());
			spannable.setSpan(urlSpan, 
					newStartAndEndPosition.getFirst(), 
					newStartAndEndPosition.getSecond(), 
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return spannable;
	}

	private void switchToViewingMode() {

        wakeLock.acquire();
		
		resetDataExceptChordTextAndFilename();
		
		searchingView.setVisibility(View.GONE);
		viewingTextView.setVisibility(View.VISIBLE);
		
		analyzeChordsAndShowInitialChordView();
		
		
	}
	
	private void switchToSearchingMode() {
		
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		
		resetData();
		
		searchingView.setVisibility(View.VISIBLE);
		viewingTextView.setVisibility(View.GONE);
	}

	private void resetData() {
		
		chordText = null;
		filename = null;
		resetDataExceptChordTextAndFilename();
		
	}
	
	private void resetDataExceptChordTextAndFilename() {
		

		chordsInText = null;
		capoFret = 0;
		transposeHalfSteps = 0;
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		// record where the user touched so we know where to place the window, so it will be out of the way
		
		ChordLinkClickedActivity.lastXRelativeCoordinate = event.getX() / viewingTextView.getWidth();
		ChordLinkClickedActivity.lastYRelativeCoordinate = event.getY() / viewingTextView.getHeight();
		
		return false;
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