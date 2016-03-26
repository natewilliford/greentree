package com.pseudoblue.greentree;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Tree {

	public static final double INIT_HEALTH = 80;
	public static final double INIT_WATER = 80;
	public static final double INIT_GROWTH = 0;
	public static final long INIT_AGE = 0;
	
	public static final int MS_HOUR = 3600000;
	
	public static final int GROWTH_LEVEL_SPROUT = 1;
	public static final int GROWTH_LEVEL_SAPLING = 2;
	public static final int GROWTH_LEVEL_YOUNG = 3;
	public static final int GROWTH_LEVEL_ADULT = 4;
	
	public static final float GROWTH_POINTS_SAPLING = 400;
	public static final float GROWTH_POINTS_YOUNG = 3500;
	public static final float GROWTH_POINTS_ADULT = 10000;
	
	public static final float GROWTH_POINTS_TRIAL_LIMIT = 2000;
//	public static final float GROWTH_POINTS_FRUIT = 300;
	
	public static final int HEALTH_LEVEL_DEAD = 1;
	public static final int HEALTH_LEVEL_DYING = 2;
	public static final int HEALTH_LEVEL_SICK = 3;
	public static final int HEALTH_LEVEL_HEALTHY = 4;
	
	public static final int WATER_VALUE_CAN = 20;
	public static final int TREE_WATER_IDEAL = 100;
	public static final int TREE_WATER_MAX = 150;
//	public static final int TREE_WATER_HEALTH_RANGE = 40;
//	public static final float WATER_EVAPORATION_RATE = (float) 0.002; // per second
//	public static final float HEALTH_CHANGE_RATE = (float) 0.00015; // per second
	public static final float GROWTH_RATE = (float) 0.0004; // (health per second) * growthrate
	public static final int HEALTH_MAX = 100;
	
	public static final int FERTILIZER_HEALTH_POINTS = 30;
	public static final int FERTILIZER_GROWTH_POINTS = 30;
	public static final int SPONGE_WATER_POINTS = 20;
	
//	public static final float STORM_HEALTH_RATE = (float) 0.002;
//	public static final float STORM_WATER_RATE = (float) 0.008;
//	
//	public static final float BUG_HEALTH_RATE = (float) 0.001;
	
	public static final String KEY_WATER = "tree_mTreeWater";
	public static final String KEY_HEALTH = "tree_mHealth";
	public static final String KEY_GROWTH = "tree_mGrowth";
	public static final String KEY_AGE = "tree_mAge";
	
	public static final int TOAST_DELAY = 30000;
	
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_NORMAL = 1;
	public static final int DIFFICULTY_HARD = 2;
	
	private TreeView mTreeView;
	private TreeGame mTreeGame;
	
	private Drawable mTreeImage;
	private Drawable mLeavesImage;
	
	private int mOffsetLeft;
	private int mOffsetTop;
	
	private float mOffsetBottomRatio;
	private float mOffsetCenterRatio;
	
	private int mDifficulty = 1;
	
	private void setBottomRatio(float ratio) {
		mOffsetBottomRatio = ratio;
		calcOffsets();
	}
	
	private void setCenterOffsetRatio(float ratio) {
		mOffsetCenterRatio = ratio;
		calcOffsets();
	}
	
	private int calcOffsetTop() {
		float derp = ((float)mTreeGame.getCanvasHeight() * mOffsetBottomRatio);  
		return (int) ((float)mTreeGame.getCanvasHeight() - derp) - mTreeImage.getIntrinsicHeight();
	}
	
	public void calcOffsets() {
		mOffsetLeft = calcOffsetLeft();
		mOffsetTop = calcOffsetTop();
	}
	
	
	// center it
	private int calcOffsetLeft() {
		int imgWidth = mTreeImage.getIntrinsicWidth();
		int canvasWidth = mTreeGame.getCanvasWidth();
		int centerOffset = (int) (canvasWidth * mOffsetCenterRatio);
		return (int) ((float)canvasWidth/2.0 - (float)imgWidth/2.0) + centerOffset; 
	}
	
	private int mDbId;
	
	private long mAge;
	private double mGrowth;
	private double mHealth;
	private int mGrowthLevel;
	private int mHealthLevel;
	private double mTreeWater;
	
	private double mTreeWaterOld;
	private double mHealthOld;
	
	private Bars mBars;
	private FruitManager mFruitManager;
	private StormEvent mStorm;
	private BugEvent mBugs;
	
	// Toast
	private long mLastToastSick = 0;
	private long mLastToastOverwatered = 0;
	
	private double mLastFruitGrowth = 0;
	
	public Tree(TreeView treeView, TreeGame treeGame) {
		mTreeView = treeView;
		mTreeGame = treeGame;
		
		mTreeImage = treeView.getResources().getDrawable(
                R.drawable.tree_sprout);
		mLeavesImage = treeView.getResources().getDrawable(
                R.drawable.leaves_young_healthy);
		
		mBars = new Bars();
		mFruitManager = new FruitManager(treeView, treeGame, this); 
		mStorm = new StormEvent(treeView, treeGame);
		mBugs = new BugEvent(treeView, treeGame);
		init();
	}
	
	public void init() {
		mAge = INIT_AGE;
		mTreeWater = INIT_WATER;
		mHealth = INIT_HEALTH;
		mGrowth = INIT_GROWTH;
		mGrowthLevel = GROWTH_LEVEL_SPROUT;
		mHealthLevel = HEALTH_LEVEL_HEALTHY;
		mLastFruitGrowth = 0;
		
		updateTreeImage();
		
		mTreeWaterOld = mTreeWater;
		mHealthOld = mHealth;
		
		mStorm.init();
		mBugs.init();
	}
	
	public void water(int waterValue) {
		mTreeWater += waterValue;
		if (mTreeWater > TREE_WATER_MAX) {
			mTreeWater = TREE_WATER_MAX;
		}
		
		if (mTreeWater > TREE_WATER_IDEAL) {
			if ((mTreeGame.getTimeNow() - mLastToastOverwatered) > TOAST_DELAY) {
				mTreeGame.setToastMessage(mTreeView.getResources().getText(R.string.tree_overwatered_message));
				mLastToastOverwatered = mTreeGame.getTimeNow(); 
			}
		}
	}
	
	public void updatePhysics() {
		updatePhysics(mTreeGame.getTimeElapsedMS());
	}
	public void updatePhysics(long elapsed) {
		updateWaterLevels(elapsed);
		updateHealth(elapsed);
		updateGrowth(elapsed);
		updateTreeImage();  // must be before Fruit
		updateFruit();
		mFruitManager.updatePhysics();
		
		if (mTreeGame.getGameState() != TreeGame.STATE_PAUSED) {
			mStorm.updatePhysics(elapsed);
			mBugs.updatePhysics(elapsed);
		}
		
		mAge += elapsed;
		
		mBars.updatePhysics();
		
		if (VirtualTree.DEBUG) {
			String str = "Tree Water: "
	        + Float.toString((float)mTreeWater)
			+ "\nHealth: "
	        + Float.toString((float)mHealth)
	        + "\nGrowth: "
	        + Float.toString((float)mGrowth)
	        + "\nAge: "
	        + Long.toString(mAge)
	        + "\n";
	        
	    	mTreeGame.addDebugMessage(str);
		}
//		else {
//			String str = " ";
//	        str += Integer.toString(getTreeAgeDays());
//	        str += " days old";
//	    	mTreeGame.setMessage((CharSequence)str);
//		}
	}
	
//	private int getTreeAgeDays() {
//		return (int)(mAge/86400000);
//	}
	
	private void updateWaterLevels(long elapsed){
		if (mTreeWater <= 0) {
			mTreeWater = 0;
			return;
		}
		else if (mTreeWater > TREE_WATER_MAX){
			mTreeWater = TREE_WATER_MAX;
		}
		
		// Keep track of old value for health update.
		mTreeWaterOld = mTreeWater; 
		mTreeWater -= (double)(getWaterEvaporationRate() * TreeGame.GAME_SPEED) * ((double)elapsed / 1000.0);  
		
		if (mTreeWater <= 0) {
			mTreeWater = 0;
		}
		
		mBars.setWater(mTreeWater);
	}
	
	public double getAppliedWater(double waterLevel) {
		 if (waterLevel > TREE_WATER_IDEAL) {
			 return (2 * TREE_WATER_IDEAL) - waterLevel;
		 }
		 else {
			 return waterLevel;
		 }
	}
	
	private void updateHealth(long elapsed){
		 // Average old water level and new, so that elapsed time doesn't matter.
		 double pts = (mTreeWater + mTreeWaterOld) / 2;
		 
		 if (pts > TREE_WATER_IDEAL)
		 {
			 pts = (3 * (TREE_WATER_IDEAL - pts)) + getTreeWaterHealthRange();
			 
//			 pts -= (TREE_WATER_IDEAL - TREE_WATER_HEALTH_RANGE/2);
		 }
		 else {
			 pts -= (TREE_WATER_IDEAL - getTreeWaterHealthRange());
		 }
		 
		 // Limit the harm under watering can do.
		 if (pts < -getTreeWaterHealthRange()){
			 pts = -getTreeWaterHealthRange();
		 }
		 
		 // Keep track of old health for growth equation.
		 mHealthOld = mHealth;
		 mHealth += pts * (getHealthChangeRate() * TreeGame.GAME_SPEED * (elapsed/1000.0));
		 
		 mBars.setHealth(mHealth);
		  
		 // Now determine health level
		 if (mHealth <= 0) {
			 mHealth = 0;
			 mHealthLevel = HEALTH_LEVEL_DEAD;
		 }
		 else if (mHealth < 15) {
			 mHealthLevel = HEALTH_LEVEL_DYING;
		 }
		 else if (mHealth < 35) {
			 mHealthLevel = HEALTH_LEVEL_SICK;
		 }
		 else if (mHealth <= HEALTH_MAX) {
			 mHealthLevel = HEALTH_LEVEL_HEALTHY;
		 }
		 else if (mHealth > HEALTH_MAX) {
			 mHealth = HEALTH_MAX;
		 }
		 
		 if ((mHealthLevel == HEALTH_LEVEL_DYING || mHealthLevel == HEALTH_LEVEL_SICK) && mHealthLevel != HEALTH_LEVEL_DEAD) {
			 if ((mTreeGame.getTimeNow() - mLastToastSick) > TOAST_DELAY) {
				 mTreeGame.setToastMessage(mTreeView.getResources().getText(R.string.tree_sick_message));
				 mLastToastSick = mTreeGame.getTimeNow(); 
			 }
		 }
		 else if (mHealthLevel == HEALTH_LEVEL_DEAD) {
			Handler handler = mTreeGame.getHandler();
			Message msg = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("message_type", TreeView.MESSAGE_TYPE_GAME_OVER);
			msg.setData(b);
			handler.sendMessage(msg);
		 }
		 
	}
	
	private void updateGrowth(long elapsed) {
		double pts = (mHealthOld + mHealth) / 2;
		mGrowth += pts * ((double)elapsed / 1000.0) * GROWTH_RATE * TreeGame.GAME_SPEED;
		
	}
	
	private void updateFruit() {
		if (mLastFruitGrowth <= 0) {
			mLastFruitGrowth = mGrowth;
		}
		
		// Fruit grows faster if tree is healthier.
		double dynamicGrowthPts = (double)getGrowthPointsFruit() + (2*(HEALTH_MAX - mHealth));
		
		double diff = mGrowth - mLastFruitGrowth;
		if (diff > dynamicGrowthPts) {
			double doubNum = diff / (double)dynamicGrowthPts;
			// Number of fruit to grow
			int intNum = (int)doubNum;
			int actualNum = 0;
			for (int i=0; i < intNum; i++) {
				if (mFruitManager.growFruit()){
					actualNum++;
				}
			}
			if (actualNum > 0) {
				mTreeGame.setToastMessage("You have grown fruit! Touch the fruit to harvest and sell it.");;
			}
			// Reset remainder into the last grown fruit.
			double remainder = doubNum - (double)intNum;
			mLastFruitGrowth = mGrowth - (remainder * dynamicGrowthPts);
		}
	}
	
	// Only to be used in updateTreeImage
	private int calculateGrowthLevel() {
		// TODO: The rest of the levels
		if (mGrowth < GROWTH_POINTS_SAPLING)
		{
			return GROWTH_LEVEL_SPROUT;
		}
		else if (mGrowth < GROWTH_POINTS_YOUNG) {
			return GROWTH_LEVEL_SAPLING;
		}
		else if (mGrowth < GROWTH_POINTS_ADULT) {
			return GROWTH_LEVEL_YOUNG;
		}
		else {
			return GROWTH_LEVEL_ADULT;
		}
	}
	
	private void updateTreeImage() {
		
		// TODO: MAKE THIS BETTER.
		int newLevel = calculateGrowthLevel();
//		if (newLevel == mGrowthLevel) {
//			return;
//		}
//		else {
			mGrowthLevel = newLevel;
//		}
			
		switch(mGrowthLevel)  {
		case GROWTH_LEVEL_SPROUT:
//			mOffsetLeft = 164;
//			mOffsetTop = 332;
			
			if (mHealthLevel == HEALTH_LEVEL_DYING || mHealthLevel == HEALTH_LEVEL_DEAD) {
				mTreeImage = mTreeView.getResources().getDrawable(
		                R.drawable.tree_sprout_dead);
			}
			else {
				mTreeImage = mTreeView.getResources().getDrawable(
		                R.drawable.tree_sprout);
			}
			mLeavesImage = null;
			setBottomRatio((float) 0.160439);
			setCenterOffsetRatio((float) 0.025);
			break;
		case GROWTH_LEVEL_SAPLING:
//			mOffsetLeft = 114;
//			mOffsetTop = 265;
			
			mTreeImage = mTreeView.getResources().getDrawable(
	                R.drawable.tree_sapling);
			switch(mHealthLevel)
			{
			case HEALTH_LEVEL_HEALTHY:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_sapling_healthy);
				break;
			case HEALTH_LEVEL_SICK:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_sapling_sick);
				break;
			default:
				mLeavesImage = null;
				break;
			}
			setBottomRatio((float) 0.160439);
			setCenterOffsetRatio((float) 0);
			break;
		case GROWTH_LEVEL_YOUNG:
//			mOffsetLeft = 48;
//			mOffsetTop = 153;
			setBottomRatio((float) 0.160439);
			mTreeImage = mTreeView.getResources().getDrawable(
	                R.drawable.tree_young);
			
			switch(mHealthLevel)
			{
			case HEALTH_LEVEL_HEALTHY:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_young_healthy);
				break;
			case HEALTH_LEVEL_SICK:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_young_sick);
				break;
			default:
				mLeavesImage = null;
				break;
			}
			setBottomRatio((float) 0.160439);
			setCenterOffsetRatio((float) 0.034375);
			break;
		case GROWTH_LEVEL_ADULT:
