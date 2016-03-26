package com.pseudoblue.greentree;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

public class TreeView extends SurfaceView implements SurfaceHolder.Callback {
	
	public static final int MESSAGE_TYPE_DEBUG = 1;
	public static final int MESSAGE_TYPE_TOAST = 2;
	public static final int MESSAGE_TYPE_TRIAL_END = 3;
	public static final int MESSAGE_TYPE_GAME_OVER = 4;
	public static final int MESSAGE_TYPE_STATUS = 5;
	
    private TreeGame mGame;
    private TextView mText;
    private TextView mStatusText;
    
    private boolean mLoaded; 
    
    public TreeView(Context context, AttributeSet attributes) {
        super(context, attributes);
        getHolder().addCallback(this);
         
        setFocusable(true); // make sure we get key events
    }
    
    private TreeGame getNewGameThread() {
    	return new TreeGame(getHolder(), this,  new Handler() {
            @Override
            public void handleMessage(Message m) {
            	VirtualTree vtContext = (VirtualTree)getContext();
            	Bundle data = m.getData();
            	switch (data.getInt("message_type")) {
				case MESSAGE_TYPE_DEBUG:
					mText.setVisibility(data.getInt("viz"));
		            mText.setText(data.getString("text"));
					break;
				case MESSAGE_TYPE_STATUS:
		            mStatusText.setVisibility(data.getInt("viz"));
		            mStatusText.setText(data.getString("text"));
					break;
				case MESSAGE_TYPE_TOAST:
					Toast toast = Toast.makeText(vtContext, data.getString("text"), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP, 0, 0);
					toast.show();
					break;
				case MESSAGE_TYPE_GAME_OVER:
					mGame.setState(TreeGame.STATE_PAUSED);
					vtContext.showDialog(VirtualTree.DIALOG_GAME_OVER);
					break;
				default:
					break;
				}
            }
        });
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mGame.setSurfaceSize(width, height);
        Tree tree = mGame.getTree();
        tree.calcOffsets();
        tree.getFruitManager().setMountPoints(true);
        if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "Surface Changed");
    	}
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "Surface Created");
    	}
    	
    	mGame = getNewGameThread();
    	
    	VirtualTree context = (VirtualTree) getContext();
    	DBAdapter db = context.getDBAdapter();
    	
		db.open();
        Cursor cursor = db.getAllTrees();
        mLoaded = true;
        if (cursor.getCount() == 0) {
        	db.insertTree(
        			Tree.INIT_WATER, 
        			Tree.INIT_HEALTH, 
        			Tree.INIT_GROWTH, 
        			Tree.INIT_AGE, 
        			0, 0, 0, 0, 0, 0, false, 1);
        	// Try again
        	cursor = db.getAllTrees();
        	mLoaded = false;
        }
        cursor.moveToFirst();
        mGame.restoreFromDb(cursor);
        cursor.close();
        db.close();
        
        mGame.init();
    	mGame.setRunning(true);
    	mGame.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	if (VirtualTree.LOGGING) {
    		Log.d("PseudoBlue", "Surface Destroyed");
    	}
        boolean retry = true;
    	VirtualTree context = (VirtualTree) getContext();
    	DBAdapter db = context.getDBAdapter();
		db.open();
        mGame.saveToDb(db);
        db.close();
        
        mGame.setRunning(false);
//        mSensorManager.unregisterListener(mSensorListener);
        while (retry) {
            try {
                mGame.join();
                retry = false;
                mGame = null;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }
    
    public boolean isLoaded() {
    	return mLoaded;
    }

	public void setTextView(TextView textView) {
		mText = textView;
	}
	
	public void setStatusTextView(TextView textView) {
		mStatusText = textView;
	}

	public TreeGame getGame() {
		return mGame;
	}
	
	public void toast(CharSequence text) {
		int duration = Toast.LENGTH_SHORT;
		toast(text, duration);
	}
	
	public void toast(CharSequence text, int duration) {
		VirtualTree context = (VirtualTree) getContext();
//		toast.setGravity(Gravity.TOP, 0, 0);
//		context.toast(text, duration);
		
		
		
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}

	public void waterTree(int waterValue) {
		if (mGame != null) {
			mGame.waterTree(waterValue);
		}
	}

	public void restartGame() {
		if (mGame != null) {
			mGame.restart();
	 	    mGame.setState(TreeGame.STATE_RUNNING);
		}
	}
	
	public void useItem(int itemId) {
		mGame.useItem(itemId);
	}
	
	public int getUserMoney() {
		return mGame.getUserMoney();
	}

	public void buyItem(int item) {
		mGame.buyItem(item);
	}
	public int getItemCount(int item) {
		try {
			return mGame.getItemCount(item);
		}
		catch (NullPointerException e) {
			Log.w(VirtualTree.LOG_TAG, "Trying to get item count when game does not exist... returning 0");
			return 0;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN: 
			// For some reason wouldn't work without catching the down action as well.
			mGame.onTouch(event.getX(), event.getY());
			VirtualTree vt = (VirtualTree) getContext();
			vt.unCacheDialogs();
			return true;
		default:
			return false;
		}
	}
}
