package com.gentoomen.sambadiscoverytest.discoveryagent;

import jcifs.UniAddress;
import jcifs.smb.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.gentoomen.entities.Pingable;

import android.content.Context;
import android.graphics.Path;
import android.net.DhcpInfo;

/*
 * This class will focus on samba discovery
 * for the network backend
 */
public class SambaDiscoveryAgent {

	private UniAddress domain;
	private NtlmPasswordAuthentication defaultAuth;
	
	private LinkedList<Pingable> hosts;
	
	protected SambaDiscoveryAgent(LinkedList<Pingable> hosts) throws Exception{
		this.hosts = hosts;
		
		defaultAuth = new NtlmPasswordAuthentication("smb://192.168.1.11", "guest", "");
		domain = UniAddress.getByName("smb://192.168.1.11");
		SmbSession.logon(domain, defaultAuth);
	}
	
	public int findNumOfSambaShares() throws Exception{
		
		SmbFile[] server = new SmbFile("smb://192.168.1.11", defaultAuth).listFiles();			
		return server.length;
		
	}
	
	public LinkedList<String> getFileListing(String path) throws Exception{
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		SmbFile share = new SmbFile(path, defaultAuth);			
		SmbFile[] fileArray = share.listFiles();		

		for(SmbFile file : fileArray){
			fileList.add(file.getName());
			System.out.println(file.getName());
		}

		return fileList;
		
	}
	
	private String intToIp(int num){
		return (num & 0xFF) + "." +  ((num >> 8 ) & 0xFF) + "."
				+ ((num >> 16 ) & 0xFF) + "." + ((num >> 24 ) & 0xFF );
	}
}
