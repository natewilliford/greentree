package com.pseudoblue.greentree;

public class User {
	
	public static final int INIT_MONEY = 15;
	
	private DBAdapter mDb;
	
	public User(DBAdapter db) {
		mDb = db;
	}
	
	public void init() {
		init(mDb);
	}
	
	public int getMoney() {
		return getMoney(mDb);
	}
	
	public boolean setMoney(int money) {
		return setMoney(mDb, money);
	}
	
	public int incrementMoney(int amount) {
		return incrementMoney(mDb, amount);
	}
	
	public boolean hasEnough(int amount) {
		return hasEnough(mDb, amount);
	}
	
	// Statics
	
	public static void init(DBAdapter db) {
		setMoney(db, INIT_MONEY);
	}
	
	public static int getMoney(DBAdapter db) {
		db.open();
		int result = db.userGetMoney();
		db.close();
		return result;
	}
	
	public static boolean setMoney(DBAdapter db, int money) {
		db.open();
		boolean result = db.userSetMoney(money);
		db.close();
		return result;
	}
	
	public static int incrementMoney(DBAdapter db, int amount) {
		int money = getMoney(db);
		money += amount;
		setMoney(db, money);
		return money;
	}
	
	public static boolean hasEnough(DBAdapter db, int amount) {
		int money = getMoney(db);
		return (money >= amount);
	}
	
}
