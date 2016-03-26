package com.pseudoblue.greentree;

public class Items {
	public static final int ITEM_BUG_SPRAY = 0;
	public static final int ITEM_SPONGE = 1;
	public static final int ITEM_FERTILIZER = 2;
	
	public static final int INIT_ITEM_COUNT = 1;
	
	private DBAdapter mDb;
	
	public static final String[] mItemNames = new String[3];
	public static final int[] mItemPrices = new int[3];
	
	public Items(DBAdapter db) {
		mDb = db;
		
		mItemNames[ITEM_BUG_SPRAY] = "Bug Spray";
		mItemNames[ITEM_SPONGE] = "Sponge";
		mItemNames[ITEM_FERTILIZER] = "Fertilizer";
		
		mItemPrices[ITEM_BUG_SPRAY] = 10;
		mItemPrices[ITEM_SPONGE] = 15;
		mItemPrices[ITEM_FERTILIZER] = 20;
	}
	
	public void init() {
		mDb.open();
		mDb.setItemCount(ITEM_BUG_SPRAY, INIT_ITEM_COUNT);
		mDb.setItemCount(ITEM_SPONGE, INIT_ITEM_COUNT);
		mDb.setItemCount(ITEM_FERTILIZER, INIT_ITEM_COUNT);
		mDb.close();
	}
	
	public boolean incrementItemCount(int item) {
		mDb.open();
		boolean result = mDb.incrementItemCount(item);
		mDb.close();
		return result;
	}
	
	public boolean decrementItemCount(int item) {
		mDb.open();
		boolean result = mDb.decrementItemCount(item);
		mDb.close();
		return result;
	}
	
	public int getItemCount(int item){
		mDb.open();
		int result = mDb.getItemCount(item);
		mDb.close();
		return result;
	}
	
	public String getItemName(int item){
		return mItemNames[item];
	}
	
	public int getItemPrice(int item){
		return mItemPrices[item];
	}
	
	public static String[] getDisplayNames() {
		String[] dNames = new String[mItemNames.length];
		for (int i=0; i<mItemNames.length; i++) {
			dNames[i] = "$" + Integer.toString(mItemPrices[i]) + " - " + mItemNames[i];
		}
		return dNames;
	}
}
