package edu.gentoomen.utilities;

public class NetworkUtils {

	public static int[] getIpRange(int gateway, int netmask) {
		int[] range = new int[2];

		range[0] = gateway & netmask;
		range[1] = range[0] | (~netmask);

		return range;

	}

	public static int[] getOctets(int num) {

		int octets[] = new int[4];

		octets[0] = num & 0xFF;
		octets[1] = (num >> 8) & 0xFF;
		octets[2] = (num >> 16) & 0xFF;
		octets[3] = (num >> 24) & 0xFF;

		return octets;

	}

	public static String bytesToHexString(byte[] bytes) {

		StringBuilder retVal = new StringBuilder();
		StringBuilder tmpString;

		for (int i = 0; i < bytes.length; i++) {

			tmpString = new StringBuilder();
			tmpString.append(Integer.toHexString(0xFF & bytes[i]));

			if (tmpString.length() == 1)
				tmpString.insert(0, '0');

			retVal.append(tmpString);

			if (i != bytes.length - 1)
				retVal.append(":");
		}

		return retVal.toString();
	}
}
