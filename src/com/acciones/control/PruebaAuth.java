package com.acciones.control;

public class PruebaAuth {
	public static void main(String[] args) {
		byte[] dirIp = new byte[4];
		byte[] dirM = new byte[6];
		dirIp[0]=(byte) 192;
		dirIp[1]=(byte) 168;
		dirIp[2]=43;
		dirIp[3]=(byte) 234;
		
		dirM[0]=0x34;//34:EA:34:F1:4B:2D 
		dirM[1]=(byte) 0xEA;
		dirM[2]=0x34;
		dirM[3]=(byte) 0xF1;
		dirM[4]=0x4B;
		dirM[5]=0x2D;
		
		SmartPlug plugaux = new SmartPlug(dirIp, dirM);
		System.out.println("Dispositivo: "+0);
		plugaux.muestraDatosPantalla();
		plugaux.consigueClaveAES(8000);
		if(plugaux.isClaveAESRecibida()){
			plugaux.muestraAESPantalla();
		}
	}
}
