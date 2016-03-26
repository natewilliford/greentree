
package com.pseudoblue.greentree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter 
{
    public static final String KEY_ROWID = "_id";
    
    public static final String KEY_WATER = "water";
    public static final String KEY_HEALTH = "health";
    public static final String KEY_GROWTH = "growth";
    public static final String KEY_AGE = "age";
    public static final String KEY_LAST_MODIFIED = "last_modified";
    public static final String KEY_FRUIT_NUM = "fruit_num";
    public static final String KEY_LAST_FRUIT_GROWTH = "last_fruit_growth";
    public static final String KEY_TIME_LEFT_STORM = "time_left_storm";
    public static final String KEY_TIME_LEFT_BUGS = "time_left_bugs";
    public static final String KEY_LAST_BUGS = "last_bugs";
    public static final String KEY_LAST_STORM = "last_storm";
    public static final String KEY_PAUSED = "paused";
    public static final String KEY_DIFFICULTY = "difficulty";
    
    public static final String KEY_ITEM_COUNT = "item_count";
    
    public static final long USER_ID = 1;
    public static final String KEY_USER_MONEY = "money";
    
    private static final String TAG = "PseudoBlue";
    private static final String PREFIX = "DBA -- ";
    
    private static final String DATABASE_NAME = "virtual_tree";
    
    private static final String DATABASE_TABLE_TREE = "tree";
    private static final String DATABASE_TABLE_ITEMS = "items";
    private static final String DATABASE_TABLE_USER = "user";
    
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE_TREE =
        "create table tree (_id integer primary key autoincrement, "
        + "water double not null, " 
        + "health double not null, " 
        + "growth double not null, " 
        + "age integer not null, "
        + "last_modified integer not null," 
        + "fruit_num integer not null,"
        + "last_fruit_growth double not null,"
        + "time_left_bugs integer not null,"
        + "time_left_storm integer not null,"
        + "last_bugs integer not null default 0,"
        + "last_storm integer not null default 0,"
        + "paused integer not null default 0,"
        + "difficulty integer not null default 0);";
    
    private static final String DATABASE_CREATE_ITEMS = 
    	"create table items (_id integer primary key, "
        + "item_count integer not null);";
    
    private static final String DATABASE_CREATE_USER = 
    	"create table user (_id integer primary key, "
        + "money integer not null);";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	if (VirtualTree.LOGGING) {
        		Log.d(TAG, PREFIX+"Creating database");
        	}
            db.execSQL(DATABASE_CREATE_TREE);
            db.execSQL(DATABASE_CREATE_ITEMS);
            db.execSQL(DATABASE_CREATE_USER);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, PREFIX+"Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion);
            switch(oldVersion) {
            case 1:
            	// Shouldn't really get here after the re-release
            	db.execSQL("alter table tree add column last_bugs integer not null default 0");
            	db.execSQL("alter table tree add column last_storm integer not null default 0");
            	db.execSQL("alter table tree add column paused integer not null default 0");
            	db.execSQL("alter table tree add column difficulty integer not null default 0");
            	break;
        	case 2:
        		Log.w(TAG, PREFIX+"Adding paused column");
	        	db.execSQL("alter table tree add column paused integer not null default 0");
	        	Log.w(TAG, PREFIX+"Adding difficulty");
	        	db.execSQL("alter table tree add column difficulty integer not null default 1");
	        	break;
        	case 3:
        		Log.w(TAG, PREFIX+"Adding difficulty");
	        	db.execSQL("alter table tree add column difficulty integer not null default 1");
	        	break;
        	}
        }
    }
    
    //---opens the database---
    public DBAdapter open() throws SQLException 
    {
    	if (VirtualTree.LOGGING) {
    		Log.d(TAG, PREFIX+"Open database");
    	}
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
    	if (VirtualTree.LOGGING) {
    		Log.d(TAG, PREFIX+"Close database");
    	}
        DBHelper.close();
    }
    
    public long insertTree(double water, double health, double growth, long age, int fruitNum, double lastFruitGrowth, long bugsTimeLeft, long stormTimeLeft, long lastBugs, long lastStorm, boolean paused, int difficulty) 
    {
    	long now = System.currentTimeMillis();
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_WATER, water);
        initialValues.put(KEY_HEALTH, health);
        initialValues.put(KEY_GROWTH, growth);
        initialValues.put(KEY_AGE, age);
        initialValues.put(KEY_LAST_MODIFIED, now);
        initialValues.put(KEY_FRUIT_NUM, fruitNum);
        initialValues.put(KEY_LAST_FRUIT_GROWTH, lastFruitGrowth);
        initialValues.put(KEY_TIME_LEFT_BUGS, bugsTimeLeft);
        initialValues.put(KEY_TIME_LEFT_STORM, stormTimeLeft);
        initialValues.put(KEY_LAST_STORM, lastBugs);
        initialValues.put(KEY_LAST_STORM, lastStorm);
        initialValues.put(KEY_PAUSED, (paused ? 1 : 0));
        initialValues.put(KEY_DIFFICULTY, difficulty);
        return db.insert(DATABASE_TABLE_TREE, null, initialValues);
    }

    public Cursor getAllTrees() 
    {
        return db.query(DATABASE_TABLE_TREE, new String[] {
        		KEY_ROWID, 
        		KEY_WATER,
        		KEY_HEALTH,
        		KEY_GROWTH,
        		KEY_AGE,
                KEY_LAST_MODIFIED,
                KEY_FRUIT_NUM,
                KEY_LAST_FRUIT_GROWTH,
                KEY_TIME_LEFT_BUGS,
                KEY_TIME_LEFT_STORM,
                KEY_LAST_BUGS,
                KEY_LAST_STORM,
                KEY_PAUSED,
                KEY_DIFFICULTY}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }

    //---retrieves a particular title---
//    public Cursor getTitle(long rowId) throws SQLException 
//    {
//        Cursor mCursor =
//                db.query(true, DATABASE_TABLE_TREE, new String[] {
//                		KEY_ROWID,
//                		KEY_ISBN, 
//                		KEY_TITLE,
//                		KEY_PUBLISHER
//                		}, 
//                		KEY_ROWID + "=" + rowId, 
//                		null,
//                		null, 
//                		null, 
//                		null, 
//                		null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//    }

    //---updates a title---
    public boolean updateTree(long rowId, 
    		double water, 
    		double health, 
    		double growth, 
    		long age, 
    		int fruitNum, 
    		double lastFruitGrowth, 
    		long timeLeftBugs, 
    		long timeLeftStorm, 
    		long lastBugs, 
    		long lastStorm,
    		boolean paused,
    		int difficulty) 
    {
    	long now = System.currentTimeMillis();
        ContentValues args = new ContentValues();
        args.put(KEY_WATER, water);
        args.put(KEY_HEALTH, health);
        args.put(KEY_GROWTH, growth);
        args.put(KEY_AGE, age);
        args.put(KEY_LAST_MODIFIED, now);
        args.put(KEY_FRUIT_NUM, fruitNum);
        args.put(KEY_LAST_FRUIT_GROWTH, lastFruitGrowth);
        args.put(KEY_TIME_LEFT_BUGS, timeLeftBugs);
        args.put(KEY_TIME_LEFT_STORM, timeLeftStorm);
        args.put(KEY_LAST_BUGS, lastBugs);
        args.put(KEY_LAST_STORM, lastStorm);
        args.put(KEY_PAUSED, (paused ? 1 : 0));
        args.put(KEY_DIFFICULTY, difficulty);

        return db.update(DATABASE_TABLE_TREE, args, 
                         KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    private long insertItem(long id, int count) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_ITEM_COUNT, count);
        return db.insert(DATABASE_TABLE_ITEMS, null, initialValues);
    }
    
    public int getItemCount(int id) {
    	Cursor c = db.query(DATABASE_TABLE_ITEMS, new String[] {
        		KEY_ROWID, 
        		KEY_ITEM_COUNT},
        		KEY_ROWID + "=" + id,
                null, 
                null, 
                null, 
                null);
    	if (c.getCount() == 0) {
    		insertItem(id, 0);
    		c.close();
    		return 0;
    	}
    	else {
    		c.moveToFirst();
    		int count = c.getInt(c.getColumnIndex(KEY_ITEM_COUNT)); 
    		c.close();
    		return count;
    	}
    }
    
    public boolean incrementItemCount(int id){
    	int count = getItemCount(id);
    	count++;
        ContentValues args = new ContentValues();
        args.put(KEY_ITEM_COUNT, count);
        return db.update(DATABASE_TABLE_ITEMS, args, 
                         KEY_ROWID + "=" + id, null) > 0;
    }
    
    public boolean decrementItemCount(int id){
    	int count = getItemCount(id);
    	if (count > 0) {
	    	count--;
	        ContentValues args = new ContentValues();
	        args.put(KEY_ITEM_COUNT, count);
	        return db.update(DATABASE_TABLE_ITEMS, args, 
	                         KEY_ROWID + "=" + id, null) > 0;
    	}
    	else {
    		Log.w(TAG, PREFIX+"Tried to decrement item count with 0 items.");
    		return true;
    	}
    }
    
    public boolean setItemCount(int id, int count) {
    	getItemCount(id); // make sure it's got a row
        ContentValues args = new ContentValues();
        args.put(KEY_ITEM_COUNT, count);
        return db.update(DATABASE_TABLE_ITEMS, args, 
                KEY_ROWID + "=" + id, null) > 0;
    }
    
    private long insertMoney() {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, USER_ID);
        initialValues.put(KEY_USER_MONEY, 0);
        return db.insert(DATABASE_TABLE_USER, null, initialValues);
    }
    
    public int userGetMoney() {
        Cursor c = db.query(DATABASE_TABLE_USER, new String[] {
        		KEY_ROWID, 
        		KEY_USER_MONEY}, 
        		KEY_ROWID + "=" + USER_ID, 
                null, 
                null, 
                null, 
                null);
        if (c.getCount() > 0) {
        	c.moveToFirst();
        	int money = c.getInt(c.getColumnIndex(KEY_USER_MONEY)); 
        	c.close();
        	return money;
        }
        else {
        	insertMoney();
        	c.close();
        	return 0;
        }
        
    }
    
    public boolean userSetMoney(int money) {
    	userGetMoney();
        ContentValues args = new ContentValues();
        args.put(KEY_USER_MONEY, money);
        return db.update(DATABASE_TABLE_USER, args, 
                         KEY_ROWID + "=" + USER_ID, null) > 0;
    }
}

