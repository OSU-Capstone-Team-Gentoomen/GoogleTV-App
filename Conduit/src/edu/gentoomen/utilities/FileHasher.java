package edu.gentoomen.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;

/*
 * This class will take an input stream and hash the music/video file according
 * to its metadata and random bits from the middle of the file. Not an ideal
 * solution, however computing the md5 on a 600mb file already takes 30 seconds.
 * We are trading reliability for performance.
 */
public class FileHasher {

	private static final String TAG = "FileHasher";

	public static String getMediaFileHash(InputStream is, long fileLength) {

		StringBuilder seed = new StringBuilder();
		byte[] buffer = new byte[8192];

		try {

			is.skip(fileLength / 2);
			is.read(buffer);

			String sampleMD5 = md5(buffer);

			if (sampleMD5 == null)
				return null;

			seed.append(sampleMD5);
			seed.append(fileLength);

			Log.d(TAG, "md5 seed is: " + seed.toString());
			return md5(seed.toString());

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	private static String md5(String input) {

		String retVal = null;

		if (input == null)
			return retVal;

		try {

			MessageDigest hasher = MessageDigest.getInstance("MD5");
			hasher.update(input.getBytes(), 0, input.length());
			retVal = new BigInteger(1, hasher.digest()).toString(16);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return retVal;
	}

	private static String md5(byte[] input) {

		String retVal = null;

		if (input == null)
			return retVal;

		try {
			MessageDigest hasher = MessageDigest.getInstance("MD5");
			hasher.update(input);
			retVal = new BigInteger(1, hasher.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return retVal;
	}

}
