package com.pseudoblue.greentree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.admob.android.ads.AdManager;
//import com.admob.android.ads.AdView;

public class VirtualTree extends Activity {
	
	public static final boolean DEBUG = false;
	public static final boolean LOGGING = true;
	public static final boolean TEST_ADS = false;
	
	public static final String PACKAGE_NAME = "com.pseudoblue.greentree";
	public static final String KEY_PACKAGE_NAME = "com.pseudoblue.greentreekey";

	public static final String LOG_TAG = "GreenTree - PsBl";
	
	private static final int MENU_WATER_TREE = 1;
	private static final int MENU_BUY_ITEM = 2;
	private static final int MENU_USE_ITEM = 3;
	private static final int MENU_RESTART = 4;
	private static final int MENU_ABOUT = 6;
	private static final int MENU_PAUSE = 7;
	private static final int MENU_RESUME = 8;
	private static final int MENU_TRIGGER_STORM = 9;
	private static final int MENU_TRIGGER_BUGS = 10;
	private static final int MENU_DIFFICULTY = 11;
	private static final int MENU_BUY = 12;
	
	public static final int DIALOG_INTRO = 2;
	public static final int DIALOG_GAME_OVER = 3;
	public static final int DIALOG_ABOUT = 4;
	public static final int DIALOG_RESTART_CONFIRM = 5;
	public static final int DIALOG_USE_ITEM_BUG_SPRAY = 6;
	public static final int DIALOG_USE_ITEM_FERTILIZER = 7;
	public static final int DIALOG_USE_ITEM_SPONGE = 8;
	public static final int DIALOG_BUY_ITEM = 9;
	public static final int DIALOG_USE_ITEM = 10;
	public static final int DIALOG_PAUSE_BLOCK = 11;
	public static final int DIALOG_DIFFICULTY = 12;
	public static final int DIALOG_BUY = 13;
	
	private TreeView mTreeView;
	private DBAdapter mDb;
	
	private boolean mRefreshMenu = true;
	
	NotificationManager mNotificationManager;
	private boolean mIsKeyPackageInstalled = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tree_layout);
        
        mTreeView = (TreeView) findViewById(R.id.tree);
//        AdView adView = (AdView)findViewById(R.id.ad);
        // give the LunarView a handle to the TextView used for messages
        mTreeView.setTextView((TextView) findViewById(R.id.text));
        mTreeView.setStatusTextView((TextView) findViewById(R.id.status));
        mDb = new DBAdapter(this);
        
        if (savedInstanceState == null) {
        	if (LOGGING) {
        		Log.d(LOG_TAG, "SIS is null");
        	}
        } else {
        	if (LOGGING) {
        		Log.d(LOG_TAG, "SIS is nonnull");
        	}
        }
//        if (TEST_ADS){
//        	AdManager.setInTestMode(true);
//        }
        
        
//    	PackageManager pm = getPackageManager(); 
//		if (pm.checkSignatures(KEY_PACKAGE_NAME, PACKAGE_NAME) == PackageManager.SIGNATURE_MATCH) {
//			mIsKeyPackageInstalled = true;
//			Log.i(LOG_TAG, "Key package IS installed.");
//		}
//		else {
//			Log.i(LOG_TAG, "Key package NOT installed.");
//		}
        
