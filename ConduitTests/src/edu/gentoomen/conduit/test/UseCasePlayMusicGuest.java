package edu.gentoomen.conduit.test;

import com.jayway.android.robotium.solo.Solo;

import edu.gentoomen.conduit.PlayerActivity;

/*
 * A Test case for playing 1 song, then quitting
 */
public class UseCasePlayMusicGuest extends UseCaseGuest {
	
	private static final String TAG = "UseCasePlayMusicGuest";
	
	private Solo solo;
	
	public UseCasePlayMusicGuest() {
		super();
	}
	
	public void testPlayMusicGuest() {
		
		if (solo == null)
			solo = new Solo(getInstrumentation(), getActivity());
		
		super.findHost(solo);
		super.loginNbtHost(solo);
		navigateToMusicFolderAndPlay();
		
	}
	
	private void navigateToMusicFolderAndPlay() {

		if (solo == null)
			solo = new Solo(getInstrumentation(), getActivity());

		solo.clickOnText(Utils.TEST_FOLDER_1);

		if (!solo.waitForText(Utils.TEST_FOLDER_2)) 
			fail();		
		solo.clickOnText(Utils.TEST_FOLDER_3);

		if (!solo.waitForText(Utils.TEST_FOLDER_4))
			fail();
		solo.clickOnText(Utils.TEST_FOLDER_4);

		if (!solo.waitForText(Utils.TEST_FOLDER_5))
			fail();
		solo.clickOnText(Utils.TEST_FOLDER_5);

		if (!solo.waitForText(Utils.TEST_MUSIC_FILE_1))
			fail();
		solo.clickOnText(Utils.TEST_MUSIC_FILE_1);

		solo.sleep(Utils.PLAY_TIME_SHORT);				
		solo.assertCurrentActivity(TAG, PlayerActivity.class);				
		solo.goBack();

		if (!solo.waitForText(Utils.TEST_MUSIC_FILE_1))
			fail();

		solo.sleep(5000);

	}
	
	public void setup() throws Exception {
		super.setup();
	}
	
	public void tearDown() throws Exception {
		super.tearDown(solo);
	}
}
