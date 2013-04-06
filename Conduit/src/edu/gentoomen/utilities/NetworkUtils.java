package edu.gentoomen.utilities;

public class NetworkUtils {

	public static int[] getIpRange(int gateway, int netmask) {
		int[] range = new int[2];
		
		range[0] = gateway & netmask;
		range[1] = range[0] | (~netmask);
		
		return range;
		
	}
}
