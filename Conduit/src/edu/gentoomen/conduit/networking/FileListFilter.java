package edu.gentoomen.conduit.networking;

import edu.gentoomen.utilities.Utils;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import android.util.Log;

/*
 * FileListFilter
 * 
 * These are the files and folders that are currently skipped:
 * Any of the specified system folders in SYS_FOLDERS
 * Any hidden Linux / Mac folders or files (beginning with a .)
 * Any file types that aren't included in Utils.theMimeTypes
 */

public class FileListFilter implements SmbFileFilter {
	private static final String TAG = "FileListFilter";
	private static final String[] SYS_FOLDERS = { "C$", "H$", "IPC$", "ADMIN$" };

	@Override
	public boolean accept(SmbFile file) throws SmbException {
		/*
		 * make sure that we don't show any hidden or system files if any of the
		 * attributes for this file are anded with these values it will make the
		 * end value greater than 0
		 */

		String name = file.getName();

		if ((file.getAttributes() & (SmbFile.ATTR_HIDDEN | SmbFile.ATTR_SYSTEM)) > 0) {
			Log.d(TAG, "skipping file " + name
					+ " for being either a system file or a hidden file");
			return false;
		}

		// ignore Samba system folders
		for (String str : SYS_FOLDERS) {
			if (name.equalsIgnoreCase(str + "/"))
				return false;
		}

		// ignore Linux / Mac hidden files
		if (name.startsWith("."))
			return false;

		// skip this file if its mime type isn't supported
		if (name.endsWith("/"))
			return true;

		if (Utils.getMimeType(name) == null)
			return false;

		return true;
	}
}
