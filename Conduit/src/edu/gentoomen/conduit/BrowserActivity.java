package edu.gentoomen.conduit;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.google.tv.leftnavbar.LeftNavBar;

import edu.gentoomen.conduit.contentproviders.DeviceContentProvider;
import edu.gentoomen.conduit.networking.Device;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.conduit.networking.DiscoveryAgent;
import edu.gentoomen.utilities.SmbCredentials;
import edu.gentoomen.utilities.Utils;

public class BrowserActivity extends FragmentActivity implements
		FileListFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {

	private static DeviceList deviceList;
	private static FileListFragment fileList;

	// TODO remove SmbCredentials
	private static SmbCredentials credentials;

	private static DiscoveryAgent discoveryAgent;
	private static ProgressDialog progressDialog;
	// TODO move to PlayerActivity if possible
	// private static ProgressDialog videoLoadingProgress;

	private static final int COL_IP_ADDRESS = 1;
	private static final int COL_MAC = 2;
	private static final int COL_NBT_ADDRESS = 3;

	// These are the rows that we will retrieve.
	static final String[] SUMMARY_PROJECTION = new String[] {
			DeviceContentProvider.ID, DeviceContentProvider.COL_IP_ADDRESS,
			DeviceContentProvider.COL_MAC, DeviceContentProvider.COL_NBTADR };

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, DeviceContentProvider.CONTENT_URI,
				SUMMARY_PROJECTION, "", null, "");
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		deviceList.reset();

		while (data.moveToNext()) {

			String title;
			String ip = data.getString(COL_IP_ADDRESS);
			String mac = data.getString(COL_MAC);
			String nbtName = data.getString(COL_NBT_ADDRESS);

			// TODO move to content provider
			if (nbtName == null)
				title = ip;
			else
				title = nbtName;

			log("Adding device to list: " + mac);

			// TODO abstract concept of adding a device tab
			deviceList.addTab(
					deviceList.newTab().setText(title)
							.setTag(DiscoveryAgent.macToPingable(mac))
							.setIcon(R.drawable.tab_d)
							.setTabListener(new TabListener(title)), false);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private class DiscoveryAgentListener implements DiscoveryAgent.ScanListener {
		@Override
		public void onScanStarted() {
			progressDialog.show();
		}

		@Override
		public void onScanFinished() {
			progressDialog.cancel();
		}
	}

	private class RefreshTabListener implements ActionBar.TabListener {

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			discoveryAgent.scan();
		}

		// TODO remove if unnecessary
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			onTabReselected(tab, ft);
		}
	}

	public void onBackPressed() {
		if (!fileList.up()) {

			/* confirm if the user really wants to exit */
			DialogInterface.OnClickListener exitDialog = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						finish();
					case DialogInterface.BUTTON_NEGATIVE:
						break;
					}

				}
			};

			// TODO move to XML
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Exit Conduit?")
					.setPositiveButton("Yes", exitDialog)
					.setNegativeButton("No", exitDialog).show();
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		deviceList = new DeviceList(this);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// TODO better message
		progressDialog.setMessage("Analyzing your network, please wait");

		// TODO move to PlayerActivity
		// videoLoadingProgress = new ProgressDialog(this);
		// videoLoadingProgress.setCancelable(true);
		// videoLoadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// videoLoadingProgress.setMessage("Loading...");

		credentials = new SmbCredentials();

		ContentResolver resolver = this.getContentResolver();
		DhcpInfo info = ((WifiManager) this
				.getSystemService(Context.WIFI_SERVICE)).getDhcpInfo();
		discoveryAgent = new DiscoveryAgent(resolver, info,
				new DiscoveryAgentListener());
		discoveryAgent.scan();

		setContentView(R.layout.browser_activity);
		getSupportLoaderManager().initLoader(0, null, this);

		fileList = ((FileListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.file_list));

		if (fileList == null)
			log("fragment not found");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.device_logout) {
			fileList.clearAllFiles();

			// TODO fix me
			if (FileListFragment.selectedServer != null)
				credentials
						.removeCredential(FileListFragment.selectedServer.mac);
		}
		return true;
	}

	@Override
	public void onFileSelected(String id) {

		String fileType = Utils.getExtension(id);
		Intent detailIntent;

		if (Utils.supportedImageFormats.contains(fileType)) {
			detailIntent = new Intent(this, ImageActivity.class);
			detailIntent.putExtra("currentPath", DeviceNavigator.getPath());
			detailIntent.putExtra("fileName", id);
		} else {
			detailIntent = new Intent(this, PlayerActivity.class);
			// TODO move to PlayerActivity
			// videoLoadingProgress.show();
		}

		startActivity(detailIntent);
	}

	@Override
	public void onPathChanged(String path) {
		deviceList.setSubtitle(path);
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

	private class TabListener implements ActionBar.TabListener {

		// private String mTitle;

		// TODO update signature to remove fileList
		public TabListener(String title) {
			// mTitle = title;
		}

		@Override
		/* Function needs refactoring */
		public void onTabSelected(final Tab tab, FragmentTransaction ft) {

			// TODO try to remove this. related to Android auto-selecting
			// invalid tab
			try {

				log("tab selected " + ((Device) tab.getTag()).mac
						+ " tab position: " + tab.getPosition());
				/* Check to ensure that the tag passed in is a valid IP address */
				if (!((Device) tab.getTag()).ip
						.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))
					return;

			} catch (Exception e) {
				log("Invalid tab selected");
				return;
			}

			// TODO no!
			FileListFragment.selectedServer = ((Device) tab.getTag());

			if (credentials.hostHasAuth(FileListFragment.selectedServer.mac)) {
				log("host address authentication exists");
				loginToShare();
				return;
			}

			// TODO UI button for login
			// TODO define UI in XML
			showLogin(tab);
		}

		// TODO remove if necessary
		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			onTabSelected(tab, ft);
		}

	}

	private class DeviceList extends LeftNavBar {
		public DeviceList(Context context) {
			super((Activity) context);

			this.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
			this.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			this.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
					ActionBar.DISPLAY_SHOW_HOME);
			this.setDisplayOptions(LeftNavBar.DISPLAY_AUTO_EXPAND,
					LeftNavBar.DISPLAY_AUTO_EXPAND);
			this.setDisplayOptions(LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED,
					LeftNavBar.DISPLAY_USE_LOGO_WHEN_EXPANDED);

			this.reset();
		}

		public void reset() {
			this.removeAllTabs();
			this.addTab(
					newTab().setText("Refresh")
							.setTabListener(new RefreshTabListener())
							.setIcon(R.drawable.refresh_normal), false);
		}
	}

	private void showLogin(final Tab tab) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter Username and Password");
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.dialog_logon, null);
		builder.setView(view);

		builder.setPositiveButton("Login",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String username = ((EditText) view
								.findViewById(R.id.username)).getText()
								.toString();
						String password = ((EditText) view
								.findViewById(R.id.password)).getText()
								.toString();

						if (!username.isEmpty() && !password.isEmpty()) {
							credentials.addCredentials(
									((Device) tab.getTag()).mac, username,
									password);
							loginToShare();
						}

						return;

					}
				});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		// TODO remove log in as guest
		builder.setNeutralButton("Login as Guest",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						credentials.addCredentials(
								FileListFragment.selectedServer.mac, "guest",
								"");
						loginToShare();

					}
				});

		builder.create();
		builder.show();
	}

	private void loginToShare() {

		String title;

		if (FileListFragment.selectedServer.nbtName != null)
			title = FileListFragment.selectedServer.nbtName;
		else
			title = FileListFragment.selectedServer.ip;

		log(fileList == null ? "fileList is null" : "not null");
		fileList.setSelectedType(FileListFragment.TYPE_FOLDER);
		fileList.setDevice(title);
		deviceList.setTitle(title);

	}

	private void log(String message) {
		Log.d("MainActivity", message);
	}

}
