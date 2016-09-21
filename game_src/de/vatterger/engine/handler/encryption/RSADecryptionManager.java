package de.vatterger.engine.handler.encryption;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class RSADecryptionManager {
	
	private static KeyPair keyPair = null;
	private static Cipher cipher = null;
	
	static {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new IllegalStateException("KeyPairGenerator not available.");
		}
		
		keyPairGenerator.initialize(1024);
		keyPair = keyPairGenerator.generateKeyPair();
		
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("RSA/ECB/PKCS1Padding Cipher not available.");
		}
	}
	
	public static RSAPublicKey getRSAPublicKey() {
		return (RSAPublicKey)keyPair.getPublic();
	}
	
	public static String decryptString(byte[] input){
		try {
			return new String(decrypt(input),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decrypt(byte[] input) {
		try {
			return cipher.doFinal(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}