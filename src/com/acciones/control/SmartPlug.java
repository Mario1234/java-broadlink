package com.acciones.control;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.comu.comun.Comun;
import com.comu.comun.UnicodeFormater;
import com.comu.criptoAES.AES;


//nextInt is normally exclusive of the top value,
//so add 1 to make it inclusive


public class SmartPlug implements Comparable<SmartPlug>{
	private final int MAXIMO = 0xffff;
	private byte[] keyAES1 = {0x09, 0x76, 0x28, 0x34, 0x3f, (byte)0xe9, (byte)0x9e, 0x23, 0x76, 0x5c, 0x15, 0x13, (byte)0xac, (byte)0xcf, (byte)0x8b, 0x02};
	private byte[] keyAES = {0x03, (byte)0xc1, 0x69, 0x3d, 0x77, (byte)0x93, 0x1d, (byte)0x99, 0x2b, 0x45, (byte)0xc7, (byte)0xb7, (byte)0xb1, (byte)0xc3, 0x0b, 0x6d};
	private final byte[] ivAES = {0x56, 0x2e, 0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58};
	private byte[] id = {0x00, 0x00, 0x00, 0x00};
	private byte[] dirIP;
	private byte[] dirMAC;
	private byte[] mensaje;
	private int i_numAleatorio;
	private boolean claveAESRecibida;
	private DatagramSocket enchufeServidor;
	
	public SmartPlug(byte[] dIp, byte[] dirm) {
		Comun.dameDirIp();
		dirIP=dIp;
		dirMAC=dirm;		
		i_numAleatorio = ThreadLocalRandom.current().nextInt(0, MAXIMO + 1);	
		setClaveAESRecibida(false);
	}
	
	public void muestraDatosPantalla() {
		
		System.out.print("  ip: ");
		System.out.println(UnicodeFormater.fromByteArrayToStringIP(dirIP));
		System.out.print("  mac: ");
		UnicodeFormater.byteArrayToHexPantalla(dirMAC,true);
	}
	
	public void consigueClaveAES(int timeout){
		mensaje = new byte[96];
		mensaje[4] = 0x31;
		mensaje[5] = 0x31;
		mensaje[6] = 0x31;
		mensaje[7] = 0x31;
		mensaje[8] = 0x31;
		mensaje[9] = 0x31;
		mensaje[10] = 0x31;
		mensaje[11] = 0x31;
		mensaje[12] = 0x31;
		mensaje[13] = 0x31;
		mensaje[14] = 0x31;
		mensaje[15] = 0x31;
		mensaje[16] = 0x31;
		mensaje[17] = 0x31;
		mensaje[18] = 0x31;
		mensaje[30] = 0x01;
		mensaje[45] = 0x01;
		mensaje[48] = 'T';
		mensaje[49] = 'e';
		mensaje[50] = 's';
		mensaje[51] = 't';
		mensaje[52] = ' ';
		mensaje[53] = ' ';
		mensaje[54] = '1';
		byte[] encriptado=new byte[96];
		try {
//			for(int i=0;i<(mensaje.length/16);i++){
//				byte[] trozoMens = new byte[16];
//				System.arraycopy(mensaje, i*16, trozoMens, 0, 16);
//				byte[] trozoEncript = AES.encrypt(keyAES, ivAES, trozoMens);
//				System.arraycopy(trozoEncript, 0, encriptado, i*16, 16);
//			}			
			encriptado = AES.encrypt(keyAES1, ivAES, mensaje);
			byte[] paqueteMensaje = creaPaqueteMensaje((byte)0x65, encriptado);
			UnicodeFormater.byteArrayToHexPantalla(mensaje,false);
			UnicodeFormater.byteArrayToHexPantalla(paqueteMensaje,false);
			
			DatagramPacket paqueteRecibido = null;
			long milisdespuesTimeout = System.currentTimeMillis() + timeout;
			while(!claveAESRecibida && System.currentTimeMillis()<milisdespuesTimeout){
				enviaDatagrama(paqueteMensaje);
				paqueteRecibido = reciveDatagrama(timeout/4);
				if(paqueteRecibido!=null && paqueteRecibido.getAddress()!=null && paqueteRecibido.getAddress().getAddress()!=null){
					System.out.println("clave aes recibida");					
					byte[] datosRecibidos = paqueteRecibido.getData();
					byte[] encriptadoRecibido = new byte[96];
					Arrays.fill(encriptadoRecibido, (byte)0x00);
					System.arraycopy(datosRecibidos,56,encriptadoRecibido,0,96);					
					byte[] desencriptadoRecibido = AES.decrypt(keyAES1, ivAES, encriptadoRecibido);
					System.out.println("mensaje desencriptado");			
					UnicodeFormater.byteArrayToHexPantalla(desencriptadoRecibido,false);
					int indiceDatRec = 0;
					for(int i=0;i<keyAES.length;i++){//clave de 16 bytes
						indiceDatRec = i+4;
						if(indiceDatRec<desencriptadoRecibido.length){
							keyAES[i] = desencriptadoRecibido[indiceDatRec];
						}				
					}		
					for(int i=0;i<id.length;i++){//id de 4 bytes
						if(i<desencriptadoRecibido.length){
							id[i] = desencriptadoRecibido[i];
						}				
					}
					setClaveAESRecibida(true);
				}
			}				
			
		}catch (Exception e) {
				e.printStackTrace();
		}
	}
	
