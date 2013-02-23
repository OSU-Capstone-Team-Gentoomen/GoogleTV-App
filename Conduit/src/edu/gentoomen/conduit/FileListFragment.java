package edu.gentoomen.conduit;

import java.io.IOException;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import edu.gentoomen.conduit.networking.HttpStreamServer;
import edu.gentoomen.utilities.Utils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.app.LoaderManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.database.Cursor;

import android.util.Log;

public class FileListFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	public static final int TYPE_FOLDER = 0;
	public static final int TYPE_FILE = 1;
	
	private static final int FILE_NAME_INDEX = 2;
	private static final int FILE_CONTENT_TYPE = 3;
	
	private String selectedFile ="";
	
	protected static HttpStreamServer server;
	
	private static final String LOG_TAG = "FileListFragment";
	SimpleCursorAdapter mAdapter;
	
    private Callbacks mCallbacks = browserCallback;

    public interface Callbacks {
        public void onFileSelected(String id);
        public void onPathChanged(String path);
    }

    private static Callbacks browserCallback = new Callbacks() {
        @Override
        public void onFileSelected(String id) {}
        @Override
        public void onPathChanged(String path) {}
    };
    	
    Uri baseUri = MediaContentProvider.MEDIA_URI;    
	int selectedType = -1;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        // TODO move this to the fragment XML
        setEmptyText("No files");

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.file_list_item, null,
                new String[] { "name", "type" },
                new int[] { R.id.text1, R.id.text1 }, 0);
        
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
    	   /** Binds the Cursor column defined by the specified index to the specified view */
    	   public boolean setViewValue(View view, Cursor cursor, int columnIndex){
    	       TextView textView = (TextView) view;
    	       textView.setCompoundDrawablePadding(10);
    		   if (columnIndex == 3) {
    	    	   int type = cursor.getInt(columnIndex);
    	    	   if (type == MediaContentProvider.MEDIA) {
    	    		   textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video, 0, 0, 0);
    	    	   } else if (type == MediaContentProvider.FOLDER) {
    	    		   textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dir, 0, 0, 0);
    	    	   }
    	           return true;
    	       }
    	       return false;
    	   }
    	});
        
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);        
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setItemsCanFocus(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = browserCallback;
    }

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
        
        Cursor i = (Cursor) listView.getItemAtPosition(position);
        String fileName = i.getString(FILE_NAME_INDEX);
        
        switch (i.getInt(FILE_CONTENT_TYPE)) {
        case MediaContentProvider.MEDIA:
        	
        	Log.d(LOG_TAG, "clicked on " + fileName);
        	try {
        		server = new HttpStreamServer(DeviceNavigator.getPath() + fileName, Utils.getMimeType(fileName));
			} catch (IOException e) {				
				e.printStackTrace();
			}
        	
        	mCallbacks.onFileSelected(i.getString(FILE_NAME_INDEX));
        	break;
        case MediaContentProvider.FOLDER:
        	setPath(i.getString(FILE_NAME_INDEX));
        	break;
        }
              
	}

    // These are the rows that we will retrieve.
    static final String[] SUMMARY_PROJECTION = new String[] {
        "_ID",
        "name",
        "path",
        "type",
    };


    public void setPath(String fileSelected) {
    	
    	Log.d(LOG_TAG, "Setting path to " + fileSelected);
    	selectedFile = fileSelected;    	    	   
    	getLoaderManager().restartLoader(0, null, this);
    	Log.d(LOG_TAG, "Current path: " + DeviceNavigator.getPath());
    	mCallbacks.onPathChanged(DeviceNavigator.getPath());
    	
    }
    
    public void setSelectedType(int type) {
    	selectedType = type;
    }
    
    public boolean up() {
    	
    	/*Don't go up past root*/
    	if (Utils.isRoot(DeviceNavigator.getPath()))
    		return false;    		
    	    	
    	setPath("..");
    	return true;
    	
    }
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	
    	Uri uri;
    	Log.d(LOG_TAG, "Selected type " + selectedType);
    	
    	switch (selectedType) {
    	case TYPE_FOLDER:
    		uri = Uri.withAppendedPath(baseUri, "");
    		break;
    	case TYPE_FILE:
    		uri = Uri.withAppendedPath(baseUri, "file");
    		break;
    	default:
    		uri = Uri.withAppendedPath(baseUri, "INVALID");
    		break;
    	}
    	
    	Log.d(LOG_TAG, "onCreate DeviceNavigator.Path: " + DeviceNavigator.getPath());
    	
    	setListShownNoAnimation(false);
        return new CursorLoader(getActivity(), uri,
                SUMMARY_PROJECTION, selectedFile, null, "");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        setListShownNoAnimation(true);
        getListView().requestFocus();
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
    
    /*
     * Clear the DeviceNavigator path
     * so we can switch between devices 
     */    
    public void setDevice(String device) {
    	
    	Log.d(LOG_TAG, "setting device to: " + device);
    	DeviceNavigator.setPath("");
    	setPath(device);
    	
    }
}