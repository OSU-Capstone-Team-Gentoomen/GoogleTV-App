package edu.gentoomen.conduit;

import com.example.google.tv.leftnavbar.LeftNavBar;

import edu.gentoomen.conduit.networking.DiscoveryAgent;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

public class BrowserActivity extends FragmentActivity 
	implements FileListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "MainActivity";
	private LeftNavBar mLeftNavBar;
	
    // These are the rows that we will retrieve.
    static final String[] SUMMARY_PROJECTION = new String[] {
        NetworkContentProvider.ID,
        NetworkContentProvider.COL_IP_ADDRESS,
    };
    
    static final String[] SELECT_SAMBA = new String[] {
    	NetworkContentProvider.COL_SAMBA + "=1"
    };
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {        
        return new CursorLoader(this, NetworkContentProvider.CONTENT_URI, SUMMARY_PROJECTION, "", null, "");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {        
    	ActionBar bar = getLeftNavBar();
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));
    	bar.removeAllTabs();
    	
    	while (data.moveToNext()) {
	        bar.addTab(bar.newTab()
	    		.setText(data.getString(1))
	    		.setTag(data.getString(1))
	    		.setIcon(R.drawable.tab_d)
	            .setTabListener(new TabListener(fileList, data.getString(1), "one")), false);
    	}
    }

    public void onLoaderReset(Loader<Cursor> loader) {}
	
    private class TabListener implements ActionBar.TabListener {
    
    	private String mPath;
    	private String mTitle;
    	private FileListFragment mFileList;
    	
    	public TabListener(FileListFragment fileList, String title, String path) {
    		mPath = path;
    		mTitle = title;
    		mFileList = fileList;
    	}
    	
    	@Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	Log.d(LOG_TAG, "tab selected " + tab.getTag());
        	mFileList.setPath(tab.getTag().toString());
        	mFileList.setSelectedType(FileListFragment.TYPE_FOLDER);
        	ActionBar bar = getLeftNavBar();
        	bar.setTitle(mTitle);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	mFileList.getListView().requestFocus();
        }    	
    }
    
    public void onBackPressed() {
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));
        if (!fileList.up()) {        
            if (getCurrentFocus() == fileList.getListView()) {
            	finish();
            }
        }
    }
   
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DiscoveryAgent discoveryAgent = new DiscoveryAgent(this);
        discoveryAgent.execute("");
        setContentView(R.layout.browser_activity);
        setupBar();
        
        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    private LeftNavBar getLeftNavBar() {
        if (mLeftNavBar == null) {
            mLeftNavBar = new LeftNavBar(this);
        }
        return mLeftNavBar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_exit) {
        	finish();
        }
        return true;
    }
    
    private void setupBar() {
        ActionBar bar = getLeftNavBar();
        
        bar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND, LeftNavBar.DISPLAY_AUTO_EXPAND);
        bar.setDisplayOptions(LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED, LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED);
        
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));

        bar.addTab(bar.newTab()
    		.setText(R.string.tab_c)
    		.setTag(R.string.tab_c)
    		.setIcon(R.drawable.tab_c)    		
            .setTabListener(new TabListener(fileList, getResources().getString(R.string.tab_c), "local")), true);
    }
    
    @Override
    public void onFileSelected(String id) {
    	Intent detailIntent = new Intent(this, PlayerActivity.class);
        startActivity(detailIntent);
    }

    @Override
    public void onPathChanged(String path) {
    	ActionBar bar = getLeftNavBar();
    	bar.setSubtitle("/" + path);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_activity, menu);
        return true;
    }
}
