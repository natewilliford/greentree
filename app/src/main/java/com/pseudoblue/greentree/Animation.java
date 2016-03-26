package com.pseudoblue.greentree;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public abstract class Animation {

	TreeView mTreeView;
	TreeGame mTreeGame;
	
	protected boolean mIsLoop = false;
	
	protected int mFrameInterval = 50;
	
	protected List<Drawable> mFrames = new ArrayList<Drawable>();
	protected int mIndex;
	
	protected int mOffsetLeft;
	protected int mOffsetTop;
	
	protected long mLastTick;
	
	protected boolean mIsRunning;
	
	Animation (TreeView treeView, TreeGame treeGame) {
		mTreeView = treeView;
		mTreeGame = treeGame;
		
		mIndex = 0;
		mOffsetLeft = 0;
		mOffsetTop = 0;
		mLastTick = -1;
	}
	
	public void init() {

	}
	
	public void setOffset(int left, int top) {
		mOffsetLeft = left;
		mOffsetTop = top;
	}
	
	public void updatePhysics() {
		if (mIsRunning) {
			long now = mTreeGame.getTimeNow();
			if (mLastTick < 0) {
				mLastTick = now;
			}
			
			if (now - mLastTick >= mFrameInterval){
				mIndex++;
				mLastTick = now;
				if (mIndex >= mFrames.size()) {
					if(mIsLoop){
						mIndex = 0;
					}
					else {
						stopAnimation();
					}
				}
			}
		}
	}
	
	public void onDraw(Canvas c) {
		if (mIsRunning) {
			Drawable image = mFrames.get(mIndex); 
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
	
	protected void setLoop(boolean loop) {
		mIsLoop = loop;
	}
	
	protected void setFrameInterval(int interval) {
		mFrameInterval = interval;
	}
	
}