//			mOffsetLeft = 48;
//			mOffsetTop = 153;
			setBottomRatio((float) 0.160439);
			mTreeImage = mTreeView.getResources().getDrawable(
	                R.drawable.tree_adult);
			
			switch(mHealthLevel)
			{
			case HEALTH_LEVEL_HEALTHY:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_adult_healthy);
				break;
			case HEALTH_LEVEL_SICK:
				mLeavesImage = mTreeView.getResources().getDrawable(
		                R.drawable.leaves_adult_sick);
				break;
			default:
				mLeavesImage = null;
				break;
			}
			setBottomRatio(0.1472f);
//			setCenterOffsetRatio(-0.03125f);
			setCenterOffsetRatio(0.00781f);
			break;
		default: 
			break;
		}
		
		mFruitManager.setMountPoints(mGrowthLevel, false);
		
	}
	
	public void onDraw(Canvas c) {
		if (mTreeImage != null) {
			mTreeImage.setBounds(mOffsetLeft, 
					mOffsetTop, 
					mOffsetLeft + mTreeImage.getIntrinsicWidth(), 
					mOffsetTop + mTreeImage.getIntrinsicHeight());
	        mTreeImage.draw(c);
		}
		if (mLeavesImage != null) {
			mLeavesImage.setBounds(mOffsetLeft, 
					mOffsetTop,
					mOffsetLeft + mLeavesImage.getIntrinsicWidth(), 
					mOffsetTop + mLeavesImage.getIntrinsicHeight());
	        mLeavesImage.draw(c);
		}
		
		mFruitManager.onDraw(c);
		mBars.onDraw(c);
		mStorm.onDraw(c);
		mBugs.onDraw(c);
	}
	
    /**
     * Dump game state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     * 
     * @return Bundle with this view's state
     */
