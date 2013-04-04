package edu.gentoomen.conduit.networking;

import java.net.InetAddress;

public class Pingable {
	
	public InetAddress addr;
	public String ip;
	public String mac;
	public String nbtName;
	
	public Pingable(InetAddress a, String macAddress, String nbt) {
		
		addr = a;				
		ip = a.getHostAddress();		
		mac = macAddress;
		nbtName = nbt;
	}
	
	@Override
	public int hashCode(){
		return mac.hashCode();
	}
	
}
