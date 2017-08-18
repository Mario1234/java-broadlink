package com.acciones.configurador;
import com.comu.comun.UnicodeFormater;
import com.comu.comun.Comun.ModoSeguridadWifi;

public class PruebaConfigurador {

	public static void main(String[] args) {
		ConfiguradorSmartPlug confSmaPl = new ConfiguradorSmartPlug();
		byte[] mensaje = confSmaPl.configuraSmartPlug("azucarito","123456788", ModoSeguridadWifi.WPA2);
		System.out.println("----------Configurando-----------");
		UnicodeFormater.byteArrayToHexPantalla(mensaje,false);
		for(int i=0; i<1;i++){
			confSmaPl.enviaBroadCast(mensaje);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}

}
