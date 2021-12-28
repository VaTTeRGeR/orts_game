package de.vatterger.engine.handler.encryption;

import java.io.UnsupportedEncodingException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class RSAEncryptionManager {
	
	static ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<Cipher>() {
		@Override
		protected Cipher initialValue() {
			try {
				return Cipher.getInstance("RSA/CBC/PKCS1Padding");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	};
	
	public static byte[] encrypt(byte[] input, RSAPublicKey rsaPublicKey) {
		if(rsaPublicKey == null)
			throw new IllegalStateException("No RSAPublicKey specified.");
		if(input == null)
			throw new IllegalStateException("input is null");
		if(input.length > 117)
			throw new IllegalStateException("input is larger than 117 bytes");

		try {
			Cipher cipher = cipherThreadLocal.get();
			cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
			return cipher.doFinal(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static byte[] encryptString(String string, RSAPublicKey rsaPublicKey) {
		try {
			return encrypt(string.getBytes("UTF-8"), rsaPublicKey);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
