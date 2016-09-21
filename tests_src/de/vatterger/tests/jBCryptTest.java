package de.vatterger.tests;

import org.mindrot.jbcrypt.BCrypt;

import de.vatterger.engine.util.Profiler;

public class jBCryptTest {
	public static void main(String[] args) {

		Profiler p = new Profiler("salt");

		String salt = BCrypt.gensalt(10);

		p.log();
		
		p = new Profiler("hash");

		String hash = BCrypt.hashpw("password", salt);
		
		p.log();

		p = new Profiler("Password check");
		
		System.out.println("salt: "+salt);
		System.out.println("hash: "+hash);
		System.out.println("match: "+BCrypt.checkpw("password", hash));
		
		p.log();
	}
}
