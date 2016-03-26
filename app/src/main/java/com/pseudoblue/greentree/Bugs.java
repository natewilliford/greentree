package com.pseudoblue.greentree;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class Bugs {

	public static final long FRAME_INTERVAL = 100;
	
	private TreeView mTreeView;
	private TreeGame mTreeGame;
	
	private Drawable[] mFrames = new Drawable[10];
	private int mIndex;
	
	private int mOffsetLeft;
	private int mOffsetTop;
	
	private long mLastTick;
	
	private boolean mIsRunning;
	
	public Bugs(TreeView treeView, TreeGame treeGame) {
		mTreeView = treeView;
		mTreeGame = treeGame;
		
		mFrames[0] = treeView.getResources().getDrawable(R.drawable.bugs0001);
		mFrames[1] = treeView.getResources().getDrawable(R.drawable.bugs0002);
		mFrames[2] = treeView.getResources().getDrawable(R.drawable.bugs0003);
		mFrames[3] = treeView.getResources().getDrawable(R.drawable.bugs0004);
		mFrames[4] = treeView.getResources().getDrawable(R.drawable.bugs0005);
		mFrames[5] = treeView.getResources().getDrawable(R.drawable.bugs0006);
		mFrames[6] = treeView.getResources().getDrawable(R.drawable.bugs0007);
		mFrames[7] = treeView.getResources().getDrawable(R.drawable.bugs0008);
		mFrames[8] = treeView.getResources().getDrawable(R.drawable.bugs0009);
		mFrames[9] = treeView.getResources().getDrawable(R.drawable.bugs0010);
		
		mIndex = 0;
		
		mOffsetLeft = 40;
		mOffsetTop = 180;
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
					mIndex = 0;
				}
			}
		}
	}
	
	public void onDraw(Canvas c) {
		Drawable image = mFrames[mIndex]; 
		image.setBounds(mOffsetLeft, 
					mOffsetTop, 
					mOffsetLeft + image.getIntrinsicWidth(), 
					mOffsetTop + image.getIntrinsicHeight());
//		image.draw(c);
	}
	
	public void startAnimation() {
		mIsRunning = true;
	}
	
	public void stopAnimation() {
		mIsRunning = false;
		mLastTick = -1;
	}
}
