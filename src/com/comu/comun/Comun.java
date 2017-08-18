package com.comu.comun;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Comun {
	public enum ModoSeguridadWifi {Ninguno, WEP, WPA1, WPA2, WPA12};
	public final static int PuertoBroadcast = 80;
	public final static int PuertoServidorLocal = 36939;
	public static byte[] DirIpServidorLocal=null;
	public static String[] PartesIPServLoc=null;
	
	private final static String PREFIJO_LAN = "192.168.";	
	public static void dameDirIp() {
		DirIpServidorLocal = new byte[4];
		Enumeration e=null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(e!= null && e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress inet = (InetAddress) ee.nextElement();
		        if(inet.getHostAddress().contains(PREFIJO_LAN)){
		        	PartesIPServLoc=inet.getHostAddress().split("\\.");
		        	for(int i=0;i<4;i++){
		        		DirIpServidorLocal[i]=(byte) Integer.parseInt(PartesIPServLoc[i]);
		        	}		        	
		        }
		    }
		}
	}
}
