package com.pseudoblue.greentree;

import com.pseudoblue.greentree.Tree.BugEvent;
import com.pseudoblue.greentree.Tree.StormEvent;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

public class TreeGame extends Thread {
	
	public static final int STATE_RUNNING = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_LOSE = 3;
	
	public static final float GAME_SPEED = 1;
	
	public static final int FRUIT_PRICE = 5;
	
    private SurfaceHolder mSurfaceHolder;
    private TreeView mTreeView;
    private Handler mHandler;
    
    // The thread's state
    private boolean mRun = false;
    
    // The game's state (don't confuse with thread's state
    private int mState;
    
    private boolean mPausedFromDb = false;
    
    private Tree mTree;
    private Bugs mBugs;
    private User mUser;
    private Items mItems;
    private WateringAnimation mWatering;
    
    private Bitmap mBackgroundImage;
    private ColorDrawable mDarknessColor;
    
    private Drawable mCloudsImage1;
    
    private double mCloudOffset1 = 0;
    private double mCloudOffset2;
    
    private int mCanvasWidth;
    private int mCanvasHeight;
    
    private String mDebugStr = "";
    
    private long mTimeNow;
    private long mTimeLast;
    private double mTimeElapsed; // Seconds since last iteration
    private long mTimeElapsedMS;
    
    private long mPausedSince = 0;
    
    public TreeGame(SurfaceHolder surfaceHolder, TreeView treeView, Handler handler) {
        mSurfaceHolder = surfaceHolder;
        mTreeView = treeView;
        mHandler = handler;
        mTree = new Tree(mTreeView, this); 
        mDarknessColor = new ColorDrawable();
        mBackgroundImage = BitmapFactory.decodeResource(mTreeView.getResources(), R.drawable.sky_clear_day);
        mCloudsImage1 = treeView.getResources().getDrawable(
                R.drawable.clouds);
//        mCloudsImage2 = treeView.getResources().getDrawable(
//                R.drawable.clouds);
        
        mBugs = new Bugs(treeView, this);
        mBugs.startAnimation();
        mWatering = new WateringAnimation(treeView, this);
        
        VirtualTree context = (VirtualTree) mTreeView.getContext();
        mUser = new User(context.getDBAdapter());
        mItems = new Items(context.getDBAdapter());
    }

    public void setRunning(boolean run) {
        mRun = run;
    }
    
    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * 
     * @param mode one of the STATE_* constants
     * @param message string to add to screen or null
     */
    public void setState(int mode) {
        /*
         * This method optionally can cause a text message to be displayed
         * to the user when the mode changes. Since the View that actually
         * renders that text is part of the main View hierarchy and not
         * owned by this thread, we can't touch the state of that View.
         * Instead we use a Message + Handler to relay commands to the main
         * thread, which updates the user-text View.
         */
        synchronized (mSurfaceHolder) {
            mState = mode;
            if (mState == STATE_RUNNING) {
            	if (VirtualTree.DEBUG) {
            		clearMessage();
            	}
            	if (mPausedSince > 0) {
        	    	long elapsed = System.currentTimeMillis() - mPausedSince;
        	    	mPausedSince = 0;
        	    	BugEvent bugs = mTree.getBugs(); 
        	    	bugs.setEventStart(bugs.getEventStart() + elapsed);
        	    	bugs.setLastOccurrence(bugs.getLastOccurrence() + elapsed, true);
        	    	StormEvent storm = mTree.getStorm();
        	    	storm.setEventStart(storm.getEventStart() + elapsed);
        	    	storm.setLastOccurrence(storm.getLastOccurrence() + elapsed, true);
            	}
            	clearStatusMessage();
            } else if (mState == STATE_PAUSED){
            	if (VirtualTree.DEBUG) {
//	                Resources res = mTreeView.getResources();
//	                CharSequence str = "Not Running";
//	                setMessage(str);
            		clearMessage();
            	}
            	setStatusMessage("Paused");
            	mPausedSince = mTimeNow;
            }
        }
    }
    
    public int getGameState() {
    	return mState;
    }
    
    public void addDebugMessage(String str) {
    	mDebugStr += str;
    }
    
