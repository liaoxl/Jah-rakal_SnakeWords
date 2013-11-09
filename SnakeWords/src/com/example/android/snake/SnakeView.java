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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * SnakeView: implementation of a simple game of Snake
 * 
 * 
 */
public class SnakeView extends TileView {

    private static final String TAG = "SnakeView";
    private Snake mParent;
    /**
     * Current mode of application: READY to run, RUNNING, or you have already
     * lost. static final ints are used instead of an enum for performance
     * reasons.
     */
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    public static final int PASS = 4;
    public static final int DONE = 5;

    private static final int WORD_COUNT = 10;
    /**
     * Current direction the snake is headed.
     */
    private int mDirection = NORTH;
    private int mNextDirection = NORTH;
    private static final int NULL_DIRE = 0;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;

    /**
     * Labels for the drawables that will be loaded into the TileView class
     */
    // 0 for null, 1-26 for A-Z, and the following: by rockuw
    private static final int SNAKE_BODY_BALL = 27;
    private static final int KONG_BALL = 28;
    private static final int REGRET_BALL = 29;
    private static final int END_BALL = 30;

    private static final int RANDOM_APPLE_COUNT = 5;
    private static final int REGRET_BALL_COUNT = 2;
    /**
     * mScore: used to track the number of apples captured mMoveDelay: number of
     * milliseconds between snake movements. This will decrease as apples are
     * captured.
     */
    private long mScore = 0;
    private long mRemain = 4;
    private long mMoveDelay = 400;
    private long mSpeed = 50;
    private long mRightCount = 0; // time to finish the game
    /**
     * mLastMove: tracks the absolute time when the snake last moved, and is used
     * to determine if a move should be made based on mMoveDelay.
     */
    private long mLastMove = 0;
    
    /**
     * mStatusText: text shows to the user in some run states
     */
    private TextView mStatusText;
    private TextView mWordMeaningText;
    private TextView mSpeedText;
    private TextView mScoreText;
    private TextView mRemainText;
    private LinearLayout mMainLayout;

    /**
     * mSnakeTrail: a list of Coordinates that make up the snake's body
     * mAppleList: the secret location of the juicy apples the snake craves.
     */
    private ArrayList<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
    private ArrayList<Coordinate> mAppleList = new ArrayList<Coordinate>();

    /**
     * Everyone needs a little randomness in their life
     */
    private static final Random RNG = new Random();

