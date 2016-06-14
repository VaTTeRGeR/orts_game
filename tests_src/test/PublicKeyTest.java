package test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

public class PublicKeyTest {
	public static void main(String[] args) {
		KeyPairGenerator keyGenerator;
		try {
			keyGenerator = KeyPairGenerator.getInstance("RSA");
			KeyPair keyPair = keyGenerator.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
			System.out.println("Public  RSA-KEY: "+publicKey.getModulus()+", "+publicKey.getPublicExponent());
			System.out.println("Private RSA-KEY: "+privateKey.getModulus()+", "+privateKey.getPrivateExponent());
			
			Cipher c = Cipher.getInstance("RSA");
			
			String raw = "GEHEIM PW !?";

			
			c.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			
			System.out.println("RAW:       "+raw);
			
			byte[] encrypted = c.doFinal(raw.getBytes("UTF8"));

			System.out.print("ENCRYPTED: ");
			for (int i = 0; i < encrypted.length; i++) {
				System.out.print(encrypted[i]+" ");
			}
			System.out.print("\n");

			c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			
			String decrypted = new String(c.doFinal(encrypted),"UTF8");

			System.out.println("DECRYPTED: "+decrypted);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
