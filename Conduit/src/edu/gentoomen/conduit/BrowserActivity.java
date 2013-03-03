package edu.gentoomen.conduit;

import java.util.ArrayList;

import com.example.google.tv.leftnavbar.LeftNavBar;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.conduit.networking.DiscoveryAgent;
import edu.gentoomen.conduit.networking.Pingable;
import edu.gentoomen.utilities.SmbCredentials;
import edu.gentoomen.utilities.Utils;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ProgressDialog;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.util.Log;

public class BrowserActivity extends FragmentActivity 
	implements FileListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {

	public static final String LOG_TAG = "MainActivity";
	
	private static final int IPADDR_COL = 1;
	private static final int MAC_ADDR_COL = 2;
	private static final int NBT_COL = 3;
	
	private LeftNavBar mLeftNavBar;
	private static SmbCredentials credentials;
	private static Context 		  context;
	private static DiscoveryAgent discoveryAgent;
	private static ProgressDialog loaderCircle;
	private static boolean 		  initialScanCompleted = false;
	
	
	//List of image formats that the app supports. 
	public static final ArrayList<String> supportedImageFormats = new ArrayList<String>();
	static {
		supportedImageFormats.add(".gif");
		supportedImageFormats.add(".png");
		supportedImageFormats.add(".jpg");
		supportedImageFormats.add(".jpeg");
	}
	
    // These are the rows that we will retrieve.
    static final String[] SUMMARY_PROJECTION = new String[] {
        NetworkContentProvider.ID,
        NetworkContentProvider.COL_IP_ADDRESS,        
        NetworkContentProvider.COL_MAC,
        NetworkContentProvider.COL_NBTADR
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
    	
    	/*Add the refresh tab*/
    	bar.addTab(bar.newTab()
			   .setText("Refresh")
			   .setTabListener(new RefreshTabListener())
			   .setIcon(R.drawable.refresh_normal), false);
    	    	    
    	while (data.moveToNext()) {
    		String title;
    		String ip = data.getString(IPADDR_COL);
    		String mac = data.getString(MAC_ADDR_COL);
    		String nbtName = data.getString(NBT_COL);
    		
    		if (nbtName == null)
    			title = ip;
    		else
    			title = nbtName;
    		
    		Log.d(LOG_TAG, "adding mac address " + mac + " to side bar");
    		
	        bar.addTab(bar.newTab()
	    		.setText(title)
	    		.setTag(DiscoveryAgent.macToPingable(mac, ip, nbtName))
	    		.setIcon(R.drawable.tab_d)
	            .setTabListener(new TabListener(fileList, data.getString(1), "one")), false);	        	
    	}
    	
    }

    public void onLoaderReset(Loader<Cursor> loader) {}
	
    private class RefreshTabListener implements ActionBar.TabListener {
    	
    	@Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	
    		ActionBar bar = getLeftNavBar();    		    		    
    		loaderCircle.show();    		
    		((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list)).clearAllFiles();
    		
    		bar.removeAllTabs();
        	
        	/*Add the refresh tab*/
        	bar.addTab(bar.newTab().setText("Refresh").setTabListener(new RefreshTabListener()), false);
        	
        	NetworkContentProvider.clearDatabase();
        	discoveryAgent.cancel(true);
    		discoveryAgent = new DiscoveryAgent(context);
    		discoveryAgent.execute("");
        	
        	onCreateLoader(0, null);
        	
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	onTabReselected(tab, ft);
        }
    }
    
    private class TabListener implements ActionBar.TabListener {
        	
    	private String mTitle;
    	private FileListFragment mFileList;
    	
    	public TabListener(FileListFragment fileList, String title, String path) {    		
    		mTitle = title;
    		mFileList = fileList;
    	}
    	    	    	    
    	@Override /*Function needs refactoring*/
        public void onTabSelected(final Tab tab, FragmentTransaction ft) {    		    
    		
    		try {
    			
    			Log.d(LOG_TAG, "tab selected " + ((Pingable)tab.getTag()).mac + " tab position: " + tab.getPosition());
	        	/*Check to ensure that the tag passed in is a valid IP address*/
	        	if (!((Pingable)tab.getTag()).ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))
	        		return;

    		} catch (Exception e) {
    			Log.d(LOG_TAG, "Invalid tab selected");
    			return;
    		}
    		
        	FileListFragment.selectedServer = ((Pingable)tab.getTag());
        	
        	if (credentials.hostHasAuth(FileListFragment.selectedServer.mac)) {
        		Log.d(LOG_TAG, "host address authentication exists");
        		loginToShare();
        		return;
        	}
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
        	builder.setTitle("Enter Username and Password");
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        	final View view = inflater.inflate(R.layout.dialog_logon, null);
        	builder.setView(view);        	      
        	
        	builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {    				
    				String username = ((EditText)view.findViewById(R.id.username)).getText().toString();
    				String password = ((EditText)view.findViewById(R.id.password)).getText().toString();
    				
    				if(!username.isEmpty() && !password.isEmpty()) {
    					credentials.addCredentials(((Pingable) tab.getTag()).mac, username, password);
    					loginToShare();
    				}
    				
    				return;
    				
    			}
    		});
        	
        	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {				    				
    			}
    		});
        	
        	builder.setNeutralButton("Login as Guest", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {    				
    				
    				credentials.addCredentials(FileListFragment.selectedServer.mac, "guest", "");
    				loginToShare();
    	        	
    			}
    		});
        	
        	builder.create();
        	builder.show();
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	//mFileList.getListView().requestFocus();
        	onTabSelected(tab, ft);
        }
        
        private void loginToShare() {
    		
        	String title;
        	
        	if (FileListFragment.selectedServer.nbtName != null)
        		title = FileListFragment.selectedServer.nbtName;
        	else
        		title = FileListFragment.selectedServer.ip;
        	
    		mFileList.setSelectedType(FileListFragment.TYPE_FOLDER);
        	mFileList.setDevice(title);        	
        	ActionBar bar = getLeftNavBar();
        	bar.setTitle(mTitle);
        	
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
        
        context = this;
        loaderCircle = new ProgressDialog(context);
        loaderCircle.setCancelable(false);
        loaderCircle.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loaderCircle.setMessage("Analyzing your network, please wait");

        
        if (!initialScanCompleted) {         	
            credentials = new SmbCredentials();            	       
	        loaderCircle.show();	        
	        discoveryAgent = new DiscoveryAgent(this);        
	        discoveryAgent.execute("");
	        initialScanCompleted = true;
        }
        
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
    
        switch (item.getItemId()) {        
        case R.id.menu_exit:
        	finish();
        	break;        
        case R.id.device_logout:
        	((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list)).clearAllFiles();
        	credentials.removeCredential(FileListFragment.selectedServer.mac);
        	break;        
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
    	
    	//String toPlay = DeviceNavigator.getPath() + id;     
    	String fileType = Utils.getExtension(id);
    	Intent detailIntent;
    	
    	Log.d(LOG_TAG, fileType);
    	
    	if (supportedImageFormats.contains(fileType)) {
    		detailIntent = new Intent(this, ImageActivity.class);
    		detailIntent.putExtra("currentPath", DeviceNavigator.getPath());
    		detailIntent.putExtra("fileName", id);
    	} else {
    		detailIntent = new Intent(this, PlayerActivity.class);
    	}
    	    	
        startActivity(detailIntent);
        
    }
    
    @Override
    public void onPathChanged(String path) {
    	ActionBar bar = getLeftNavBar();
    	bar.setSubtitle(path);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_activity, menu);
        return true;
    }
    
    public static SmbCredentials getCredentials() {
    	return credentials;
    }
    
    public static ProgressDialog getLoaderCircle() {
    	return loaderCircle;
    }
    
}
