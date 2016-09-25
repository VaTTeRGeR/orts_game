package de.vatterger.tests;

import de.vatterger.game.systems.network.util.ClientUtil;

public class UserNameCanonicalPathTest {

	public static void main(String[] args) {
		System.out.println("bert.avi path: " + ClientUtil.getCanonicalPathOfUser("bert.avi"));
		System.out.println("bert path: " + ClientUtil.getCanonicalPathOfUser("bert"));
		System.out.println("'' path: " + ClientUtil.getCanonicalPathOfUser(""));
		System.out.println("' ' path: " + ClientUtil.getCanonicalPathOfUser(" "));
	}

}
