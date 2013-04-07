package edu.gentoomen.conduit;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

import jcifs.smb.SmbFile;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.utilities.Utils;

public class ImageActivity extends Activity implements OnClickListener {

	/** Called when the activity is first created. */

	private static int MAX_IMAGE_COUNT = 0;
	private static int current_image_index = 0;
	private static final String TAG = "ImageActivity";
	private static final String SERVER = "http://127.0.0.1:8888";
	private static String currentPhoto = null;
	private static String currentPath = null;

	private static ArrayList<SmbFile> listOfPictures;

	// Returns a linked list of (supported) pictures in the current folder.

	private void getPicturesInCurrentFolder() {

		LinkedList<SmbFile> folderLS = DeviceNavigator.deviceLS();

		listOfPictures = new ArrayList<SmbFile>();
		for (SmbFile f : folderLS) {
			// if the file type is not in the list of supported file types,
			// remove it from the list.
			if (Utils.supportedImageFormats.contains(Utils.getExtension(f
					.getName()))) {
				listOfPictures.add(f);
			}
		}

		MAX_IMAGE_COUNT = listOfPictures.size();

		/* Find where our selected image is in the list */
		for (int i = 0; i < listOfPictures.size(); i++) {
			if (listOfPictures.get(i).getName().equalsIgnoreCase(currentPhoto)) {
				current_image_index = i;
				break;
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// String path = getIntent().getExtras().getString("currentPath");
		// Log.d(TAG, "the current path: " + path);

		/* Need to put in null checks!! */
		currentPhoto = getIntent().getExtras().getString("fileName");
		currentPath = getIntent().getExtras().getString("currentPath");
		getPicturesInCurrentFolder();

		setContentView(R.layout.image_activity);

		Button btnPrevious = (Button) findViewById(R.id.previous_btn);
		btnPrevious.setOnClickListener(this);
		Button btnNext = (Button) findViewById(R.id.next_btn);
		btnNext.setOnClickListener(this);

		showImage();

	}

	private void showImage() {

		ImageView imgView = (ImageView) findViewById(R.id.myimage);
		new ImageDownloader(imgView).execute(SERVER);

	}

	public void onClick(View v) {

		switch (v.getId()) {

		case (R.id.previous_btn):
			current_image_index--;
			/*
			 * Going before the first image in the array will go to the last
			 * image
			 */
			if (current_image_index == -1) {
				current_image_index = MAX_IMAGE_COUNT - 1;
			}

			break;

		case (R.id.next_btn):
			current_image_index++;

			if (current_image_index == MAX_IMAGE_COUNT) {
				current_image_index = 0;
			}

			break;

		}
		Log.d(TAG,
				"New file selected: " + currentPath
						+ listOfPictures.get(current_image_index).getName());
		FileListFragment.server
				.setNewFile(
						currentPath
								+ listOfPictures.get(current_image_index)
										.getName(),
						Utils.getMimeType(listOfPictures.get(
								current_image_index).getName()));
		showImage();
	}

	/* This class will handle all the network streaming logic */
	private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
		ImageView myImage;

		/* Need to get the pointer to the image element in the layout */
		public ImageDownloader(ImageView image) {
			this.myImage = image;
		}

		@Override
		protected Bitmap doInBackground(String... url) {
			String httpURL = url[0];
			Bitmap image = null;

			try {
				InputStream in = new URL(httpURL).openStream();
				image = BitmapFactory.decodeStream(in);
			} catch (MalformedURLException e) {
				Log.d(TAG, "Invalid URL");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "IO Exception thrown");
				e.printStackTrace();
			}

			return image;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			myImage.setImageBitmap(bitmap);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		FileListFragment.server.close();
	}
}