/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Snake: a simple game that everyone can enjoy.
 * 
 * This is an implementation of the classic Game "Snake", in which you control a
 * serpent roaming around the garden looking for apples. Be careful, though,
 * because when you catch one, not only will you become longer, but you'll move
 * faster. Running into yourself or the walls will end the game.
 * 
 */
public class Snake extends Activity {

	private static final int WORD_COUNT = 10;
	private DIC mDict;
	private String mWordBookName;
	private int mWordList;
	private Word[] mWordListArray;
    private SnakeView mSnakeView;
    private TextView mWordListTextView;
    private LinearLayout mMainLayout;
    private MenuItem mReviewItem;
    private Boolean mIsReview = true;
    
    private static String ICICLE_KEY = "snake-view";

    /**
     * Called when Activity is first created. Turns off the title bar, sets up
     * the content views, and fires up the SnakeView.
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snake_layout);

        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setParent(this);
        mSnakeView.setTextView((TextView) findViewById(R.id.status_textview),
        					   (TextView) findViewById(R.id.word_meaning_textview),
        					   (TextView) findViewById(R.id.speed_textview),
        					   (TextView) findViewById(R.id.score_textview),
        					   (TextView) findViewById(R.id.remain_textview));
        mWordListTextView = (TextView) findViewById(R.id.wordlist_textview);
        mMainLayout = (LinearLayout) findViewById(R.id.main_linearlayout);
        
        Intent it = getIntent();
        mWordBookName = it.getStringExtra("com.example.android.snake.wordBookName");
        mWordList = it.getIntExtra("com.example.android.snake.wordList", 0);
        
        setWordList();
        mSnakeView.setMode(SnakeView.READY);
        
        if (savedInstanceState == null) {
            // We were just launched -- set up a new game
            //mSnakeView.setMode(SnakeView.READY);
        } else {
            // We are being restored
            //Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
            //if (map != null) {
            //    mSnakeView.restoreState(map);
            //} else {
            //    mSnakeView.setMode(SnakeView.PAUSE);
            //}
        }
    }

    private void setWordList()
    {
    	mDict = new DIC(this.getApplicationContext(), mWordBookName);
        mDict.loadList("list_" + mWordList);
        mWordListArray = mDict.getRandomWords(WORD_COUNT);
        mSnakeView.setWordList(mWordListArray);
        
        String wordlistString = "";
        for(int i = 0; i < mWordListArray.length; i ++)
        	wordlistString += 
        		(mWordListArray[i].getSpelling() + " " + mWordListArray[i].getMeaning() + "\n");
        mWordListTextView.setText(wordlistString);
    }
    
    private void startGame() {
		mWordListTextView.setVisibility(View.INVISIBLE);
		mMainLayout.setVisibility(View.VISIBLE);
		
		mReviewItem.setTitle("ReviewWords");
		mIsReview = false;
	}
    
    private void reviewWords() {
    	mWordListTextView.setVisibility(View.VISIBLE);
		mMainLayout.setVisibility(View.INVISIBLE);
		
		mReviewItem.setTitle("StartGame");
		mIsReview = true;
	}
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the game along with the activity
        //mSnakeView.setMode(SnakeView.PAUSE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Store the game state
        //outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.snake_menu, menu);
	    mReviewItem = menu.findItem(R.id.reviewwords_item);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.speedup_item:
	        mSnakeView.changeSpeed(+1);
	        return true;
	    case R.id.slowdown_item:
	    	mSnakeView.changeSpeed(-1);
	        return true;
	    case R.id.reviewwords_item:
	    	if(!mIsReview)
	    		reviewWords();
	    	else
	    		startGame();
	    	return true;
	    case R.id.snake_quit_item:
	    	this.finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
