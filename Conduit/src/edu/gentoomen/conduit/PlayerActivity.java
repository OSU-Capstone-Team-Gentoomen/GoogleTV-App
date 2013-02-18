package edu.gentoomen.conduit;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayerActivity extends Activity {
	private static final String TAG = "PlayerActivity";

	String SrcPath = "http://127.0.0.1:8888";
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Log.d(TAG, "in player activity class");
	    Uri uri = Uri.parse(SrcPath);
	    
	    if(uri == null) {
	    	Log.d(TAG, "url null, returning");
	    	return;
	    }
	    
	    Log.d(TAG, "valid URI " + uri);
	    	    
	    setContentView(R.layout.player_activity);
	    VideoView myVideoView = (VideoView)findViewById(R.id.myvideoview);	    
	    myVideoView.setVideoURI(uri);
	    myVideoView.setMediaController(new MediaController(this));
	    myVideoView.requestFocus();
	    myVideoView.start();	    
	    
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();		
		FileListFragment.server.close();
	}
}