//    public Bundle saveState(Bundle map) {
//        if (map != null) {
//            map.putLong(KEY_AGE, Long.valueOf(mAge));
//            map.putDouble(KEY_GROWTH, Double.valueOf(mGrowth));
//            map.putDouble(KEY_HEALTH, Double.valueOf(mHealth));
//            map.putDouble(KEY_WATER, Double.valueOf(mTreeWater));
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
//    	init();
//    	
//    	mAge = savedState.getLong(KEY_AGE);
//    	mGrowth = savedState.getDouble(KEY_GROWTH);
//    	mHealth = savedState.getDouble(KEY_HEALTH);
//    	mTreeWater = savedState.getDouble(KEY_WATER);
//    }

	public void restoreFromDb(Cursor c) {
		mDbId = c.getInt(c.getColumnIndex(DBAdapter.KEY_ROWID));
		mAge = c.getInt(c.getColumnIndex(DBAdapter.KEY_AGE));
		mHealth = c.getDouble(c.getColumnIndex(DBAdapter.KEY_HEALTH));
		mGrowth = c.getDouble(c.getColumnIndexOrThrow(DBAdapter.KEY_GROWTH));
		mTreeWater = c.getDouble(c.getColumnIndex(DBAdapter.KEY_WATER));
		mDifficulty = c.getInt(c.getColumnIndex(DBAdapter.KEY_DIFFICULTY));
		int numFruit = c.getInt(c.getColumnIndex(DBAdapter.KEY_FRUIT_NUM));
		mLastFruitGrowth = c.getDouble(c.getColumnIndex(DBAdapter.KEY_LAST_FRUIT_GROWTH));
		
		long lastMod = c.getLong(c.getColumnIndex(DBAdapter.KEY_LAST_MODIFIED));
		mBugs.setTimeLeft(c.getLong(c.getColumnIndex(DBAdapter.KEY_TIME_LEFT_BUGS)));
		mStorm.setTimeLeft(c.getLong(c.getColumnIndex(DBAdapter.KEY_TIME_LEFT_STORM)));
		long elapsed = System.currentTimeMillis() - lastMod;
		boolean paused = (c.getInt(c.getColumnIndex(DBAdapter.KEY_PAUSED)) > 0);
		if (paused) {
			mBugs.setLastOccurrence(c.getLong(c.getColumnIndex(DBAdapter.KEY_LAST_BUGS)) + elapsed);
			mStorm.setLastOccurrence(c.getLong(c.getColumnIndex(DBAdapter.KEY_LAST_STORM)) + elapsed);
			updatePhysics(0);
		}
		else {
			mBugs.setLastOccurrence(c.getLong(c.getColumnIndex(DBAdapter.KEY_LAST_BUGS)));
			mStorm.setLastOccurrence(c.getLong(c.getColumnIndex(DBAdapter.KEY_LAST_STORM)));
			updatePhysics(elapsed);
		}
		
		// Add the fruit back
		for (int i=0; i<numFruit; i++) {
			mFruitManager.growFruit();
		}
	}

	public void saveToDb(DBAdapter db) {
		boolean paused = (mTreeGame.getGameState() == TreeGame.STATE_PAUSED);
		db.updateTree(mDbId, 
				mTreeWater, 
				mHealth, 
				mGrowth,
				mAge, 
				mFruitManager.getFruitNum(), 
				mLastFruitGrowth, 
				mBugs.getTimeLeft(), 
				mStorm.getTimeLeft(), 
				mBugs.getLastOccurrence(), 
				mStorm.getLastOccurrence(), 
				paused,
				mDifficulty);
	}
	
	public void triggerStorm() {
		mStorm.trigger();
	}
	
	public void triggerBugs() {
		mBugs.trigger();
	}
	
	public void useItem(int itemId) {
		switch(itemId) {
		case Items.ITEM_BUG_SPRAY:
			mBugs.finishEvent();
			break;
		case Items.ITEM_FERTILIZER:
			mHealth += FERTILIZER_HEALTH_POINTS;
			mGrowth += FERTILIZER_GROWTH_POINTS;
			if (mHealth > HEALTH_MAX) {
				mHealth = HEALTH_MAX;
			}
			break;
		case Items.ITEM_SPONGE:
			mTreeWater -= SPONGE_WATER_POINTS;
			if (mTreeWater < 0) {
				mTreeWater = 0;
			}
			break;
		}
	}
	
	public FruitManager getFruitManager() {
		return mFruitManager;
	}
	
	public int getOffsetLeft() {
		return mOffsetLeft;
	}
	
	public int getOffsetTop() {
		return mOffsetTop;
	}
	
	public void setDifficulty(int d) {
		mDifficulty = d;
		Log.i(VirtualTree.LOG_TAG, "Game difficulty set to " + String.valueOf(d));
	}
	
	public int getDifficulty() {
		return mDifficulty;
	}
	
	private int getTreeWaterHealthRange() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 48;
		case(DIFFICULTY_HARD):
			return 39;
		default:
			return 45;
		}
	}
	
	private float getHealthChangeRate() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 0.00007f;
		case(DIFFICULTY_HARD):
			return 0.00015f;
		default:
			return 0.0001f;
		}
	}
	
	private float getStormHealthRate() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 0.0017f;
		case(DIFFICULTY_HARD):
			return 0.0023f;
		default:
			return 0.002f;
		}
	}
	
	private float getStormWaterRate() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 0.007f;
		case(DIFFICULTY_HARD):
			return 0.009f;
		default:
			return 0.007f;
		}
	}
	
	private float getBugHealthRate() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 0.0008f;
		case(DIFFICULTY_HARD):
			return 0.001f;
		default:
			return 0.008f;
		}
	}
	
	private float getWaterEvaporationRate() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 0.002f;
		case(DIFFICULTY_HARD):
			return 0.002f;
		default:
			return 0.002f;
		}
	}
	
