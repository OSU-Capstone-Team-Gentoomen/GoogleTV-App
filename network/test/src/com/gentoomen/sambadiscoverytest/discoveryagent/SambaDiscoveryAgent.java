package com.gentoomen.sambadiscoverytest.discoveryagent;

import jcifs.Config;
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
		//Config.setProperty("jcifs.smb.client.username", "guest");
		//Config.setProperty("jcifs.smb.client.domain", null);
		//Config.setProperty("jcifs.smb.client.password", null);
		
		//defaultAuth = NtlmPasswordAuthentication.ANONYMOUS;
		//domain = UniAddress.getByName("smb://192.168.1.11");
		//SmbSession.logon(domain, defaultAuth);
	}
	
	public int findNumOfSambaShares() throws Exception{
		
		SmbFile[] server = new SmbFile("smb://192.168.1.37").listFiles();			
		return server.length;
		
	}
	
	public LinkedList<String> getFileListing(String path) throws Exception{
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		SmbFile share = new SmbFile(path);			
		SmbFile[] fileArray = share.listFiles();		

		for(SmbFile file : fileArray){
			fileList.add(file.getName());
			System.out.println(file.getName());
		}

		return fileList;
		
	}
	
}
