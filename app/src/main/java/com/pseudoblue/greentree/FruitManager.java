package com.pseudoblue.greentree;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class FruitManager {
	
	class MountPoint {
		
		class DollarAnimation extends Animation{
			DollarAnimation (TreeView treeView, TreeGame treeGame) {
				super(treeView, treeGame);
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar01));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar02));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar03));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar04));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar05));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar06));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar07));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar08));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar09));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.dollar10));
			}
		}
		
		
		public static final int WIDTH = 24;
		public static final int HEIGHT = 30;

		public static final int TOUCH_PADDING = 15;
		
		// Absolute pos
		private int mX;
		private int mY;
		
		private boolean mHasFruit = false;
		
		private Drawable mFruitImage;
		
		private DollarAnimation mAnimation;
		
		MountPoint(int x, int y, int offsetX, int offsetY) {
			mX = x + offsetX;
			mY = y + offsetY;
			
			mFruitImage = mTreeView.getResources().getDrawable(
	                R.drawable.fruit);
			
			mAnimation = new DollarAnimation (mTreeView, mTreeGame);
			mAnimation.setOffset(mX, mY-HEIGHT-10);
		}

		public void updatePhysics() {
			mAnimation.updatePhysics();
		}
		
		public void onDraw(Canvas c) {
			if (mHasFruit) {
				mFruitImage.setBounds(mX, mY, mX + WIDTH, mY + HEIGHT);
				mFruitImage.draw(c);
			}
			mAnimation.onDraw(c);
		}
		
		public boolean hasFruit() {
			return mHasFruit;
		}
		
		public void mountFruit() {
			mHasFruit = true;
		}
		
		public void pickFruit(boolean showAnimation) {
			mHasFruit = false;
			if (showAnimation) {
				mAnimation.startAnimation();
			}
		}
		
		public boolean intersects(int x, int y) {
			if (x > mX - TOUCH_PADDING
				&& x < (mX + WIDTH + TOUCH_PADDING)
				&& y > (mY - TOUCH_PADDING)
				&& y < (mY + HEIGHT + TOUCH_PADDING)
			){
				return true;
			}
			return false;
		}

	}
	
	private TreeView mTreeView;
	private TreeGame mTreeGame;
	private Tree mTree;
	
	private int mLevel;
	
	private int mFruitNum = 0;
	
	private List<MountPoint> mMountPoints = new ArrayList<MountPoint>();
	
	FruitManager(TreeView treeView, TreeGame treeGame, Tree tree) {
		mTreeView = treeView;
		mTreeGame = treeGame;
		mTree = tree;
	}
	
	public void setMountPoints(boolean override){
		setMountPoints(mLevel, override);
	}
	
	public void setMountPoints(int level, boolean override) {
		if (mLevel != level || override) {
			mMountPoints.clear();
			int offLeft = mTree.getOffsetLeft();
			int offTop = mTree.getOffsetTop();
			// It was easier to do coords relative to tree image.
			switch(level) {
			case Tree.GROWTH_LEVEL_SAPLING: 
				mMountPoints.add(new MountPoint(15, 56, offLeft, offTop));
				mMountPoints.add(new MountPoint(57, 39, offLeft, offTop));
				break;
			case Tree.GROWTH_LEVEL_YOUNG: 
				mMountPoints.add(new MountPoint(36, 165, offLeft, offTop));
				mMountPoints.add(new MountPoint(82, 171, offLeft, offTop));
				mMountPoints.add(new MountPoint(56, 113, offLeft, offTop));
				mMountPoints.add(new MountPoint(86, 70, offLeft, offTop));
				mMountPoints.add(new MountPoint(159, 90, offLeft, offTop));
				mMountPoints.add(new MountPoint(135, 153, offLeft, offTop));
				mMountPoints.add(new MountPoint(183, 159, offLeft, offTop));
				break;
			case Tree.GROWTH_LEVEL_ADULT: 
				mMountPoints.add(new MountPoint(25, 176, offLeft, offTop));
				mMountPoints.add(new MountPoint(74, 182, offLeft, offTop));
				mMountPoints.add(new MountPoint(113, 191, offLeft, offTop));
				mMountPoints.add(new MountPoint(52, 111, offLeft, offTop));
				mMountPoints.add(new MountPoint(116, 117, offLeft, offTop));
				mMountPoints.add(new MountPoint(107, 36, offLeft, offTop));
				mMountPoints.add(new MountPoint(151, 35, offLeft, offTop));
				mMountPoints.add(new MountPoint(163, 110, offLeft, offTop));
				mMountPoints.add(new MountPoint(206, 101, offLeft, offTop));
				mMountPoints.add(new MountPoint(172, 171, offLeft, offTop));
				mMountPoints.add(new MountPoint(223, 176, offLeft, offTop));
				break;
			default:
				// Just clearing the mount points is fine
				break;
			}
			// We want to keep the same number of fruits, so we don't
			// cheat them when their tree grows.
			remountAllFruit();
			mLevel = level;
		}
	}
	public void updatePhysics() {
		for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
			MountPoint mp = it.next();
			mp.updatePhysics();
		}
	}
	public void onDraw(Canvas c) {
		for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
			MountPoint mp = it.next();
			mp.onDraw(c);
		}
	}
	
	public boolean isFull() {
//		return mFruitNum >= mMountPoints.size();
		for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
			MountPoint mp = it.next();
			if (!mp.hasFruit()) {
				return false;
			}
		}
		return true;
	}
	
	private MountPoint getEmptyMountPoint() {
		return getRandEmptyMountPoint(0);
	}
	
	private MountPoint getRandEmptyMountPoint(int pass) {
		// TODO: randomize
		if (!isFull()){
			
			int index = new Random().nextInt(mMountPoints.size());
			MountPoint mp = mMountPoints.get(index);
			if (!mp.hasFruit()) {
				return mp;
			}
			else {
				if (pass < 10) {
					// Recurse
					return getRandEmptyMountPoint(pass+1);
				}
				else {
					return getNextEmptyMountPoint();
					
				}
			}
		}
		else {
			Log.w(VirtualTree.LOG_TAG, "Failed to get a fruit mount point.");
			return null;
		}
	}
	
	private MountPoint getNextEmptyMountPoint() {
		if (isFull()) {
			return null;
		}
		else {
			for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
				MountPoint mp = it.next();
				if (!mp.hasFruit()) {
					return mp;
				}
			}
			Log.w(VirtualTree.LOG_TAG, "Failed to get a fruit mount point.");
			return null;
		}
	}
	
	public boolean addFruit() {
		if (!isFull()) {
			MountPoint mp = getEmptyMountPoint();
			mp.mountFruit();
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean growFruit() {
		boolean result = addFruit();
		if (result) {
			mFruitNum++;
		}
		return result;
	}
	
	private MountPoint getFruitForCoords(int x, int y) {
		if (!mMountPoints.isEmpty()) {
			for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
				MountPoint mp = it.next();
				if (mp.hasFruit() && mp.intersects(x, y)){
					return mp;
				}
			}
		}
		return null;
	}
	
	public boolean pickFruitOrNot(int x, int y) {
		MountPoint fruit = getFruitForCoords(x, y);
		if (fruit != null) {
			fruit.pickFruit(true);
			mFruitNum--;
			return true;
		}
		return false;
	}
	
	public void clearFruit() {
		for (Iterator<MountPoint> it = mMountPoints.iterator(); it.hasNext();) {
			MountPoint mp = it.next();
			mp.pickFruit(false);
		}
	}
	
	private void remountAllFruit() {
		clearFruit();
		for (int i=0; i<mFruitNum; i++) {
			boolean result = addFruit();
			result = false;
		}
	}
	
	public int getFruitNum(){
		return mFruitNum;
	}
	
	public void init() {
		clearFruit();
		mFruitNum = 0;
	}
}
