package de.vatterger.tests;

import de.vatterger.game.systems.network.ClientUtil;

public class UserNameValidTest {

	public static void main(String[] args) {
		System.out.println("bert.avi valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser("bert.avi")));
		System.out.println("bert valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser("bert")));
		System.out.println("bert< valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser("bert<")));
		System.out.println("'' valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser("")));
		System.out.println("' ' valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser(" ")));
		System.out.println("null valid: " + ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser(null)));
	}

}