//        if (TEST_ADS){
//        	AdManager.setInTestMode(true);
//        }
//        
//        if (mIsKeyPackageInstalled) {
//        	AdManager.setInTestMode(true);
//        	adView.setVisibility(View.GONE);
//        }
    }
    
    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
    	if (LOGGING) {
    		Log.d(LOG_TAG, "SIS called");
    	}
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	if (LOGGING) {
    		Log.d(LOG_TAG, "RIS called");
    	}
    	super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	if (mRefreshMenu) {
    		mRefreshMenu = false;
	        menu.clear();
	        TreeGame game = mTreeView.getGame();
	        
	        menu.add(0, MENU_WATER_TREE, 0, R.string.menu_water_tree);
	        menu.add(0, MENU_USE_ITEM, 1, "Use Item");
	        menu.add(0, MENU_BUY_ITEM, 2, "Buy Item");
	        
	        if (game.getGameState() == TreeGame.STATE_PAUSED) {
	        	menu.add(0, MENU_RESUME, 3, "Resume");
	        }
	        else {
	        	menu.add(0, MENU_PAUSE, 3, "Pause");
	        }
	        menu.add(0, MENU_ABOUT, 4, "Help");
	        menu.add(0, MENU_RESTART, 5, "Restart Game");
	        menu.add(0, MENU_DIFFICULTY, 6, "Difficulty");
//	        menu.add(0, MENU_BUY, 7, "No-Ads Key");
//	        if (DEBUG) {
//		        menu.add(0, MENU_TRIGGER_STORM, 8, "Storm");
//		        menu.add(0, MENU_TRIGGER_BUGS, 9, "Bugs");
//	        }
	        
    	}
        return true;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	TreeGame game = mTreeView.getGame();
        switch (item.getItemId()) {
            case MENU_WATER_TREE:
            	if (game.getGameState() == TreeGame.STATE_PAUSED) {
            		showDialog(DIALOG_PAUSE_BLOCK);
            	}
            	else {
            		mTreeView.waterTree(Tree.WATER_VALUE_CAN);
            	}
                return true;
            case MENU_BUY_ITEM:
            	showDialog(DIALOG_BUY_ITEM);
            	return true;
            case MENU_USE_ITEM:
            	if (game.getGameState() == TreeGame.STATE_PAUSED) {
            		showDialog(DIALOG_PAUSE_BLOCK);
            	}
            	else {
            		showDialog(DIALOG_USE_ITEM);
            	}
            	return true;
            case MENU_PAUSE:
            	mTreeView.getGame().setState(TreeGame.STATE_PAUSED);
            	mRefreshMenu = true;
            	return true;
            case MENU_RESUME:
            	mTreeView.getGame().setState(TreeGame.STATE_RUNNING);
            	mRefreshMenu = true;
            	return true;
            case MENU_RESTART:
            	showDialog(DIALOG_RESTART_CONFIRM);
            	return true;
            case MENU_ABOUT:
            	showDialog(DIALOG_ABOUT);
            	return true;
            case MENU_TRIGGER_STORM:
            	mTreeView.getGame().getTree().triggerStorm();
            	return true;
            case MENU_TRIGGER_BUGS:
            	mTreeView.getGame().getTree().triggerBugs();
            	return true;
            case MENU_DIFFICULTY:
            	showDialog(DIALOG_DIFFICULTY);
            	return true;
            case MENU_BUY:
            	showDialog(DIALOG_BUY);
            	return true;
            default:
            	return false;
        }
    }
    
	@Override
	protected void onPause() {
		
    	if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "VirtualTree noPause called");
    	}

		super.onPause();
	}
	
	@Override
	protected void onResume() {
		if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "VirtualTree noResume called");
    	}
		super.onResume();
	}
	
	public void toast(CharSequence text, int duration) {
		Context context = getApplicationContext();
		
		Toast toast = Toast.makeText(context, text, duration);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case DIALOG_INTRO:
			builder.setMessage(mTreeView.getResources().getText(R.string.dialog_intro))
			       .setCancelable(false)
			       .setPositiveButton(mTreeView.getResources().getText(R.string.dialog_start), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			           }
			       });
			return builder.create();
		case DIALOG_PAUSE_BLOCK:
			builder.setMessage(mTreeView.getResources().getText(R.string.dialog_pause_block))
			       .setCancelable(false)
			       .setPositiveButton(mTreeView.getResources().getText(R.string.dialog_close), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			           }
			       });
			return builder.create();
		case DIALOG_ABOUT:

			Dialog alert = new Dialog(this);

			alert.setContentView(R.layout.about_dialog);
			alert.setTitle("Help");

			TextView introText = (TextView) alert.findViewById(R.id.intro);
			introText.setText("Welcome to Green Tree! Treat your sprout well by watering it regularly and dealing with any problems that arise and watch it grow to a beautiful tree. You will be rewarded with fruit that you can sell to buy tools and supplies.");
			
			ImageView image = (ImageView) alert.findViewById(R.id.image_health);
			image.setImageResource(R.drawable.bar_leaf);
			
			TextView textHealth = (TextView) alert.findViewById(R.id.text_health);
			textHealth.setText("Your tree's health is shown with the bar to the far left under the leaf symbol.");
			
			ImageView imageDrop = (ImageView) alert.findViewById(R.id.image_water);
			imageDrop.setImageResource(R.drawable.bar_drop);
			
			TextView textWater = (TextView) alert.findViewById(R.id.text_water);
			textWater.setText("The water saturation of the soil is shown under the drop icon. Be sure not to over water your tree as this will hurt your health some over time.");
			
			TextView textItems = (TextView) alert.findViewById(R.id.items);
			textItems.setText("Items can be bought with money gained from fruit that you have picked." +
					"\n\nThe Bug Spray (Environmentally friendly of course) is used to get rid of bugs that swarm your tree and slowly bring down your health" +
					"\n\nThe Sponge is usefull when your tree is over watered, soaking up excess water and keeping your tree from drowning after a rain storm." +
					"\n\nFertilizer gives your tree a boost of health and growth. You should keep this around in case your tree's health becomes dangerously low.");
			
			TextView textabout = (TextView) alert.findViewById(R.id.about);
			textabout.setText("Green Tree was made by PseudoBlue (c) 2010.");
			
			return alert;
		case DIALOG_BUY:

			Dialog buyAlert = new Dialog(this);

			buyAlert.setContentView(R.layout.buy_dialog);
			buyAlert.setTitle("Buy");
			
			Button b = (Button)buyAlert.findViewById(R.id.button);
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + KEY_PACKAGE_NAME));
					startActivity(i);
				}
			});

			return buyAlert;
		case DIALOG_GAME_OVER:
			builder.setMessage(mTreeView.getResources().getText(R.string.dialog_game_over))
		       .setCancelable(false)
		       .setPositiveButton(mTreeView.getResources().getText(R.string.dialog_restart), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   mTreeView.restartGame();
		               dialog.cancel();
		           }
		       });
