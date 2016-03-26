package com.pseudoblue.greentree;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class WateringAnimation {

	public static final long FRAME_INTERVAL = 50;
	
	private TreeView mTreeView;
	private TreeGame mTreeGame;
	
	private Drawable[] mFrames = new Drawable[14];
	private int mIndex;
	
	private int mOffsetLeft;
	private int mOffsetTop;
	
	private long mLastTick;
	
	private boolean mIsRunning;
	
	public WateringAnimation(TreeView treeView, TreeGame treeGame) {
		mTreeView = treeView;
		mTreeGame = treeGame;
		
		mFrames[0] = treeView.getResources().getDrawable(R.drawable.watering01);
		mFrames[1] = treeView.getResources().getDrawable(R.drawable.watering02);
		mFrames[2] = treeView.getResources().getDrawable(R.drawable.watering03);
		mFrames[3] = treeView.getResources().getDrawable(R.drawable.watering04);
		mFrames[4] = treeView.getResources().getDrawable(R.drawable.watering05);
		mFrames[5] = treeView.getResources().getDrawable(R.drawable.watering06);
		mFrames[6] = treeView.getResources().getDrawable(R.drawable.watering07);
		mFrames[7] = treeView.getResources().getDrawable(R.drawable.watering08);
		mFrames[8] = treeView.getResources().getDrawable(R.drawable.watering09);
		mFrames[9] = treeView.getResources().getDrawable(R.drawable.watering10);
		mFrames[10] = treeView.getResources().getDrawable(R.drawable.watering11);
		mFrames[11] = treeView.getResources().getDrawable(R.drawable.watering12);
		mFrames[12] = treeView.getResources().getDrawable(R.drawable.watering13);
		mFrames[13] = treeView.getResources().getDrawable(R.drawable.watering14);
		
		mIndex = 0;
		
		mOffsetLeft = 120;
//		mOffsetTop = 315;
		setCanvasHeight(455);
		
		mLastTick = -1;
		
		mIsRunning = false;
	}
	
	public void init() {

	}
	
	public void updatePhysics() {
		if (mIsRunning) {
			long now = mTreeGame.getTimeNow();
			if (mLastTick < 0) {
				mLastTick = now;
			}
			
			if (now - mLastTick >= FRAME_INTERVAL){
				mIndex++;
				mLastTick = now;
				if (mIndex >= mFrames.length) {
					stopAnimation();
				}
			}
		}
	}
	
	public void onDraw(Canvas c) {
		if (mIsRunning) {
			Drawable image = mFrames[mIndex]; 
			image.setBounds(mOffsetLeft, 
						mOffsetTop, 
						mOffsetLeft + image.getIntrinsicWidth(), 
						mOffsetTop + image.getIntrinsicHeight());
			image.draw(c);
		}
	}
	
	public void startAnimation() {
		mIsRunning = true;
	}
	
	public void stopAnimation() {
		mIsRunning = false;
		mLastTick = -1;
		mIndex = 0;
	}
	
	public void setCanvasHeight(int ch) {
		int offsetBottom = (int) ((40f/455f)*ch);
		mOffsetTop = ch - (offsetBottom + mFrames[0].getIntrinsicHeight());
	}
}
