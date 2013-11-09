package com.example.android.snake;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Start extends Activity implements OnClickListener{
	private DIC mDict;
	private int mListCount;
	private int mList;
	private String mBookName;
	private String[] mLists;
	private String[] mBooks;
	
	private LinearLayout mMainLayout;
	private LinearLayout mHelpLayout;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.start_layout);
        
        Button startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(this);
        Button helpButton = (Button) findViewById(R.id.help_button);
        helpButton.setOnClickListener(this);
        
        mMainLayout = (LinearLayout) findViewById(R.id.start_layout);
        mHelpLayout = (LinearLayout) findViewById(R.id.help_layout);
        
        Spinner bookSpinner = (Spinner) findViewById(R.id.book_spinner);
        Spinner listSpinner = (Spinner) findViewById(R.id.list_spinner);
        mBooks = new String[2];
        
        mBooks[0] = "CET4";
        mBooks[1] = "CET6";
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        			this.getApplicationContext(), 
        			R.layout.spinner_item, mBooks);
        bookSpinner.setAdapter(adapter);
        bookSpinner.setOnItemSelectedListener(new BookOnItemSelectedListener());
        
        listSpinner.setOnItemSelectedListener(new ListOnItemSelectedListener());
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.start_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.new_game_item:
	        startGame();
	        return true;
	    case R.id.quit_item:
	        quitGame();
	        return true;
	    case R.id.help_item:
	        showHelp();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onClick(View v) {
	      // do something when the button is clicked
			switch (v.getId()) {
		    case R.id.start_button:
		        startGame();
		        return;
		    case R.id.back_button:
		        helpBack();
		        return;
		    case R.id.help_button:
		        showHelp();
		        return;
		    default:
		        return;
		    }
	    }
	
	public class BookOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	mBookName = mBooks[pos];
	    	mDict = new DIC(Start.this.getApplicationContext(), mBooks[pos]);
	    	mListCount = mDict.getlistCount();
	    	mLists = new String[mListCount];
	    	for(int i = 1; i <= mListCount; i ++)
	    	{
	    		mLists[i-1] = "List " + i; 
	    	}
	    	Spinner listSpinner = (Spinner) findViewById(R.id.list_spinner);
	    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(
        			Start.this.getApplicationContext(), 
        			R.layout.spinner_item, mLists);
	    	listSpinner.setAdapter(adapter);
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public class ListOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	mList = pos + 1;
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	private void startGame() {
		Intent it = new Intent();
		it.putExtra("com.example.android.snake.wordBookName", mBookName);
		it.putExtra("com.example.android.snake.wordList", mList);
		it.setClassName("com.example.android.snake", "com.example.android.snake.Snake");
		startActivity(it);
	}
	
	private void helpBack(){
		mMainLayout.setVisibility(View.VISIBLE);
		mHelpLayout.setVisibility(View.INVISIBLE);
	}
	
	private void showHelp(){
		mMainLayout.setVisibility(View.INVISIBLE);
		mHelpLayout.setVisibility(View.VISIBLE);
	}
	
	private void quitGame() {
		this.finish();
	}
}