//		       .setNegativeButton(mTreeView.getResources().getText(R.string.dialog_exit), new DialogInterface.OnClickListener() {
//		           public void onClick(DialogInterface dialog, int id) {
//		        	   finish();
//		           }
//		       });
			return builder.create();
		case DIALOG_RESTART_CONFIRM:
			builder.setMessage(mTreeView.getResources().getText(R.string.dialog_restart_confirm))
		       .setCancelable(false)
		       .setPositiveButton(mTreeView.getResources().getText(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   mRefreshMenu = true;
		        	   mTreeView.restartGame();
		        	   dialog.cancel();
		           }
		       })
		       .setNegativeButton(mTreeView.getResources().getText(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
		       });
			return builder.create();
		case DIALOG_BUY_ITEM:
			final CharSequence[] items = Items.getDisplayNames();

			builder = new AlertDialog.Builder(this);
			builder.setTitle(mTreeView.getResources().getText(R.string.dialog_buy_item_title) + " - You have $" + Integer.toString(User.getMoney(mDb)));
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					int money = User.getMoney(mDb);
					if (money < Items.mItemPrices[item]){
						Toast toast = Toast.makeText(getApplicationContext(), mTreeView.getResources().getText(R.string.not_enough), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.TOP, 0, 0);
						toast.show();
					}
					else {
						mTreeView.buyItem(item);
						Toast toast = Toast.makeText(getApplicationContext(), mTreeView.getResources().getText(R.string.bought_item) + " " +Items.mItemNames[item], Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.TOP, 0, 0);
						toast.show();
					}
					// Don't cache
					unCacheDialogs();
			    }
			});
			return builder.create();
		case DIALOG_USE_ITEM:
			final CharSequence[] items2 = Items.mItemNames.clone();
			
			for(int i=0; i<items2.length; i++) {
				items2[i] = items2[i]+ " ("+ Integer.toString(mTreeView.getItemCount(i))+")";
			}

			builder = new AlertDialog.Builder(this);
			builder.setTitle(mTreeView.getResources().getText(R.string.dialog_use_item_title));
			builder.setItems(items2, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (mTreeView.getItemCount(item) <= 0) {
						Toast toast = Toast.makeText(getApplicationContext(), "You do not have any of this item. You must buy items before using them.", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.TOP, 0, 0);
						toast.show();
					}
					else {
						mTreeView.useItem(item);
						Toast toast = Toast.makeText(getApplicationContext(), "You used "+Items.mItemNames[item], Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.TOP, 0, 0);
						toast.show();
					}
					// Don't cache
					unCacheDialogs();
			    }
			});
			return builder.create();
			
		case DIALOG_DIFFICULTY:
			final CharSequence[] choices = new CharSequence[3];
			choices[Tree.DIFFICULTY_EASY] = "Easy";
			choices[Tree.DIFFICULTY_NORMAL] = "Normal";
			choices[Tree.DIFFICULTY_HARD] = "Hard";
			
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Set Game Difficulty");
			builder.setSingleChoiceItems(choices, mTreeView.getGame().getTree().getDifficulty(), new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	try {
				    	mTreeView.getGame().getTree().setDifficulty(item);
				    	dialog.cancel();
			    	}
			    	catch (NullPointerException e) {
			    		Log.w(LOG_TAG, "Trying to set difficulty when objects are not available.");
			    	}
			    }
			});
			return  builder.create();
		default:
			return null;
		}
	}

	/**
	 * Some of the dialogs I'm using have dynamic data in them, so I want to un-cache them when
	 * the data changes.
	 * 
	 */
	public void unCacheDialogs() {
		removeDialog(DIALOG_USE_ITEM);
		removeDialog(DIALOG_BUY_ITEM);
	}
	
	protected void onStart() {
		if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "VirtualTree onStart called");
    	}
		super.onStart();
	}
	
	@Override
	protected void onDestroy() {
		if (VirtualTree.LOGGING) {
    		Log.d(VirtualTree.LOG_TAG, "VirtualTree onDestroy called");
    	}
		super.onDestroy();
	}

	public DBAdapter getDBAdapter() {
		return mDb;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}