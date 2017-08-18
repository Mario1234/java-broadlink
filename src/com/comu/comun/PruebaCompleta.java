package com.comu.comun;

import java.util.Set;

import com.acciones.configurador.ConfiguradorSmartPlug;
import com.acciones.control.SmartPlug;
import com.acciones.descubrimiento.Descubrimiento;
import com.comu.comun.Comun.ModoSeguridadWifi;

public class PruebaCompleta {

	public static void main(String[] args) {
		//crear una wifi azucarito con zona wifi del movil, contrasenia 123456789
		//pulsar mantenido dos veces el boton reset del plug
		//conectar este pc a BroadlinProv
//		ConfiguradorSmartPlug confSmaPl = new ConfiguradorSmartPlug();
//		byte[] mensaje = confSmaPl.configuraSmartPlug("azucarito","123456789", ModoSeguridadWifi.WPA2);
//		System.out.println("----------Configurando-----------");
//		UnicodeFormater.byteArrayToHex(mensaje);
//		for(int i=0; i<20;i++){
//			confSmaPl.enviaBroadCast(mensaje);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		//descubrir los dispositivos en azucarito
		System.out.println("----------Descubriendo-----------");
		Descubrimiento des = new Descubrimiento();
		int i=0;
		boolean encontrado = false;
		while(i<10 && !encontrado){
			encontrado=des.descubreDispositivos(10000);			
			i++;
		}
		
		//pedir clave AES
		System.out.println("----------Pidiendo clave AES-----------");
		Set<SmartPlug> listaDisp = des.dameListaDispositivos();
		for(SmartPlug smar: listaDisp){
			smar.consigueClaveAES(8000);
		}		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//probar a encender y apagar los dispositivos		
		System.out.println("----------Prueba control-----------");
		listaDisp = des.dameListaDispositivos();
		for(SmartPlug smar: listaDisp){
			if(smar.isClaveAESRecibida()){
				smar.muestraAESPantalla();
				smar.InterruptorRemoto(true,8000);
			}			
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(SmartPlug smar: listaDisp){
			if(smar.isClaveAESRecibida()){
				smar.InterruptorRemoto(false,8000);
			}	
		}
		
	}

}
