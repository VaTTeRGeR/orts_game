package de.vatterger.engine.handler.encryption;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public class RSAPublicKeyUtility {
	public static RSAPublicKey fromString(String MODxEXP) {
		String [] Parts = MODxEXP.split("x");     

		RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(new BigInteger(Parts[0]), new BigInteger(Parts[1]));
		
		try {
			return (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String toString(RSAPublicKey rsaPublicKey) {
		if(rsaPublicKey != null) {
			return rsaPublicKey.getModulus().toString() + "x" + rsaPublicKey.getPublicExponent().toString();
		} else {
			return null;
		}
	}
}
