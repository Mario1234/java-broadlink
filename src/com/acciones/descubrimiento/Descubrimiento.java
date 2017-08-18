package com.acciones.descubrimiento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import com.acciones.control.SmartPlug;
import com.comu.comun.Comun;
import com.comu.comun.UnicodeFormater;

public class Descubrimiento {
	
	private Set<SmartPlug> listaDispositivos;
	private DatagramSocket enchufeServidor;

	public boolean descubreDispositivos(int timeout){
		Comun.dameDirIp();
		if(Comun.DirIpServidorLocal==null){
			return false;
		}
		byte[] mensaje = new byte[48];
		Calendar cal = Calendar.getInstance();
		Date hoy = new Date();
		TimeZone tz = TimeZone.getTimeZone("Europe/Spain");
		long desplazamiento = tz.getOffset(hoy.getTime());
		int zonaHoraria = (int) (desplazamiento+1);
		if(zonaHoraria<0){
			mensaje[8] = (byte) (0xff + zonaHoraria - 1);
			mensaje[9] = (byte) 0xff;
			mensaje[10] = (byte) 0xff;	
			mensaje[11] = (byte) 0xff;
		}
		else{
			mensaje[8] = (byte) zonaHoraria;
			mensaje[9] = 0x00;
			mensaje[10] = 0x00;	
			mensaje[11] = 0x00;
		}
		mensaje[12] = (byte) (cal.get(Calendar.YEAR) & 0x0ff);
		mensaje[13] = (byte) (cal.get(Calendar.YEAR) >> 8);
		mensaje[14] = (byte) cal.get(Calendar.MINUTE);
		mensaje[15] = (byte) cal.get(Calendar.HOUR_OF_DAY);
		int anio2Dig = cal.get(Calendar.YEAR)%100;
		mensaje[16] = (byte) anio2Dig;
		mensaje[17] = (byte) (cal.get(Calendar.DAY_OF_WEEK)-1);
		mensaje[18] = (byte) cal.get(Calendar.DAY_OF_MONTH);
		mensaje[19] = (byte) (cal.get(Calendar.MONTH)+1);
		mensaje[24] = (byte)Comun.DirIpServidorLocal[0];
		mensaje[25] = (byte)Comun.DirIpServidorLocal[1];
		mensaje[26] = (byte)Comun.DirIpServidorLocal[2];
		mensaje[27] = (byte)Comun.DirIpServidorLocal[3];
		mensaje[28] = Comun.PuertoServidorLocal & 0x0ff;
		mensaje[29] = (byte) (Comun.PuertoServidorLocal >> 8);
	  	mensaje[38] = 6;
	  	int checksum = 0xbeaf;
	  	for(int i=0;i<mensaje.length;i++){
	  		checksum += (mensaje[i]& 0x0ff);	  
	  	}
	  	checksum &= 0xffff;
	  	mensaje[32] = (byte) (checksum & 0x0ff);
	  	mensaje[33] = (byte) (checksum >> 8);	  	
	  	
	  	//comunicacion descubrimiento
	  	listaDispositivos = new TreeSet<SmartPlug>();		
	  	enviaMensajeDescubrimiento(mensaje);
	  	UnicodeFormater.byteArrayToHexPantalla(mensaje,false);
	  	return recibeRespuestaDescubrimiento(timeout);
	}

	public Set<SmartPlug> dameListaDispositivos(){
		return listaDispositivos;
	}
	
	private void enviaMensajeDescubrimiento(byte[] mensaje){
	  	try {
	  		InetAddress inetMiIp = InetAddress.getByAddress(Comun.DirIpServidorLocal);
	  		enchufeServidor = new DatagramSocket(null);
			enchufeServidor.setBroadcast(true);
			enchufeServidor.setReuseAddress(true);
		  	InetSocketAddress address = new InetSocketAddress(inetMiIp, Comun.PuertoServidorLocal);
		  	enchufeServidor.bind(address);		
	  		System.out.println("dir broadcast de LAN: "+Comun.PartesIPServLoc[0]+"."+Comun.PartesIPServLoc[1]+"."+Comun.PartesIPServLoc[2]+".255");
			InetAddress ipBroadcast = InetAddress.getByName(Comun.PartesIPServLoc[0]+"."+Comun.PartesIPServLoc[1]+"."+Comun.PartesIPServLoc[2]+".255");		
	        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length, ipBroadcast, Comun.PuertoBroadcast);
	        enchufeServidor.send(datagrama);	     
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean recibeRespuestaDescubrimiento(int timeout){
		DatagramSocket enchufeServidor1 = null;
		try {
			enchufeServidor.close();
			enchufeServidor=null;
			enchufeServidor1 = new DatagramSocket(Comun.PuertoServidorLocal);
			enchufeServidor1.setSoTimeout(timeout);
		  	byte[] bufferRecepcion = new byte[1024];
		  	DatagramPacket paqueteRecibido = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
	  	
		  	enchufeServidor1.receive(paqueteRecibido);  		
			System.out.println("recibido");
			byte[] direccionIPdispositivo = paqueteRecibido.getAddress().getAddress();
			byte[] datosRecibidos = paqueteRecibido.getData();
			UnicodeFormater.byteArrayToHexPantalla(datosRecibidos,false);
			byte[] direccionMac = new byte[6];
			direccionMac[0]=datosRecibidos[63];
			direccionMac[1]=datosRecibidos[62];
			direccionMac[2]=datosRecibidos[61];
			direccionMac[3]=datosRecibidos[60];
			direccionMac[4]=datosRecibidos[59];
			direccionMac[5]=datosRecibidos[58];
			System.out.println(new String(direccionMac));
			if(esHangZhou(direccionMac)){
				SmartPlug smartPlug = new SmartPlug(direccionIPdispositivo,direccionMac);
				listaDispositivos.add(smartPlug);
				return true;
			}			
			else{
				return false;
			}
		}catch (SocketTimeoutException e) {
			System.out.println("timeout recepcion");
			if (enchufeServidor != null) {
	            enchufeServidor.close();
	        }
	    }catch (SocketException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
	        if (enchufeServidor1 != null) {
	        	enchufeServidor1.close();
	        }	     	   
		}
		return false;
	}	
	
//	private boolean esBroadLink(byte[] mac) { 
//		return (mac[0]==0xB4 && mac[1]==0x43 && mac[2]==0x0D);
//	}
	
	private boolean esHangZhou(byte[] mac) { 
		return (mac[0]==0x34 && mac[1]==(byte)0xEA && mac[2]==0x34);
	}
}