	public void InterruptorRemoto(boolean enciende, int timeout){
		byte by_enciende = 0;
		if(enciende){
			by_enciende=1;
		}
		mensaje = new byte[32];
		mensaje[0]=2;//o i_enciende
		mensaje[4]=by_enciende;
		id[3]=1;
		byte[] encriptado;
		try {
			encriptado = AES.encrypt(keyAES, ivAES, mensaje);
			byte[] paqueteMensaje = creaPaqueteMensaje((byte)0x6a, encriptado);//o 0x66
			UnicodeFormater.byteArrayToHexPantalla(mensaje,false);
			UnicodeFormater.byteArrayToHexPantalla(paqueteMensaje,false);
			
			boolean recibido = false;
			long milisdespuesTimeout = System.currentTimeMillis() + timeout;
			while(!recibido && System.currentTimeMillis()<milisdespuesTimeout){
				enviaDatagrama(paqueteMensaje);
				DatagramPacket paqueteRecibido = reciveDatagrama(timeout/4);
				if(paqueteRecibido!=null && paqueteRecibido.getAddress()!=null && paqueteRecibido.getAddress().getAddress()!=null){
					byte[] direccionIPdispositivo = paqueteRecibido.getAddress().getAddress();
	//				byte[] datosRecibidos = paqueteRecibido.getData();
					String s_dirIp= new String(dirIP);
					String s_dirIpRespuesta = new String(direccionIPdispositivo);
					if(s_dirIp.equalsIgnoreCase(s_dirIpRespuesta)){
						recibido = true;
					}
				}		
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}	

	@Override
	public int compareTo(SmartPlug arg0) {
		if(this.dirMAC[3]<arg0.dirMAC[3]){
			return -1;
		}
		else if(this.dirMAC[3]==arg0.dirMAC[3]){
			if(this.dirMAC[4]<arg0.dirMAC[4]){
				return -1;
			}
			else if(this.dirMAC[4]==arg0.dirMAC[4]){
				if(this.dirMAC[5]<arg0.dirMAC[5]){
					return -1;
				}
				else if(this.dirMAC[5]==arg0.dirMAC[5]){
					return 0;
				}
			}
		}	
		return 1;
	}	
	
	private byte[] creaPaqueteMensaje(byte comando, byte[] encriptado){
		i_numAleatorio++;
		i_numAleatorio&=0xffff;
		
		byte[] paqueteMensaje = new byte[56+encriptado.length];//del 56 al 71 va el mensaje encriptado dentro, 16bytes
		paqueteMensaje[0] = 0x5a;
		paqueteMensaje[1] = (byte) 0xa5;
		paqueteMensaje[2] = (byte) 0xaa;
		paqueteMensaje[3] = 0x55;
		paqueteMensaje[4] = 0x5a;
		paqueteMensaje[5] = (byte) 0xa5;
		paqueteMensaje[6] = (byte) 0xaa;
		paqueteMensaje[7] = 0x55;
		paqueteMensaje[36] = 0x2a;
		paqueteMensaje[37] = 0x27;
		paqueteMensaje[38] = comando;
		paqueteMensaje[40] = (byte) (i_numAleatorio & 0x0ff);
		paqueteMensaje[41] = (byte) (i_numAleatorio >> 8);
		paqueteMensaje[42] = dirMAC[5];
		paqueteMensaje[43] = dirMAC[4];
		paqueteMensaje[44] = dirMAC[3];
		paqueteMensaje[45] = dirMAC[2];
		paqueteMensaje[46] = dirMAC[1];
		paqueteMensaje[47] = dirMAC[0];
	    paqueteMensaje[48] = id[3];
	    paqueteMensaje[49] = id[2];
	    paqueteMensaje[50] = id[1];
	    paqueteMensaje[51] = id[0];
	    
	    int checksuMensaje = 0xbeaf;
	    for(int i=0;i<mensaje.length;i++){
	    	checksuMensaje += (mensaje[i]& 0x0ff);
	    	checksuMensaje &= 0xffff;
	    }	    
		
	    paqueteMensaje[52] = (byte) (checksuMensaje & 0x0ff);
	    paqueteMensaje[53] = (byte) (checksuMensaje >> 8);

	    int iniMensaje = 56;
	    for(int i=0;i<encriptado.length;i++){
	    	paqueteMensaje[iniMensaje+i]=encriptado[i];
	    }
	    int checksumPaquete = 0xbeaf;
	    for(int i=0;i<paqueteMensaje.length;i++){
	    	checksumPaquete += (paqueteMensaje[i]& 0x0ff);
	    	checksumPaquete &= 0xffff;
	    }
	    
	    paqueteMensaje[32] = (byte) (checksumPaquete & 0x0ff);
	    paqueteMensaje[33] = (byte) (checksumPaquete >> 8);
	    
	    return paqueteMensaje;
	}
	
	private void enviaDatagrama(byte[] paqueteMensaje){
		try {
//			Comun.dameDirIp();
			InetAddress inetMiIp = InetAddress.getByAddress(Comun.DirIpServidorLocal);
			InetAddress ipDispositivo = InetAddress.getByAddress(dirIP);
			enchufeServidor = new DatagramSocket(null);
			enchufeServidor.setReuseAddress(true);
		  	InetSocketAddress address = new InetSocketAddress(inetMiIp, Comun.PuertoServidorLocal);
		  	enchufeServidor.bind(address);	
	        DatagramPacket datagrama = new DatagramPacket(paqueteMensaje, paqueteMensaje.length, ipDispositivo, Comun.PuertoBroadcast);	        
	        enchufeServidor.send(datagrama);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private DatagramPacket reciveDatagrama(int timeout){
		DatagramPacket paqueteRecibido = null;
		try {				
			enchufeServidor.setSoTimeout(timeout);
		  	byte[] bufferRecepcion = new byte[1024];
		  	paqueteRecibido = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
			enchufeServidor.receive(paqueteRecibido);  	
		}catch (SocketTimeoutException e) {
			System.out.println("timeout recepcion");
	    }catch (IOException e) {
			e.printStackTrace();
		}finally {
	        if (enchufeServidor != null) {
	            enchufeServidor.close();
	        }
		}
		return paqueteRecibido;
	}
	
	public boolean isClaveAESRecibida() {
		return claveAESRecibida;
	}

	public void setClaveAESRecibida(boolean claveAESRecibida) {
		this.claveAESRecibida = claveAESRecibida;
	}
	
	public void muestraAESPantalla() {
		System.out.println("KeyAES");	
		UnicodeFormater.byteArrayToHexPantalla(keyAES,false);		
		System.out.println("ivAES");	
		UnicodeFormater.byteArrayToHexPantalla(ivAES,false);	
		System.out.println("id");	
		UnicodeFormater.byteArrayToHexPantalla(id,false);		
	}
	
}
