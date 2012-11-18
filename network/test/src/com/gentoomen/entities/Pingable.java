package com.gentoomen.entities;

import java.net.InetAddress;

public class Pingable {
	public InetAddress addr;
	public int ip;
	public boolean hasSambaShare = false;	
	
	public Pingable(InetAddress a) {
		addr = a;
		ip = ipToInt(addr.getHostAddress());
	}
	
	@Override
	public int hashCode(){
		return this.ip;
	}
	
	private int ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");
        int value = 0;
        for (int i=0; i<addrArray.length; i++) {            
            value += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,3-i)));
        }
        return value;

    }
}
