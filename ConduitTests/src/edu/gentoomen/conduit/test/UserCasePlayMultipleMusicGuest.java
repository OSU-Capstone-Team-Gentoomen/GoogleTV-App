package edu.gentoomen.conduit.test;

import com.jayway.android.robotium.solo.Solo;

import edu.gentoomen.conduit.PlayerActivity;

/*
 * A test case for playing 3 songs, then quitting
 */
public class UserCasePlayMultipleMusicGuest extends UseCaseGuest {

	private static final String TAG = "UserCasePlayMultipleMusicGuest";
	
	private static Solo solo;
	
	public UserCasePlayMultipleMusicGuest() {
		super();
	}
	
	public void testUseCaseMusic2() {
	
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

		if (!solo.searchText(Utils.TEST_MUSIC_FILE_2))
			fail();
		solo.clickOnText(Utils.TEST_MUSIC_FILE_2);

		solo.sleep(Utils.PLAY_TIME_SHORT);				
		solo.assertCurrentActivity(TAG, PlayerActivity.class);		
		solo.goBack();
		
		if (!solo.searchText(Utils.TEST_MUSIC_FILE_3))
			fail();
		solo.clickOnText(Utils.TEST_MUSIC_FILE_3);

		solo.sleep(Utils.PLAY_TIME_SHORT);				
		solo.assertCurrentActivity(TAG, PlayerActivity.class);				
		solo.goBack();
				
	}
	
	public void setup() throws Exception {
		super.setup();
	}
	
	public void tearDown() throws Exception {
		super.tearDown(solo);
	}
}
