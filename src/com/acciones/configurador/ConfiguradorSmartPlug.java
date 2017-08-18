package com.acciones.configurador;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.comu.comun.Comun;
import com.comu.comun.Comun.ModoSeguridadWifi;

public class ConfiguradorSmartPlug {

	private DatagramSocket enchufeServidor;

	public byte[] configuraSmartPlug(String ssid, String contrasenia, ModoSeguridadWifi modoSeguridadWifi){
		byte[] mensaje = new byte[136];
		mensaje[38]=0x14;//siempre igual, no se sabe por que
		
		//mete ssid en el mensaje
		int indiceEscrituraSSID = 68;//empieza en la posicion 68
		for(char letra : ssid.toCharArray()){
			mensaje[indiceEscrituraSSID]=(byte) letra;
			indiceEscrituraSSID++;
		}
		//mete contrasenia en el mensaje
		int indiceEscrituraContrasenia = 100;//empieza en la posicion 68
		for(char letra : contrasenia.toCharArray()){
			mensaje[indiceEscrituraContrasenia]=(byte) letra;
			indiceEscrituraContrasenia++;
		}
		
		mensaje[132]=(byte) ssid.length();
		mensaje[133]=(byte) contrasenia.length();
		mensaje[134]=(byte) modoSeguridadWifi.ordinal();
		
		//calculo de checksum
		int checksum = 0xbeaf;
		for(byte dato : mensaje){
			checksum += (dato & 0x00ff);
			checksum &= 0xffff;
		}
		mensaje[32] = (byte) (checksum & 0x00ff);
		mensaje[33] = (byte) (checksum >> 8);
		return mensaje;
	}
	
	public void enviaBroadCast(byte[] mensaje){
		try {
			Comun.dameDirIp();
			InetAddress ipBroadcast = InetAddress.getByName(Comun.PartesIPServLoc[0]+"."+Comun.PartesIPServLoc[1]+"."+Comun.PartesIPServLoc[2]+".255");			
			InetAddress inetMiIp = InetAddress.getByAddress(Comun.DirIpServidorLocal);
	  		enchufeServidor = new DatagramSocket(null);
			enchufeServidor.setBroadcast(true);
			enchufeServidor.setReuseAddress(true);
		  	InetSocketAddress address = new InetSocketAddress(inetMiIp, Comun.PuertoServidorLocal);
		  	enchufeServidor.bind(address);		
	        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length, ipBroadcast, Comun.PuertoBroadcast);
	        enchufeServidor.send(datagrama);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
	        if (enchufeServidor != null) {
	            enchufeServidor.close();
	        }	     	      
		}
	}
}