    /**
     * Create a simple handler that we can use to cause animation to happen.  We
     * set ourselves as a target and we can use the sleep()
     * function to cause an update/invalidate to occur at a later date.
     */
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };


    /**
     * Constructs a SnakeView based on inflation from XML
     * 
     * @param context
     * @param attrs
     */
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSnakeView();
   }

    public SnakeView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	initSnakeView();
    }

    private void initSnakeView() {
        setFocusable(true);
        this.requestFocus();
        this.setFocusableInTouchMode(true);

        Resources r = this.getContext().getResources();
        
        // load tiles, by rockuw
        resetTiles(31);
        
        for(int i = 1; i <= 26; i ++)
        	loadTile(i, r.getDrawable(R.drawable.lettera + i - 1));	
        
        loadTile(SNAKE_BODY_BALL, r.getDrawable(R.drawable.body));
        loadTile(KONG_BALL, r.getDrawable(R.drawable.kong));
        loadTile(REGRET_BALL, r.getDrawable(R.drawable.regret));
        loadTile(END_BALL, r.getDrawable(R.drawable.end));
    }
    
    // dictionary
    private Word[] mWordList;
    private Word mWord = null;
    private int mWordIndex = 0;
    
    public void setWordList(Word[] list)
    {
    	mWordList = list;
    }
    
    public void setParent(Snake s)
    {
    	mParent = s;
    }
    
    private Word getNextWord()
    {
    	if(mWordIndex < WORD_COUNT)
    		return mWordList[mWordIndex ++];
    	return null;
    }
    
    private void initRecord(){
    	mRightCount = 0;
    	mMoveDelay = 400;
        mScore = 0;
        mRemain = WORD_COUNT;
        mSpeed = 50;
        mSpeedText.setText(mSpeed + "%");
        mRemainText.setText("" + mRemain);
    }
    
    private void initNewGame() {
        mSnakeTrail.clear();
        mAppleList.clear();

        // For now we're just going to load up a short default eastbound snake
        // that's just turned north

        mSnakeTrail.add(new Coordinate(7, 7, SNAKE_BODY_BALL));
        mSnakeTrail.add(new Coordinate(6, 7, SNAKE_BODY_BALL));
        mSnakeTrail.add(new Coordinate(5, 7, SNAKE_BODY_BALL));
        mSnakeTrail.add(new Coordinate(4, 7, SNAKE_BODY_BALL));
        mNextDirection = NORTH;

        // add word apple
        mWord = getNextWord();
        
        String spelling = mWord.getSpelling().toUpperCase();
        String meaning = mWord.getMeaning().trim();
        meaning = meaning.replaceAll("\n", " ");
        
        mWordMeaningText.setText(meaning);
        
        for(int i = 0; i < spelling.length(); i ++)
        {
        	addRandomApple(spelling.charAt(i) - 'A' + 1);
        }
        // add random apple
        for(int i = 0; i < RANDOM_APPLE_COUNT; i ++)
        	addRandomApple(0);
        
        // add regret apple and end apple
        for(int i = 0; i < REGRET_BALL_COUNT; i ++)
        	addRandomApple(REGRET_BALL);

        addRandomApple(END_BALL);
    }


    /**
     * Given a ArrayList of coordinates, we need to flatten them into an array of
     * ints before we can stuff them into a map for flattening and storage.
     * 
     * @param cvec : a ArrayList of Coordinate objects
     * @return : a simple array containing the x/y values of the coordinates
     * as [x1,y1,x2,y2,x3,y3...]
     */
    private int[] coordArrayListToArray(ArrayList<Coordinate> cvec) {
        int count = cvec.size();
        int[] rawArray = new int[count * 2];
        for (int index = 0; index < count; index++) {
            Coordinate c = cvec.get(index);
            rawArray[2 * index] = c.x;
            rawArray[2 * index + 1] = c.y;
        }
        return rawArray;
    }

    /**
     * Save game state so that the user does not lose anything
     * if the game process is killed while we are in the 
     * background.
     * 
     * @return a Bundle with this view's state
     */
    public Bundle saveState() {
        Bundle map = new Bundle();

        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList));
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail));

        return map;
    }

    /**
     * Given a flattened array of ordinate pairs, we reconstitute them into a
     * ArrayList of Coordinate objects
     * 
     * @param rawArray : [x1,y1,x2,y2,...]
     * @return a ArrayList of Coordinates
     */
    private ArrayList<Coordinate> coordArrayToArrayList(int[] rawArray) {
        ArrayList<Coordinate> coordArrayList = new ArrayList<Coordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }

    /**
     * Restore game state if our process is being relaunched
     * 
     * @param icicle a Bundle containing the game state
     */
    public void restoreState(Bundle icicle) {
        setMode(PAUSE);

        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
    }

    /*
     * handles key events in the game. Update the direction our snake is traveling
     * based on the DPAD. Ignore events that would cause the snake to immediately
     * turn back on itself.
     * 
     * (non-Javadoc)
     * 
     * @see android.view.View#onKeyDown(int, android.os.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
    	if(mDirection == NULL_DIRE) return super.onKeyDown(keyCode, msg); // this is for a regret apple
    	
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
    	{
    		if(mMode == READY) // a new game
    		{
    			initRecord();
    			initNewGame();
    			setMode(RUNNING);
    		}
    		else if(mMode == RUNNING) // to pause
    		{
    			setMode(PAUSE);
    		}
    		else if(mMode == PAUSE) // to resume
    		{
    			setMode(RUNNING);
    		}
    		else if(mMode == LOSE || mMode == PASS)
    		{
    			initNewGame();
    			setMode(RUNNING);
    		}
    		else if(mMode == DONE) // return to start activity
    		{
    			mParent.finish();
    		}
    		return (true);
    	}
    	if(mMode == RUNNING)
    	{
	        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
	            if (mDirection != SOUTH) {
	                mNextDirection = NORTH;
	            }
	            return (true);
	        }
	
	        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
	            if (mDirection != NORTH) {
	                mNextDirection = SOUTH;
	            }
	            return (true);
	        }
	
	        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
	            if (mDirection != EAST) {
	                mNextDirection = WEST;
	            }
	            return (true);
	        }
	
	        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
	            if (mDirection != WEST) {
	                mNextDirection = EAST;
	            }
	            return (true);
	        }
    	}
        return super.onKeyDown(keyCode, msg);
    }

    /**
     * Sets the TextView that will be used to give information (such as "Game
     * Over" to the user.
     * 
     * @param newView
     */
    public void setTextView(TextView statusView, TextView meanView, TextView speedView, 
    		TextView scoreView, TextView remainView) {
        mStatusText = statusView;
        mWordMeaningText = meanView;
        mSpeedText = speedView;
        mScoreText = scoreView;
        mRemainText = remainView;
    }

    /**
     * Updates the current mode of the application (RUNNING or PAUSED or the like)
     * as well as sets the visibility of textview for notification
     * 
     * @param newMode
     */
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;

        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
            str = res.getText(R.string.mode_pause);
        }
        if (newMode == READY) {
            str = res.getText(R.string.mode_ready);
        }
        if (newMode == LOSE) {
            str = res.getText(R.string.mode_lose);
        }
        if (newMode == PASS) {
        	str = res.getText(R.string.mode_pass);
        }
        if (newMode == DONE) {
        	str = "Report \nRight: " + mRightCount + "\nWrong: " + (WORD_COUNT-mRightCount) + "\nScore: " + mScore;
        	//this.setVisibility(View.INVISIBLE);
        }
        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }

    /**
     * Selects a random location within the garden that is not currently covered
     * by the snake. Currently _could_ go into an infinite loop if the snake
     * currently fills the garden, but we'll leave discovery of this prize to a
     * truly excellent snake-player.
     * 
     */
    // the type of apple is t-1+'A', if t == 0 then the type is random char. by rockuw
    private Boolean addRandomApple(int t) {
        Coordinate newCoord = null;
        boolean found = false;
        while (!found) {
            // Choose a new location for our apple
            int newX = 1 + RNG.nextInt(mXTileCount - 1);
            int newY = 1 + RNG.nextInt(mYTileCount - 1);
            newCoord = new Coordinate(newX, newY, t);

            // Make sure it's not already under the snake
            boolean collision = false;
            int snakelength = mSnakeTrail.size();
            for (int index = 0; index < snakelength; index++) {
                if (mSnakeTrail.get(index).equals(newCoord)) {
                    collision = true;
                }
            }
            // if we're here and there's been no collision, then we have
            // a good location for an apple. Otherwise, we'll circle back
            // and try again
            found = !collision;
        }
        if (newCoord == null) {
            Log.e(TAG, "Somehow ended up with a null newCoord!");
            return false;
        }
        // add random letter, by rockuw
        if(t == 0){
        	newCoord.type = RNG.nextInt(26) + 1;
        }
        mAppleList.add(newCoord);
        return true;
    }


    /**
     * Handles the basic update loop, checking to see if we are in the running
     * state, determining if a move should be made, updating the snake's location.
     */
    public void update() {
        if (mMode == RUNNING) {
            long now = System.currentTimeMillis();

            if (now - mLastMove > mMoveDelay) {
                clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                invalidate();
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }

    }

    /**
     * Draws some walls.
     * 
     */
    private void updateWalls() {
        // draw 4 lines
    }

    /**
     * Draws some apples.
     * 
     */
    private void updateApples() {
        for (Coordinate c : mAppleList) {
            setTile(c.type, c.x, c.y);
        }
    }

    /**
     * Figure out which way the snake is going, see if he's run into anything (the
     * walls, himself, or an apple). If he's not going to die, we then add to the
     * front and subtract from the rear in order to simulate motion. If we want to
     * grow him, we don't subtract from the rear.
     * 
     */
    private void updateSnake() {
        boolean growSnake = false;

        // grab the snake by the head
        Coordinate head = mSnakeTrail.get(0);
        Coordinate newHead = new Coordinate(1, 1);

        mDirection = mNextDirection;

        switch (mDirection) {
	        case EAST: {
	            newHead = new Coordinate(head.x + 1, head.y);
	            break;
	        }
	        case WEST: {
	            newHead = new Coordinate(head.x - 1, head.y);
	            break;
	        }
	        case NORTH: {
	            newHead = new Coordinate(head.x, head.y - 1);
	            break;
	        }
	        case SOUTH: {
	            newHead = new Coordinate(head.x, head.y + 1);
	            break;
	        }
        }

        // Collision detection
        // For now we have a 1-square wall around the entire arena
        if ((newHead.x < 0) || (newHead.y < 0) || (newHead.x > mXTileCount - 1)
                || (newHead.y > mYTileCount - 1)) {
        	mRemain --;
        	mRemainText.setText("" + mRemain);
        	if(mRemain > 0)
        		setMode(LOSE);
        	else setMode(DONE);
            return;
        }

        // Look for collisions with itself
        int snakelength = mSnakeTrail.size();
        for (int snakeindex = 0; snakeindex < snakelength; snakeindex++) {
            Coordinate c = mSnakeTrail.get(snakeindex);
            if (c.equals(newHead)) {
            	mRemain --;
            	mRemainText.setText("" + mRemain);
            	if(mRemain > 0)
            		setMode(LOSE);
            	else setMode(DONE);
                return;
            }
        }

        // Look for apples
        int applecount = mAppleList.size();
        for (int appleindex = 0; appleindex < applecount; appleindex++) {
            Coordinate c = mAppleList.get(appleindex);
            if (c.equals(newHead)) {
            	newHead.type = c.type;
                mAppleList.remove(c);
                	
                growSnake = true;
                break;
            }
        }
        
        // update snake trail, by rockuw
        mSnakeTrail.add(0, newHead);
    	int snakeLen = mSnakeTrail.size();
        if(growSnake) // catch an apple
        {
        	if(newHead.type == REGRET_BALL) // catch a regret ball
        	{
        		mSnakeTrail.remove(0);
        		mSnakeTrail.remove(0);
        		mDirection = NULL_DIRE; // this is important
        	}
        	else if(newHead.type == END_BALL) // catch an end ball
        	{
        		mRemain --;
            	mRemainText.setText("" + mRemain);
            	
        		String w = "";
        		int i = snakeLen - 1;
        		while(mSnakeTrail.get(i).type == SNAKE_BODY_BALL) i --;
        		for(; i > 0; i --)
        			w += (char)(mSnakeTrail.get(i).type + 'A' - 1);
        		if(mWord.getSpelling().equalsIgnoreCase(w)) // right
        		{
        			mRightCount ++;
        			mScore += 10;
        			mScoreText.setText(""+mScore);
        			if(mRemain > 0)
        				setMode(PASS);
        			else setMode(DONE);
        		}
        		else {
        			if(mRemain > 0)
        				setMode(LOSE);
        			else setMode(DONE);
        		}
                return;
        	}
        	else if(mSnakeTrail.get(snakeLen - 1).type == SNAKE_BODY_BALL){
        		mSnakeTrail.remove(snakeLen - 1);
        	}
        }
        else{	// no apple caught
        	for(int i = 0; i < snakeLen-1; i ++)
        	{
        		Coordinate tmp = new Coordinate(1, 1);
        		tmp.x = mSnakeTrail.get(i).x;
        		tmp.y = mSnakeTrail.get(i).y;
        		tmp.type = mSnakeTrail.get(i+1).type;
        		mSnakeTrail.set(i, tmp);
        	}
    		mSnakeTrail.remove(snakeLen - 1);
        }
        
        int index = 0;
        for (Coordinate c : mSnakeTrail) {
            if (index == 0) {
                setTile(c.type, c.x, c.y);
            } else {
                setTile(c.type, c.x, c.y);
            }
            index++;
        }
    }

    public void changeSpeed(int offset){
    	if(mSpeed <= 0 || mSpeed >= 100) return;
    	if(offset > 0){
    		mSpeed += 10;
        	mMoveDelay -= 50;
    	}
    	else if(offset < 0)
    	{
    		mSpeed -= 10;
        	mMoveDelay += 50;
    	}
    	mSpeedText.setText(mSpeed + "%");
    }
    /**
     * Simple class containing two integer values and a comparison function.
     * There's probably something I should use instead, but this was quick and
     * easy to build.
     * 
     */
    
    private class Coordinate {
        public int x;
        public int y;
        public int type;

        public Coordinate(int newX, int newY) {
            x = newX;
            y = newY;
            type = 0; // default type, by rockuw
        }
        // another constructor, by rockuw
        public Coordinate(int newX, int newY, int newType)
        {
        	x = newX;
        	y = newY;
        	type = newType;
        }

        public boolean equals(Coordinate other) {
            if (x == other.x && y == other.y) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Coordinate: [" + x + "," + y + "]";
        }
    }
}
