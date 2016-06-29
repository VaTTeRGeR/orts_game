package de.vatterger.tests;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

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
			
			String keyString = ((RSAPublicKey)keyPair.getPublic()).getModulus().toString() + "x" +
			((RSAPublicKey)keyPair.getPublic()).getPublicExponent().toString();
			
			System.out.println("RSAPublicKey to String of length "+keyString.length());
			System.out.println("RSAPublicKey: "+keyString);
			pro.log();
			System.out.println();
			pro.start();
			
			String [] Parts = keyString.split("x");     
			RSAPublicKeySpec Spec = new RSAPublicKeySpec(
			        new BigInteger(Parts[0]),
			        new BigInteger(Parts[1]));
			RSAPublicKey publicKey = (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(Spec);			

			System.out.println("RSAPublicKey from String");
			pro.log();
			System.out.println();
			pro.start();

			c.init(Cipher.ENCRYPT_MODE, publicKey);
			
			System.out.println("Cipher encrypt-init");
			pro.log();
			System.out.println();
			pro.start();

			System.out.println("RAW:       "+raw);
			
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
