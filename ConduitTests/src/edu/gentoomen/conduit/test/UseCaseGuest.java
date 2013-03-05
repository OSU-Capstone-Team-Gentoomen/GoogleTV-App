package edu.gentoomen.conduit.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jayway.android.robotium.solo.Solo;

import edu.gentoomen.conduit.BrowserActivity;

/*
 * A parent class for a guest session
 */
public class UseCaseGuest extends ActivityInstrumentationTestCase2<BrowserActivity> {

	private static final String TAG = "UseCasePlayMusicGuest";
	
	public UseCaseGuest() {		
		super(BrowserActivity.class);		
	}
	
	protected void findHost(Solo solo) {
		
		if (solo == null)
			solo = new Solo(getInstrumentation(), getActivity());

		Log.d(TAG, "Waiting for dialog to close");
		solo.waitForText(Utils.TEST_HOST_NAME, 1, 45000);
		Log.d(TAG, "Network scan completed");		
		assertTrue(solo.searchText(Utils.TEST_HOST_NAME, true));
		
	}
	
	protected void loginNbtHost(Solo solo) {

		if (solo == null)
			solo = new Solo(getInstrumentation(), getActivity());

		Log.d(TAG, "Attempting to login to share");
		solo.clickOnText(Utils.TEST_HOST_NAME);
		solo.clickOnText("Login as Guest");
		Log.d(TAG, "Logged in as guest");
		
		if (!solo.waitForText(Utils.TEST_FOLDER_1))
			fail();
				
		Log.d(TAG, "Found test folder");

	}
		
	public void setup() throws Exception {		
		super.setUp();	
	}

	public void tearDown(Solo solo) throws Exception {
		
		if (solo != null) {
			solo.sendKey(Solo.MENU);
			solo.clickOnText("Logout of device");
			solo.finishOpenedActivities();
		}
		
		super.tearDown();		
	}
	
}
