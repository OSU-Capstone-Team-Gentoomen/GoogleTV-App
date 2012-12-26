package edu.gentoomen.utilities;

import java.util.Arrays;
import java.util.HashSet;

import android.database.Cursor;

public class DatabaseUtility {

	public static void checkColumns(String[] projection,
									String[] availableColumns) {
		
		if(projection != null){
			HashSet<String> availColumns = 
					new HashSet<String>(Arrays.asList(availableColumns));
			
			HashSet<String> reqColumns = 
					new HashSet<String>(Arrays.asList(projection));
			
			/*Check if all columns that were requested are available*/
			if(!availColumns.containsAll(reqColumns))
				throw new IllegalArgumentException("Unknown Column in projection");
		}
		
	}
	
	/*Possible method that we can refactor the query to...?*/
	public static Cursor buildQuery() {
		
		return null;
	}
}