//	public static final float GROWTH_POINTS_FRUIT = 300;
	
	private float getGrowthPointsFruit() {
		switch(mDifficulty) {
		case (DIFFICULTY_EASY):
			return 250f;
		case(DIFFICULTY_HARD):
			return 350f;
		default:
			return 300f;
		}
	}

	class Bars {
		
		public static final int OFFSET_TOP = 200;
		public static final int OFFSET_LEFT = 10;
		public static final int SPACING = 5;
		
		private double mHealth;
		private double mWater;
		
		private Drawable mBarBackground;
		private Drawable mBlueBar;
		private Drawable mGreenBar;
		private Drawable mLeafImage;
		private Drawable mDropImage;
		
		Bars() {
			mBarBackground = mTreeView.getResources().getDrawable(
	                R.drawable.bar_empty);
			mBlueBar = mTreeView.getResources().getDrawable(
	                R.drawable.bar_blue);
			mGreenBar = mTreeView.getResources().getDrawable(
	                R.drawable.bar_green);
			mLeafImage = mTreeView.getResources().getDrawable(
	                R.drawable.bar_leaf);
			mDropImage = mTreeView.getResources().getDrawable(
	                R.drawable.bar_drop);
		}
		
		public void setHealth(double health) {
			mHealth = health;
		}
		
		public void setWater(double water) {
			mWater = water;
		}
		
		public void updatePhysics() {
			
		}
		
		public void onDraw(Canvas c) {
			int offsetLeft = OFFSET_LEFT;
			int offsetTop = OFFSET_TOP;
			
			mLeafImage.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mLeafImage.getIntrinsicWidth(), 
					offsetTop + mLeafImage.getIntrinsicHeight());
			mLeafImage.draw(c);
			
			offsetLeft = OFFSET_LEFT + mLeafImage.getIntrinsicWidth() + SPACING;
			mDropImage.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mDropImage.getIntrinsicWidth(), 
					offsetTop + mDropImage.getIntrinsicHeight());
			mDropImage.draw(c);
			
			offsetTop = OFFSET_TOP + mLeafImage.getIntrinsicHeight() + SPACING;
			offsetLeft = OFFSET_LEFT;
			mBarBackground.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mBarBackground.getIntrinsicWidth(), 
					offsetTop + mBarBackground.getIntrinsicHeight());
			mBarBackground.draw(c);
			
			offsetLeft = OFFSET_LEFT + mBarBackground.getIntrinsicWidth() + SPACING;
			mBarBackground.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mBarBackground.getIntrinsicWidth(), 
					offsetTop + mBarBackground.getIntrinsicHeight());
			mBarBackground.draw(c);
			
			// Now for the hard part!

			// Health bar
			float perc = ((float)mHealth / (float)HEALTH_MAX);
			int staticBottom = OFFSET_TOP + mLeafImage.getIntrinsicHeight() + SPACING + mBarBackground.getIntrinsicHeight(); 
			
			offsetTop = OFFSET_TOP + mLeafImage.getIntrinsicHeight() + SPACING;
			offsetTop += (int)(mBarBackground.getIntrinsicHeight() * (1 - perc));
			offsetLeft = OFFSET_LEFT;
			
			if (mHealthLevel == HEALTH_LEVEL_DYING) {
				mGreenBar = mTreeView.getResources().getDrawable(
		                R.drawable.bar_red);
			}
			else if (mHealthLevel == HEALTH_LEVEL_SICK) {
				mGreenBar = mTreeView.getResources().getDrawable(
		                R.drawable.bar_yellow);
			}
			else {
				mGreenBar = mTreeView.getResources().getDrawable(
		                R.drawable.bar_green);
			}
			
			mGreenBar.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mGreenBar.getIntrinsicWidth(), 
					staticBottom);
			mGreenBar.draw(c);
			
			// Water bar
			perc = ((float)mWater / (float)TREE_WATER_IDEAL);

			if (perc > 1)
			{
				if (perc < 1.15) {
					mBlueBar = mTreeView.getResources().getDrawable(
			                R.drawable.bar_yellow);
				}
				else {
					mBlueBar = mTreeView.getResources().getDrawable(
			                R.drawable.bar_red);
				}
				perc = 1;
			}
			else {
				if (mWater < (TREE_WATER_IDEAL - getTreeWaterHealthRange()) / 2) {
					mBlueBar = mTreeView.getResources().getDrawable(
			                R.drawable.bar_red);
				}
				else if (mWater < (TREE_WATER_IDEAL - getTreeWaterHealthRange())) {
					mBlueBar = mTreeView.getResources().getDrawable(
			                R.drawable.bar_yellow);
				}
				else {
					mBlueBar = mTreeView.getResources().getDrawable(
			                R.drawable.bar_blue);
				}
			}
			
			offsetTop = OFFSET_TOP + mLeafImage.getIntrinsicHeight() + SPACING;
			offsetTop += (int)(mBarBackground.getIntrinsicHeight() * (1 - perc));
			offsetLeft = OFFSET_LEFT + mBarBackground.getIntrinsicWidth() + SPACING;;
			
			mBlueBar.setBounds(offsetLeft, 
					offsetTop,
					offsetLeft + mGreenBar.getIntrinsicWidth(), 
					staticBottom);
			mBlueBar.draw(c);
			
		}
	}
	
	class StormEvent extends Event {
		
		class RainAnimation extends Animation {
			RainAnimation (TreeView treeView, TreeGame treeGame) {
				super(treeView, treeGame);
				mFrames.add(treeView.getResources().getDrawable(R.drawable.storm01));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.storm02));
				
				setFrameInterval(200);
				setLoop(true);
			}

			public boolean isRunning() {
				return mIsRunning;
			}
		}
		
		private RainAnimation mRainAnimation;
		
		@Override
		protected int getOccurrenceRangeMin(){return (int) ((8 * MS_HOUR)/TreeGame.GAME_SPEED);};
		@Override
		protected int getOccurrenceRangeMax(){return (int) ((12 * MS_HOUR)/TreeGame.GAME_SPEED);};
		
		@Override
		protected int getEventLengthMin() {return (int) ((1 * MS_HOUR)/TreeGame.GAME_SPEED);} // 1 hr
		@Override
		protected int getEventLengthMax() {return (int) ((2 * MS_HOUR)/TreeGame.GAME_SPEED);} // 2 hr
		
		StormEvent(TreeView treeView, TreeGame treeGame) {
			super(treeView, treeGame);
			mRainAnimation = new RainAnimation(treeView, treeGame);
		}
		
		@Override
		public void updatePhysics(long elapsed) {
			super.updatePhysics(elapsed);
			if (mIsRunning) {
				if (!mRainAnimation.isRunning()) {
					mRainAnimation.startAnimation();
				}
			}
			
			mRainAnimation.updatePhysics();
		}
		
		@Override
		protected void affect(long timePeriod) {
			super.affect(timePeriod);
			
			mHealth -= (double) (getStormHealthRate() * timePeriod * TreeGame.GAME_SPEED) / 1000.0;
			mTreeWater += (double) (getStormWaterRate() * timePeriod * TreeGame.GAME_SPEED) / 1000.0;
			
		}
		
		public void onDraw(Canvas c) {
			mRainAnimation.onDraw(c);
		}
		
		@Override
		public void startEvent(long now) {
			super.startEvent(now);
			mTreeGame.setToastMessage("A rain storm affects your health and water level.");
			mTreeGame.setToastMessage("You may want to keep some Sponges to soak up extra water and Fertilizer to help your plant after a storm.");
			if (VirtualTree.LOGGING) {
        		Log.d(VirtualTree.LOG_TAG, "!!!!Storming!!!!");
        	}
		}
		
		@Override
		public void finishEvent() {
			super.finishEvent();
			mRainAnimation.stopAnimation();
		}
		
		@Override
		public void init() { 
			super.init();
			mRainAnimation.stopAnimation(); 
		}
		
	}
	
	class BugEvent extends Event {
		
		class BugAnimation extends Animation {
			BugAnimation (TreeView treeView, TreeGame treeGame) {
				super(treeView, treeGame);
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0001));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0002));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0003));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0004));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0005));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0006));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0007));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0008));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0009));
				mFrames.add(treeView.getResources().getDrawable(R.drawable.bugs0010));
				
				mOffsetLeft = 35;
				mOffsetTop = 175;
				
				setFrameInterval(50);
				setLoop(true);
			}

			public boolean isRunning() {
				return mIsRunning;
			}
		}
		
		private BugAnimation mBugAnimation;
		
		@Override
		protected int getOccurrenceRangeMin(){return (int) ((6 * MS_HOUR)/TreeGame.GAME_SPEED);}; // 6 hrs
		@Override
		protected int getOccurrenceRangeMax(){return (int) ((12 * MS_HOUR)/TreeGame.GAME_SPEED);}; // 12 hrs

		@Override
		protected int getEventLengthMin() {return (int) ((2 * MS_HOUR)/TreeGame.GAME_SPEED);} // 2 hrs
		@Override
		protected int getEventLengthMax() {return (int) ((4 * MS_HOUR)/TreeGame.GAME_SPEED);} // 4 hrs
		
		BugEvent(TreeView treeView, TreeGame treeGame) {
			super(treeView, treeGame);
			mBugAnimation = new BugAnimation(treeView, treeGame);
		}
		
		@Override
		public void updatePhysics(long elapsed) {
			super.updatePhysics(elapsed);
			if (mIsRunning) {
				if (!mBugAnimation.isRunning()) {
					mBugAnimation.startAnimation();
				}
			}
			mBugAnimation.updatePhysics();
			
//			if (VirtualTree.DEBUG) {
//				String str = "Wait Per: "
//		        + Long.toString(mWaitPeriod)
//				+ "\nEv Length: "
//		        + Long.toString(mEventLength)
//		        + "\nLast: "
//		        + Long.toString(mLastOccurrence)
//		        + "\nStart: "
//		        + Long.toString(mEventStart)
//		        + "\n";
//		        
//		    	mTreeGame.addDebugMessage(str);
//			}
		}
		
		@Override
		protected void affect (long timePeriod) { 
			super.affect(timePeriod);
			mHealth -= (double) (getBugHealthRate() * timePeriod * TreeGame.GAME_SPEED) / 1000.0;
		}
		
		public void onDraw(Canvas c) {
			mBugAnimation.onDraw(c);
		}
		
		@Override
		public void finishEvent() {
			super.finishEvent();
			mBugAnimation.stopAnimation();
		}
		
		@Override
		public void startEvent(long now) {
			super.startEvent(now);
			mTreeGame.setToastMessage("Your tree has attracted bugs! They will stick around for a while hurting your tree unless you use Bug Spray on them.");
        	if (VirtualTree.LOGGING) {
        		Log.d(VirtualTree.LOG_TAG, "!!!!Bugs!!!!");
        	}
		}
		
		public void init() { 
			super.init();
			mBugAnimation.stopAnimation(); 
		}
		
	}
	
	public BugEvent getBugs() {
		return mBugs;
	}
	
	public StormEvent getStorm() {
		return mStorm;
	}
}