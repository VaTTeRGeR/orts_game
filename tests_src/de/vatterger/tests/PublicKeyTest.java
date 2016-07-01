package de.vatterger.tests;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import de.vatterger.engine.handler.encryption.RSAPublicKeyUtility;
import de.vatterger.engine.util.Profiler;

public class PublicKeyTest {
	public static void main(String[] args) {
		KeyPairGenerator keyPairGenerator;
		try {
			Profiler pro = new Profiler("t");
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);

			System.out.println("KeyPairGenerator creation");
			pro.log();
			System.out.println();
			pro.start();
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			System.out.println("KeyPair creation");
			pro.log();
			System.out.println();
			pro.start();
			
			String raw = "GEHEIM PW !?";

			
			Cipher c = Cipher.getInstance("RSA");

			System.out.println("Cipher creation");
			pro.log();
			System.out.println();
			pro.start();
			
			byte[] keyBytes = RSAPublicKeyUtility.toBytes((RSAPublicKey)keyPair.getPublic());
			
			System.out.println("RSAPublicKey to byte-array of length " + keyBytes.length);
			pro.log();
			System.out.println();
			pro.start();
			
			RSAPublicKey publicKey = RSAPublicKeyUtility.fromBytes(keyBytes);

			System.out.println("RSAPublicKey from byte-array");
			pro.log();
			System.out.println();
			pro.start();

			c.init(Cipher.ENCRYPT_MODE, publicKey);
			
			System.out.println("Cipher encrypt-init");
			pro.log();
			System.out.println();
			pro.start();

			System.out.println("RAW:       "+raw);
			System.out.println();
			
			byte[] encrypted = c.doFinal(raw.getBytes("UTF8"));

			System.out.println("encryption");
			pro.log();
			System.out.println();
			pro.start();
			
			System.out.println("ENCRYPTED: ");
			for (int i = 0; i < encrypted.length; i++) {
				System.out.print(encrypted[i]+" ");
			}
			System.out.println("\n");

			c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());

			System.out.println("Cipher decrypt-init");
			pro.log();
			System.out.println();
			pro.start();
			
			String decrypted = new String(c.doFinal(encrypted), "UTF8");
			
			System.out.println("decryption");
			pro.log();
			System.out.println();
			pro.start();

			System.out.println("DECRYPTED: " + decrypted);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
