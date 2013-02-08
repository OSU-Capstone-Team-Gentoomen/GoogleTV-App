package edu.gentoomen.conduit;

import java.util.LinkedList;

import jcifs.smb.SmbFile;

import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.utilities.Utils;

import android.app.Activity;    
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends Activity implements OnClickListener {

    /** Called when the activity is first created. */

	private static final int MAX_IMAGE_COUNT = 100;
    int image_index = 0;
    private static int CURRENT_IMAGE_INDEX;
    private static LinkedList<SmbFile> listOfPictures; 

    //Returns a linked list of (supported) pictures in the current folder.
    private static LinkedList<SmbFile>getPicturesInCurrentFolder() {
    	listOfPictures = DeviceNavigator.deviceLS();
    	for(SmbFile f : listOfPictures) {
    		//if the file type is not in the list of supported file types, remove it from the list.
    		if(!BrowserActivity.supportedImageFormats.contains(Utils.getMimeType(f.getName()))) {
    			listOfPictures.remove(f);
    		}
    	}
    	CURRENT_IMAGE_INDEX = 0;
    	return listOfPictures;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.image_activity);
//
//        Button btnPrevious = (Button)findViewById(R.id.previous_btn);
//        btnPrevious.setOnClickListener(this);       
//        Button btnNext = (Button)findViewById(R.id.next_btn);
//        btnNext.setOnClickListener(this);

        showImage();        

    }

    private void showImage() {

//        ImageView imgView = (ImageView) findViewById(R.id.myimage);
//        Uri uri = Uri.parse("http://127.0.0.1:8888");
//        imgView.setImageURI(uri);       

    }

    public void onClick(View v) {

//        switch (v.getId()) {
//
//            case (R.layout.image_activity):
//
//                image_index--;
//
//                if (image_index == -1) {                    
//                    image_index = MAX_IMAGE_COUNT - 1;                  
//                }
//
//                showImage();
//
//            break;
//
//            case (R.id.next_btn):
//
//                image_index++;
//
//                if (image_index == MAX_IMAGE_COUNT) {               
//                image_index = 0;                
//            }
//
//                showImage();
//
//            break;      
//
//        }

    }
}