package com.nolanlawson.chordreader.db;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class QueryCursorAdapter extends CursorAdapter {

	private ChordReaderDBHelper dbHelper;
	private long queryLimit;
	
	public QueryCursorAdapter(Context context, int layout, long queryLimit, Cursor cursor) {
		super(context, cursor);
		dbHelper = new ChordReaderDBHelper(context);
		this.queryLimit = queryLimit;
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		return cursor.getString(1);
	}

	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		return dbHelper.findAllQueries(queryLimit, constraint);
	}
	
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(android.R.layout.simple_dropdown_item_1line,
                        parent, false);

        return view;
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final CharSequence text = convertToString(cursor);
        ((TextView) view).setText(text);
    }
	
	public void destroy() {
		if (dbHelper != null) {
			dbHelper.close();
		}
	}
}
