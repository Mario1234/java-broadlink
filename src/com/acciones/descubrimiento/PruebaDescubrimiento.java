package com.acciones.descubrimiento;

import java.util.Set;

import com.acciones.control.SmartPlug;

public class PruebaDescubrimiento {

	public static void main(String[] args) {
		Descubrimiento des = new Descubrimiento();
		int i=0;
		boolean encontrado = false;
		while(i<10 && !encontrado){
			encontrado=des.descubreDispositivos(10000);			
			i++;
		}
		if(encontrado){
			Set<SmartPlug> lista = des.dameListaDispositivos();
			for(SmartPlug disp: lista){
				disp.muestraDatosPantalla();
			}
		}
		else{
			System.out.println("no hay plug");
		}
	}

}
