package edu.gentoomen.conduit;

import java.io.InputStream;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import jcifs.smb.SmbFile;

import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.utilities.Utils;

import android.app.Activity;    
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends Activity implements OnClickListener {

    /** Called when the activity is first created. */

	private static final int MAX_IMAGE_COUNT = 100;
    int image_index = 0;
    private static final String TAG = "ImageActivity";
    private static final String SERVER = "http://127.0.0.1:8888";
    private static int CURRENT_IMAGE_INDEX;
    private static LinkedList<SmbFile> listOfPictures; 

    //Returns a linked list of (supported) pictures in the current folder.
    private void getPicturesInCurrentFolder() {
    	LinkedList<SmbFile> folderLS = DeviceNavigator.deviceLS();
    	
    	listOfPictures = new LinkedList<SmbFile>();
    	for(SmbFile f : folderLS) {
    		//if the file type is not in the list of supported file types, remove it from the list.
    		if(BrowserActivity.supportedImageFormats.contains(Utils.getMimeType(f.getName()))) {
    			listOfPictures.add(f);
    		}
    	}
    	
    	CURRENT_IMAGE_INDEX = 0;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        Button btnPrevious = (Button)findViewById(R.id.previous_btn);
        btnPrevious.setOnClickListener(this);       
        Button btnNext = (Button)findViewById(R.id.next_btn);
        btnNext.setOnClickListener(this);
        
        //populate listOfPictures with pictures in the current directory
        getPicturesInCurrentFolder();

        showImage();

    }

    private void showImage() {

        ImageView imgView = (ImageView) findViewById(R.id.myimage);
        Uri uri = Uri.parse(SERVER);
        imgView.setImageBitmap(downloadBitmap(SERVER));
        //imgView.setImageURI(uri);
        imgView.requestFocus();

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case (R.id.previous_btn):

                image_index--;

                if (image_index == -1) {                    
                    image_index = MAX_IMAGE_COUNT - 1;                  
                }

                showImage();

            break;

            case (R.id.next_btn):

                image_index++;

                if (image_index == MAX_IMAGE_COUNT) {               
                	image_index = 0;                
                }

                showImage();

            break;      

        }

    }
    
    //taken from: http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
    //using this for now to test some network problems with ImageActivity
    private static Bitmap downloadBitmap(String url) {
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) { 
                Log.d(TAG, "Error " + statusCode + " while retrieving bitmap from " + url); 
                return null;
            }
            
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent(); 
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();  
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            Log.d(TAG, "Error while retrieving bitmap from " + url);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }
}