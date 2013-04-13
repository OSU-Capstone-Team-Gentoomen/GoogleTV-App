package edu.gentoomen.conduit;

import java.io.IOException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import edu.gentoomen.conduit.contentproviders.ResumeContentProvider;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.conduit.networking.HttpStreamServer;
import edu.gentoomen.utilities.FileHasher;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayerActivity extends Activity {

	/*HttpStreamServer should be moved here*/
	private static HttpStreamServer server; /*Static?*/
	
	private static final String TAG = "PlayerActivity";
	private static final String SRC_PATH = "http://127.0.0.1:8888";
	private static final int TIME_COL = 3;

	private boolean updateEntry = false;
	private String fileHash = null;

	VideoView myVideoView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "in player activity class");
		Uri uri = Uri.parse(SRC_PATH);

		if (uri == null) {
			Log.d(TAG, "url null, returning");
			return;
		}

		Log.d(TAG, "valid URI " + uri);

		setContentView(R.layout.player_activity);
		myVideoView = (VideoView) findViewById(R.id.myvideoview);
		myVideoView.requestFocus();
		myVideoView.setVideoURI(uri);
		myVideoView.setMediaController(new MediaController(this));

		myVideoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			// Will listen for the end of the video
			public void onCompletion(MediaPlayer mp) {

				if (fileHash != null)
					removeResumeMedia(fileHash);

				finish();
			}
		});

		myVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {

				String projection[] = { 
						ResumeContentProvider.COL_HASH,
						ResumeContentProvider.COL_NAME,
						ResumeContentProvider.COL_PATH,
						ResumeContentProvider.COL_TIME };

				try {
					fileHash = FileHasher.getMediaFileHash(
							FileListFragment.server.getFile().getInputStream(),
							FileListFragment.server.getFile().length());
				} catch (SmbException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (fileHash != null) {

					Cursor curse = getContentResolver().query(
							ResumeContentProvider.CONTENT_URI,
							projection,
							ResumeContentProvider.COL_HASH + "= '" + fileHash
									+ "'", null, "");
					if (curse.getCount() == 1) {
						updateEntry = true;
						if (curse.moveToNext()) {
							Log.d(TAG,
									"Need to resume at point: "
											+ curse.getInt(TIME_COL));
							myVideoView.seekTo(curse.getInt(TIME_COL));
						}
					}
				}

				// BrowserActivity.getVideoProgressBar().cancel();
				myVideoView.start();

			}
		});

	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		FileListFragment.server.close();

		// if (BrowserActivity.getVideoProgressBar().isShowing())
		// BrowserActivity.getVideoProgressBar().cancel();

	}

	@Override
	public void onBackPressed() {

		SmbFile file = FileListFragment.server.getFile();

		if (myVideoView != null && myVideoView.hasFocus() && file != null
				&& fileHash != null) {
			try {
				if (updateEntry)
					updateResumeMediaEntry(myVideoView.getCurrentPosition());
				else
					insertNewResumeMedia(file.getName(),
							FileHasher.getMediaFileHash(file.getInputStream(),
									file.length()), DeviceNavigator.getPath(),
							myVideoView.getCurrentPosition());
			} catch (SmbException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		super.onBackPressed();
	}

	private void updateResumeMediaEntry(int currentPosition) {

		ContentValues values = new ContentValues();
		values.put(ResumeContentProvider.COL_TIME, currentPosition);
		Log.d(TAG, "Updating current video file play time: " + currentPosition);
		getContentResolver().update(ResumeContentProvider.CONTENT_URI, values,
				ResumeContentProvider.COL_HASH + "='" + fileHash + "'", null);

	}

	private void insertNewResumeMedia(String name, String hash, String path,
			int played) {

		ContentValues values = new ContentValues();
		values.put(ResumeContentProvider.COL_HASH, hash);
		values.put(ResumeContentProvider.COL_NAME, name);
		values.put(ResumeContentProvider.COL_TIME, played);
		values.put(ResumeContentProvider.COL_PATH, path);

		Log.d(TAG, "Inserting Time: " + played + ", Hash: " + hash + ", Path: "
				+ path + ", Name: " + name);

		getContentResolver().insert(ResumeContentProvider.CONTENT_URI, values);

	}

	private void removeResumeMedia(String hash) {
		getContentResolver().delete(ResumeContentProvider.CONTENT_URI,
				ResumeContentProvider.COL_HASH + "='" + hash + "'", null);
	}

}
