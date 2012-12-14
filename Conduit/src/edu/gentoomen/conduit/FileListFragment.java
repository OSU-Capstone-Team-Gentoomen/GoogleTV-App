package edu.gentoomen.conduit;

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
	
	private static final String LOG_TAG = "FileListFragment";
	SimpleCursorAdapter mAdapter;
	
    private Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks {
        public void onFileSelected(String id);
        public void onPathChanged(String path);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onFileSelected(String id) {}
        @Override
        public void onPathChanged(String path) {}
    };
    
	Uri baseUri = Uri.parse("content://edu.gentoomen.conduit.dummyprovider");
	String currentPath = "one";
	
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
    	    	   if (type == DummyProvider.FILE_TYPE) {
    	    		   textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.video, 0, 0, 0);
    	    	   } else if (type == DummyProvider.DIR_TYPE) {
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
        mCallbacks = sDummyCallbacks;
    }

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
        
        Cursor i = (Cursor) listView.getItemAtPosition(position);
        
        switch (i.getInt(3)) {
        case DummyProvider.FILE_TYPE:
        	mCallbacks.onFileSelected(i.getString(2));
        	break;
        case DummyProvider.DIR_TYPE:
        	setPath(i.getString(2));
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

    public void setPath(String path) {
    	currentPath = path;
    	getLoaderManager().restartLoader(0, null, this);
    	mCallbacks.onPathChanged(path);
    }
    
    public boolean up() {
    	int i = currentPath.lastIndexOf('/');
    	if (i != -1) {
    		String path = currentPath.substring(0, i);
    		setPath(path);
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    	Uri uri = Uri.withAppendedPath(baseUri, currentPath);
    	setListShownNoAnimation(false);
        return new CursorLoader(getActivity(), uri,
                SUMMARY_PROJECTION, "", null, "");
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
}