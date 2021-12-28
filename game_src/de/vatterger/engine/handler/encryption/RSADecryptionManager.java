package de.vatterger.engine.handler.encryption;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class RSADecryptionManager {
	
	private static KeyPair keyPair = null;

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
	}
	
	static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>() {
		@Override
		protected Cipher initialValue() {
			try {
				Cipher cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding");
				cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
				return cipher;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	};
	
	public static RSAPublicKey getRSAPublicKey() {
		return (RSAPublicKey)keyPair.getPublic();
	}
	
	public static String decryptString(byte[] input){
		try {
			return new String(decrypt(input),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decrypt(byte[] input) {
		try {
			return cipherThreadLocal.get().doFinal(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
