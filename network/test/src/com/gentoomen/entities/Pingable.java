package com.gentoomen.entities;

import java.net.InetAddress;

public class Pingable {
	public InetAddress addr;
	public boolean isOnline;
	public boolean pinged;
	public boolean hasAuthentication;	
	
	public Pingable(InetAddress a) {
		addr = a;
		isOnline = true;
		pinged = true;
	}
}
