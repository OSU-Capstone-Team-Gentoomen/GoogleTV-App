package edu.gentoomen.conduit;

import java.util.ArrayList;

import com.example.google.tv.leftnavbar.LeftNavBar;

import contentproviders.NetworkContentProvider;
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

  private void log(String message) {
    Log.d("MainActivity", message);
  }
	
  // TODO should be removed and replaced with constants defined in DeviceContentProvider
	private static final int IPADDR_COL = 1;
	private static final int MAC_ADDR_COL = 2;
	private static final int NBT_COL = 3;
	
  // TODO rename to deviceList or something
	private LeftNavBar mLeftNavBar;

  // TODO remove SmbCredentials
	private static SmbCredentials credentials;

  // TODO remove context, it's just an alias for "this"
	private static Context 		  context;

	private static DiscoveryAgent discoveryAgent;
	private static ProgressDialog scannerProgressBar;
  // TODO move to PlayerActivity if possible
	private static ProgressDialog videoLoadingProgress;
  // TODO probably remove/move to DiscoveryAgent?
	private static boolean 		  initialScanCompleted = false;
	
	
  // TODO move into Utils
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
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {    	
        return new CursorLoader(this, NetworkContentProvider.CONTENT_URI, SUMMARY_PROJECTION, "", null, "");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {        
    	
      // TODO possibly subclass into device list specific ActionBar
    	ActionBar bar = getLeftNavBar();

      // TODO move to class-wide variable
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));

      // TODO abstract concept of resetting device list
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
    		
        // TODO move to content provider
    		if (nbtName == null)
    			title = ip;
    		else
    			title = nbtName;
    		
    		log("adding mac address " + mac + " to side bar");
    		
        // TODO abstract concept of adding a device tab
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
        	
        // TODO will be removed by class-wide variable
    		ActionBar bar = getLeftNavBar();    		    		    

    		scannerProgressBar.show();    		

        // TODO will be removed by class-wide variable
        // TODO make sure to keep the clearAllFiles() call
        // e.g. deviceList.clearAllFiles()
    		((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list)).clearAllFiles();
    		
        // TODO will be removed by DeviceListBar.reset()
    		bar.removeAllTabs();
        	
        // TODO will be removed by DeviceListBar.reset()
        	/*Add the refresh tab*/
        	bar.addTab(bar.newTab().setText("Refresh").setTabListener(new RefreshTabListener()), false);
        	
          // TODO encapsulate refresh logic in DiscoveryAgent
          // e.g. discoveryAgent.refresh();
        	NetworkContentProvider.clearDatabase();
        	discoveryAgent.cancel(true);
    		discoveryAgent = new DiscoveryAgent(context);
    		discoveryAgent.execute("");
        	
        	onCreateLoader(0, null);
        	
        }

        // TODO remove if unnecessary
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	onTabReselected(tab, ft);
        }
    }
    
    private class TabListener implements ActionBar.TabListener {
        	
    	private String mTitle;
      // TODO will be removed by class-wide variable
    	private FileListFragment mFileList;
    	
      // TODO update signature to remove fileList
    	public TabListener(FileListFragment fileList, String title, String path) {    		
    		mTitle = title;
        // TODO will be removed by class-wide variable
    		mFileList = fileList;
    	}
    	    	    	    
    	@Override /*Function needs refactoring*/
        public void onTabSelected(final Tab tab, FragmentTransaction ft) {    		    
    		
        // TODO try to remove this. related to Android auto-selecting invalid tab
    		try {
    			
    			log("tab selected " + ((Pingable)tab.getTag()).mac + " tab position: " + tab.getPosition());
	        	/*Check to ensure that the tag passed in is a valid IP address*/
	        	if (!((Pingable)tab.getTag()).ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))
	        		return;

    		} catch (Exception e) {
    			log("Invalid tab selected");
    			return;
    		}
    		
        // TODO no!
        	FileListFragment.selectedServer = ((Pingable)tab.getTag());
        	
        	if (credentials.hostHasAuth(FileListFragment.selectedServer.mac)) {
        		log("host address authentication exists");
        		loginToShare();
        		return;
        	}

          // TODO UI button for login
        	
          // TODO define UI in XML

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

            try {
                login(username, password)
            } catch (LoginException) {
              // TODO prompt user that password or credentials is incorrect
            }
    				
    				return;
    				
    			}
    		});
        	
        	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int which) {				    				
    			}
    		});
        	
          // TODO remove log in as guest
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

        // TODO remove if necessary
        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
          // TODO remove
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
          // TODO will be removed by class-wide variable
        	ActionBar bar = getLeftNavBar();
        	bar.setTitle(mTitle);
        	
    	}
    }
    
    public void onBackPressed() {
    	log("onBackPressed called");
      // TODO will be removed by class-wide variable
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));
        if (!fileList.up()) {
        	
        	/* confirm if the user really wants to exit */
        	DialogInterface.OnClickListener exitDialog = new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
						case DialogInterface.BUTTON_POSITIVE:
							finish();
						case DialogInterface.BUTTON_NEGATIVE:
							break;
					}
					
				}
			};
			
      // TODO move to XML
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage("Exit Conduit?").setPositiveButton("Yes", exitDialog)
				.setNegativeButton("No", exitDialog).show();
        }
                
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        context = this;
        scannerProgressBar = new ProgressDialog(context);
        scannerProgressBar.setCancelable(false);
        scannerProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        scannerProgressBar.setMessage("Analyzing your network, please wait");
        
        // TODO move to PlayerActivity
        videoLoadingProgress = new ProgressDialog(context);
        videoLoadingProgress.setCancelable(true);
        videoLoadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        videoLoadingProgress.setMessage("Loading...");

        
        // TODO probably goes away
        if (!initialScanCompleted) {         	
            credentials = new SmbCredentials();            	       

          // TODO don't forget keep this
	        scannerProgressBar.show();	        

	        discoveryAgent = new DiscoveryAgent(this);        
	        discoveryAgent.execute("");
	        initialScanCompleted = true;
        }
        
        setContentView(R.layout.browser_activity);
        // TODO move to DeviceList constructor
        setupBar();
        
        getSupportLoaderManager().initLoader(0, null, this);
        
    }
    
    // TODO removed by class-wide variable
    private LeftNavBar getLeftNavBar() {
        if (mLeftNavBar == null) {
            mLeftNavBar = new LeftNavBar(this);
        }
        return mLeftNavBar;
    }
        
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    
        // TODO unneeded switch statement?
        switch (item.getItemId()) {      
        case R.id.device_logout:
          // TODO removed by class-wide variable
        	((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list)).clearAllFiles();
        	if (FileListFragment.selectedServer != null)
        		credentials.removeCredential(FileListFragment.selectedServer.mac);
        	break;        
        }
        return true;
    }
    
    // TODO moves to DeviceList constructor
    private void setupBar() {
    	
        ActionBar bar = getLeftNavBar();
        
        bar.setBackgroundDrawable(new ColorDrawable(Color.BLACK));        
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_HOME);
        bar.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND, LeftNavBar.DISPLAY_AUTO_EXPAND);
        bar.setDisplayOptions(LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED, LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED);
        
      // TODO class-wide variable
    	FileListFragment fileList = ((FileListFragment) getSupportFragmentManager().findFragmentById(R.id.file_list));

        // TODO moves to DeviceList.reset()
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
    	
    	log(fileType);
    	
    	if (supportedImageFormats.contains(fileType)) {
    		detailIntent = new Intent(this, ImageActivity.class);
    		detailIntent.putExtra("currentPath", DeviceNavigator.getPath());
    		detailIntent.putExtra("fileName", id);
    	} else {
    		detailIntent = new Intent(this, PlayerActivity.class);
        // TODO move to PlayerActivity
    		videoLoadingProgress.show();
    	}
    	
        startActivity(detailIntent);
        
    }
    
    @Override
    public void onPathChanged(String path) {
      // TODO go away!
    	ActionBar bar = getLeftNavBar();

    	bar.setSubtitle(path);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_activity, menu);
        return true;
    }
    
    // TODO go away!
    public static SmbCredentials getCredentials() {
    	return credentials;
    }
    
    public static ProgressDialog getLoaderCircle() {
    	return scannerProgressBar;
    }
    
    // TODO go away!
    public static ProgressDialog getVideoProgressBar() {
    	return videoLoadingProgress;
    }
    
}
