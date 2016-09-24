package de.vatterger.tests;

import de.vatterger.engine.handler.encryption.RSADecryptionManager;
import de.vatterger.engine.handler.encryption.RSAEncryptionManager;

public class RSAUtilityThreadingTest {
	public static void main(String[] args) {
		StringBuilder builder = new StringBuilder("0");
		for (int i = 1; i < 117; i++) {
			builder.append(i%10 == 0 ? "-" : i%10);
		}
		
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(builder.toString());
					byte[] encrypted = RSAEncryptionManager.encryptString(builder.toString(),RSADecryptionManager.getRSAPublicKey());
					System.out.println("Encrypted length: "+encrypted.length);
					String decrypted = RSADecryptionManager.decryptString(encrypted);
					System.out.println(decrypted);
				}
			}).start();
		}
	}
}