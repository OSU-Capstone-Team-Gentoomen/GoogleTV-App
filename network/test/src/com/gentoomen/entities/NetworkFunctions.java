package com.gentoomen.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.widget.*;

public class NetworkFunctions {

	private static String[] diagButtons = new String[]{		
		"Get Default Gateway", "Get Current IP Address","Get Subnet Mask" ,"Ping router",
		"Determine scannable IP Range"
	};
	
	private static String[] browsingButtons = new String[]{
		"Browsing not implemented"
	};
	
	private static String[] pingingButtons = new String[]{
		"Ping all addresses on network", "Scan for Samba on network"
	};
	
    public static class NetworkFunctionItem {

        public String id;
        public String tabTitle;
        public String[] mContent;
        

        public NetworkFunctionItem(String id, String title, String[] content) {
            this.id = id;
            this.tabTitle = title;
            mContent = content;
        }
        
        @Override
        public String toString() {
            return tabTitle;
        }
    }

    public static List<NetworkFunctionItem> ITEMS = new ArrayList<NetworkFunctionItem>();
    public static Map<String, NetworkFunctionItem> ITEM_MAP = new HashMap<String, NetworkFunctionItem>();

    static {    	           	
    	addItem(new NetworkFunctionItem("0", "Diagnostic Information/Permissions Testing", diagButtons));
        addItem(new NetworkFunctionItem("1", "Pinging/Scanning", pingingButtons));
        addItem(new NetworkFunctionItem("2", "Browsing/Authentication", browsingButtons));
    }
    
   
    private static void addItem(NetworkFunctionItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }
}
