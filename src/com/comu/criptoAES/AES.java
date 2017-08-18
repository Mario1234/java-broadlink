package com.comu.criptoAES;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.comu.comun.UnicodeFormater;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
 
/**
 * @version 1.0
 * Clase que contiene los métodos encrypt y descrypt, cuyos objetivos son
 * encriptar y desencriptar respectivamente, utilizando los algoritmos y codificación
 * definidas en las variables estáticas alg y cI.
 * Requiere la librería Apache Commons Codec
 * @see <a href="http://commons.apache.org/proper/commons-codec/">Apache Commons Codec</a>
 * @see <a href="http://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html">javax.crypto Class Cipher</a>
 * @see <a href="http://es.wikipedia.org/wiki/Advanced_Encryption_Standard">WikiES: Advanced Encryption Standard</a>
 * @see <a href="http://es.wikipedia.org/wiki/Criptograf%C3%ADa">WikiES: Criptografía</a>
 * @see <a href="http://es.wikipedia.org/wiki/Vector_de_inicializaci%C3%B3n">WikiES: Vector de inicialización</a>
 * @see <a href="http://es.wikipedia.org/wiki/Cifrado_por_bloques">WikiES: Cifrado por bloques</a>
 * @see <a href="http://www.linkedin.com/in/jchinchilla">Julio Chinchilla</a>
 * @author Julio Chinchilla
 */
public class AES {
 
    // Definición del tipo de algoritmo a utilizar (AES, DES, RSA)
    private final static String alg = "AES";
    // Definición del modo de cifrado a utilizar
    private final static String cI = "AES/CBC/NoPadding";
 
    /**
     * Función de tipo String que recibe una llave (key), un vector de inicialización (iv)
     * y el texto que se desea cifrar
     * @param key la llave en tipo String a utilizar
     * @param iv el vector de inicialización a utilizar
     * @param cleartext el texto sin cifrar a encriptar
     * @return el texto cifrado en modo String
     * @throws Exception puede devolver excepciones de los siguientes tipos: NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException
     */
    public static String encrypt(String key, String iv, String cleartext) throws Exception {
            Cipher cipher = Cipher.getInstance(cI);
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), alg);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(cleartext.getBytes());
            return new String(encodeBase64(encrypted));
    }
 
    /**
     * Función de tipo String que recibe una llave (key), un vector de inicialización (iv)
     * y el texto que se desea descifrar
     * @param key la llave en tipo String a utilizar
     * @param iv el vector de inicialización a utilizar
     * @param encrypted el texto cifrado en modo String
     * @return el texto desencriptado en modo String
     * @throws Exception puede devolver excepciones de los siguientes tipos: NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException
     */
    public static String decrypt(String key, String iv, String encrypted) throws Exception {
            Cipher cipher = Cipher.getInstance(cI);
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), alg);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
            byte[] enc = decodeBase64(encrypted);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(enc);
            return new String(decrypted);
    }
    
    public static byte[] encrypt(byte[] key, byte[] iv, byte[] mensaje) throws Exception {
        Cipher cipher = Cipher.getInstance(cI);
        SecretKeySpec skeySpec = new SecretKeySpec(key, alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(mensaje);
        return encrypted;
//      return encodeBase64(encrypted);
    }
    
    public static byte[] decrypt(byte[] key, byte[] iv, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(cI);
        SecretKeySpec skeySpec = new SecretKeySpec(key, alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        //byte[] enc = decodeBase64(encrypted);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
    
    public static byte[] encrypt64(byte[] key, byte[] iv, byte[] mensaje) throws Exception {
        Cipher cipher = Cipher.getInstance(cI);
        SecretKeySpec skeySpec = new SecretKeySpec(key, alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(mensaje);
        return encodeBase64(encrypted);
    }
    
    public static byte[] decrypt64(byte[] key, byte[] iv, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(cI);
        SecretKeySpec skeySpec = new SecretKeySpec(key, alg);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        byte[] enc = decodeBase64(encrypted);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] decrypted = cipher.doFinal(enc);
        return decrypted;
    }
 
    /**
     * Clase que consume los métodos de la clase StringEncrypt
     * @author Julio Chinchilla
     */
     
     public static void main(String[] args) throws Exception {
	     String key = "92AE31A79FEEB2A3"; //llave
	     String iv = "0123456789ABCDEF"; // vector de inicialización	     
	     String cleartext = "hola           0";//16 bytes minimo
	     System.out.println("Texto encriptado: "+encrypt(key, iv,cleartext));
	     System.out.println("Texto desencriptado: "+decrypt(key, iv,encrypt(key, iv,cleartext)));
	     byte[] mensajeSinEncript = {2,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	     byte[] keyAES = {0x03, (byte)0xc1, 0x69, 0x3d, 0x77, (byte)0x93, 0x1d, (byte)0x99, 0x2b, 0x45, (byte)0xc7, (byte)0xb7, (byte)0xb1, (byte)0xc3, 0x0b, 0x6d};
	 	 byte[] ivAES = {0x56, 0x2e, 0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28, (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58};
	 	 byte[] trozo1 = new byte[16];
	 	 byte[] trozo2 = new byte[16];
	 	 for(int i=0;i<16;i++){
	 		 trozo1[i]=mensajeSinEncript[i];
	 	 }
	 	for(int i=0;i<16;i++){
	 		 trozo2[i]=mensajeSinEncript[i+16];
	 	 }
	 	 UnicodeFormater.byteArrayToHexPantalla(encrypt(keyAES,ivAES,trozo1),false);
	 	 UnicodeFormater.byteArrayToHexPantalla(encrypt(keyAES,ivAES,trozo2),false);
	 	 UnicodeFormater.byteArrayToHexPantalla(encrypt(keyAES,ivAES,mensajeSinEncript),false);
     }
}
