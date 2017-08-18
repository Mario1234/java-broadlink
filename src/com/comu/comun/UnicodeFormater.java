package com.comu.comun;

public class UnicodeFormater {
	static public String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
							'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}
 
	static public String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}
	
	static public void byteArrayToHexPantalla(byte[] barray, boolean macStyle){
		for(byte b : barray){
			String saux = UnicodeFormater.byteToHex(b);
			if(macStyle){
				saux+=":";
			}
			System.out.print(saux);
		}
		System.out.println();
	}
	
	static public String fromByteArrayToStringIP(byte[] ip){
		String s_ip = "";
		int i_aux=0;
		for(byte b : ip){
			i_aux=b;
			if(b<0){
				i_aux=256+b;
			}
			s_ip += i_aux+".";
		}
		return s_ip;
	}
}