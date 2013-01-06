package edu.gentoomen.conduit;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayerActivity extends Activity {

	String SrcPath = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.player_activity);
	    VideoView myVideoView = (VideoView)findViewById(R.id.myvideoview);
	    myVideoView.setVideoURI(Uri.parse(SrcPath));
	    myVideoView.setMediaController(new MediaController(this));
	    myVideoView.requestFocus();
	    myVideoView.start();
	}
}
