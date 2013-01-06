package edu.gentoomen.conduit.networking;

import java.net.InetAddress;

public class Pingable {
	public InetAddress addr;
	public String ip;
	
	public Pingable(InetAddress a) {
		addr = a;				
		ip = a.getHostAddress();
	}
	
	@Override
	public int hashCode(){
		return ip.hashCode();
	}
	
}