    public void setMessage(CharSequence str) {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", TreeView.MESSAGE_TYPE_DEBUG);
        b.putString("text", str.toString());
        b.putInt("viz", View.VISIBLE);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    public void clearMessage() {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", TreeView.MESSAGE_TYPE_DEBUG);
        b.putString("text", "");
        b.putInt("viz", View.INVISIBLE);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    public void setStatusMessage(CharSequence str) {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", TreeView.MESSAGE_TYPE_STATUS);
        b.putString("text", str.toString());
        b.putInt("viz", View.VISIBLE);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    public void clearStatusMessage() {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", TreeView.MESSAGE_TYPE_STATUS);
        b.putString("text", "");
        b.putInt("viz", View.INVISIBLE);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    public void setToastMessage(CharSequence str) {
        Message msg = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("message_type", TreeView.MESSAGE_TYPE_TOAST);
        b.putString("text", str.toString());
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
    
    public void setMoneyMessage(int money) {
    	if (!VirtualTree.DEBUG) {
    		setMessage(" $" + Integer.toString(money));
    	}
    }

    @Override
    public void start() 
    {
    	mTimeNow = System.currentTimeMillis();
    	mTimeLast = mTimeNow;
    	mTimeElapsed = 0;
    	
    	if (!mPausedFromDb) {
    		setState(STATE_RUNNING);
    	}
    	else {
    		setState(STATE_PAUSED);
    		mPausedSince = mTimeNow;
    	}
    	
    	if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "Game started");
    	}
    	super.start();
    }
    
    public void init() {
    	synchronized(mSurfaceHolder){
    		if (!mTreeView.isLoaded()) {
	    		mTree.init();
	    		mUser.init();
	    		mItems.init();
	    		
	    		
	    		setToastMessage(mTreeView.getResources().getText(R.string.toast_intro));
	    		setToastMessage(mTreeView.getResources().getText(R.string.toast_intro_2));
    		}
    		if (VirtualTree.LOGGING) {
        		Log.d(VirtualTree.LOG_TAG, "Game initialized");
        	}
    		setMoneyMessage(mUser.getMoney());
    		
    		
    	}
    }
    
    public void restart() {
    	synchronized(mSurfaceHolder){
			mTree.init();
			mUser.init();
			mItems.init();
			
			mPausedSince = 0;
			
			mTree.getFruitManager().init();
			VirtualTree c = (VirtualTree)mTreeView.getContext();
			DBAdapter db =c.getDBAdapter();
			// Save restarted data to db
			db.open();
			mTree.saveToDb(db);
			db.close();
			setToastMessage(mTreeView.getResources().getText(R.string.toast_intro));
			mTimeLast = System.currentTimeMillis();
			setMoneyMessage(mUser.getMoney());
			
    	}
    }

    @Override
    public void run() {
        Canvas c;
        while (mRun) {
            c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
					if (c != null) {
						updatePhysics();
						onDraw(c);
					}
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
    
//    public void setOrientation(float[] values)
//    {
//      	mOrientationValues = (float[])values.clone();
//    }
    
    public void updatePhysics()
    {
    	mTimeNow = System.currentTimeMillis();
    	
    	// if last time is in the future, get out!
    	if (mTimeLast > mTimeNow) return;
        
    	if (mState != STATE_PAUSED){ 
	        mTimeElapsed = (mTimeNow - mTimeLast) / 1000.0;
	        mTimeElapsedMS = mTimeNow - mTimeLast;
	        
			mDarknessColor.setBounds(0, 0, getCanvasWidth(), getCanvasHeight());
	//		mDarknessColor.setAlpha(50);
			if (mCloudOffset1 > getCanvasWidth())
			{
				mCloudOffset1 = mCloudOffset2;
			}
			mCloudOffset1 += (10.0*mTimeElapsed);
			mCloudOffset2 = mCloudOffset1 - mCloudsImage1.getIntrinsicWidth();
			
			
	    	// Update All the other objects
	    	mTree.updatePhysics();
	    	mBugs.updatePhysics();
	    	mWatering.updatePhysics();
	    	
	    	if (VirtualTree.DEBUG) {
	    		setMessage((CharSequence)mDebugStr);
	    		mDebugStr ="";
	    	}
	    	
	    	
	    	// Debug
	//        String str = "X: ";
	//        str += Float.toString(mOrientationValues[SensorManager.DATA_X]);
	//        str += "\n";
	//    	String str = "Y: ";
	//        str += Float.toString(mOrientationValues[SensorManager.DATA_Y]);
	//        str += "\n";
	//        str += "Z: ";
	//        str += Float.toString(mOrientationValues[SensorManager.DATA_Z]);
	//    	setMessage((CharSequence)str);
	    	
    	}
    	// We're done now, so update the previous time
    	mTimeLast = mTimeNow;
    }
    

    
    public void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(mBackgroundImage, 0, 0, null);
        mCloudsImage1.setBounds((int)mCloudOffset1, 0, mCloudsImage1.getIntrinsicWidth() + (int)mCloudOffset1, mCloudsImage1.getIntrinsicHeight());
        mCloudsImage1.draw(canvas);
        mCloudsImage1.setBounds((int)mCloudOffset2, 0, mCloudsImage1.getIntrinsicWidth() + (int)mCloudOffset2, mCloudsImage1.getIntrinsicHeight());
        mCloudsImage1.draw(canvas);
		mTree.onDraw(canvas);
		mBugs.onDraw(canvas);
		mWatering.onDraw(canvas);
    }

	public void setSurfaceSize(int width, int height) {
		mCanvasWidth = width;
		mCanvasHeight = height;
		
        mBackgroundImage = mBackgroundImage.createScaledBitmap(mBackgroundImage, width, height, true);
        mWatering.setCanvasHeight(height);
	}
	
	public int getCanvasWidth()
	{
		return mCanvasWidth;
	}
	
	public int getCanvasHeight()
	{
		return mCanvasHeight;
	}
	
	public long getTimeNow(){
		return mTimeNow;
	}
	public double getTimeElapsed(){
		return mTimeElapsed;
	}
	public long getTimeElapsedMS(){
		return mTimeElapsedMS;
	}
	
	public Tree getTree() {
		return mTree;
	}
	
	public void waterTree(int waterValue) {
		mTree.water(waterValue);
		if (waterValue == Tree.WATER_VALUE_CAN) {
			mWatering.startAnimation();
		}
	}
	
    /**
     * Dump game state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     * 
     * @return Bundle with this view's state
     */
//    public Bundle saveState(Bundle map) {
//        synchronized (mSurfaceHolder) {
//            if (map != null) {
//            	mTree.saveState(map);
//            	// If we want to save anything more, do it here.
//            }
//        }
//        return map;
//    }
	
    /**
     * Restores game state from the indicated Bundle. Typically called when
     * the Activity is being restored after having been previously
     * destroyed.
     * 
     * @param savedState Bundle containing the game state
     */
//    public synchronized void restoreState(Bundle savedState) {
//        synchronized (mSurfaceHolder) {
//            mTree.restoreState(savedState);
//        }
//    }
    
    public void restoreFromDb(Cursor c) { 
    	if (c.getInt(c.getColumnIndex(DBAdapter.KEY_PAUSED)) > 0) {
    		mPausedFromDb = true;
    	}
    	mTree.restoreFromDb(c);
    }
    
    public void saveToDb(DBAdapter db) {
    	if (mPausedSince > 0) {
	    	long elapsed = System.currentTimeMillis() - mPausedSince;
	    	mPausedSince = 0;
	    	BugEvent bugs = mTree.getBugs(); 
	    	bugs.setEventStart(bugs.getEventStart() + elapsed);
	    	bugs.setLastOccurrence(bugs.getLastOccurrence() + elapsed);
	    	StormEvent storm = mTree.getStorm(); 
	    	storm.setEventStart(storm.getEventStart() + elapsed);
	    	storm.setLastOccurrence(storm.getLastOccurrence() + elapsed);
    	}
    	mTree.saveToDb(db);
    }
    
    
	public Handler getHandler() {
		return mHandler;
	}
	
	public void useItem(int itemId) {
//		synchronized (mSurfaceHolder) {
			mItems.decrementItemCount(itemId);
			mTree.useItem(itemId);
//		}
	}
	
	public int getUserMoney() {
//		synchronized (mSurfaceHolder) {
			return mUser.getMoney();
//		}
	}

	public void buyItem(int item) {
//		synchronized (mSurfaceHolder) {
			int money = mUser.incrementMoney(-Items.mItemPrices[item]);
			setMoneyMessage(money);
			mItems.incrementItemCount(item);
//		}
	}
	
	public int getItemCount(int item) {
//		synchronized (mSurfaceHolder) {
			return mItems.getItemCount(item);
//		}
	}
	
	public void onTouch(float x, float y) {
//		synchronized (mSurfaceHolder) {
			if (VirtualTree.LOGGING) {
				Log.d(VirtualTree.LOG_TAG, "Touch at (" + Float.toString(x) + ", " + Float.toString(y) + ")");
			}
			if (mState == STATE_RUNNING) {
				FruitManager fm = mTree.getFruitManager();
				if (fm.pickFruitOrNot((int)x, (int)y)) {
					int money = mUser.incrementMoney(FRUIT_PRICE);
					setMoneyMessage(money);
				}
			}
//		}
	}
}
