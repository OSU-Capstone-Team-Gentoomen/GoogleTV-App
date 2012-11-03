package com.gentoomen.sambadisoverytest;

import com.gentoomen.sambadisoverytest.dummy.NetworkFunctions;
import com.gentoomen.sambadiscoverytest.discoveryagent.*;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceDetailFragment extends Fragment {

	private final static int DIAG = 0;
	private final static int PINGSCAN = 1;
	private final static int BROWSE = 2;	
	
	private Context activityContext;
	private DiscoveryAgent myAgent;
	
    public static final String ARG_ITEM_ID = "item_id";

    NetworkFunctions.NetworkFunctionItem mItem;

    public DeviceDetailFragment() { 	
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = NetworkFunctions.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
        if(myAgent == null){
        	myAgent = new DiscoveryAgent((Context)getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	View rootView =  inflater.inflate(R.layout.fragmment_pingbrowse, container, false);  
    	
    	 if (mItem != null) {
    		 ListView mListView = (ListView) rootView.findViewById(R.id.testingList);
    		 ArrayAdapter<String> adapter = new ArrayAdapter<String>((Context)getActivity(), android.R.layout.simple_list_item_1, mItem.mContent);
    		 mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {    			

				public void onItemClick(AdapterView arg0, View arg1,
						int arg2, long arg3) {
					int mTabPosition = Integer.parseInt(NetworkFunctions.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID)).id);
					switch(mTabPosition){
					
					case 0:
						showToastMessage(myAgent.doInBackground(new String[]{String.valueOf(arg2)}));
						break;
					case 1:
						showToastMessage("Pinging/Scanning Not Implemented");
						break;
					case 2:
						showToastMessage("Browsing/Authentication Not Implemented");
					}
//					switch(arg2){
//					
//					case 0:
//						showToastMessage(myAgent.doInBackground(new String[]{String.valueOf(arg2)}));
//						break;
//					case 1:
//						break;
//					case 2:
//						break;
//					default:
//						break;
//					}
					
				}
    			     		 
			});    		     		
    		 mListView.setAdapter(adapter);
         }
    	
    	return rootView;
    }
    
    private void showToastMessage(String message){    	
    	Toast.makeText((Context)getActivity(), message, Toast.LENGTH_SHORT).show();
    }
            

    
    
}
