package test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

public class PublicKeyTest {
	public static void main(String[] args) {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			
			
			String raw = "GEHEIM PW !?";

			
			Cipher c = Cipher.getInstance("RSA");
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
