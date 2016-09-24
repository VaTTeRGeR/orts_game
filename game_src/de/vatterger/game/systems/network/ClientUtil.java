package de.vatterger.game.systems.network;

import java.io.File;
import java.io.IOException;

import com.artemis.World;

import de.vatterger.game.components.client.ChangePassword;
import de.vatterger.game.components.client.CheckPassword;
import de.vatterger.game.components.client.ClientDeclined;
import de.vatterger.game.components.client.ClientLoggedIn;
import de.vatterger.game.components.client.ClientNotLoggedIn;
import de.vatterger.game.components.client.ClientNotRegistered;
import de.vatterger.game.components.client.ClientRegistered;
import de.vatterger.game.components.client.CreatePassword;

public class ClientUtil {
	public static void setLoggedIn(World world, int e) {
		world.edit(e).
		add(new ClientLoggedIn()).
		remove(ClientNotLoggedIn.class);
	}

	public static void setNotLoggedIn(World world, int e) {
		world.edit(e).
		add(new ClientNotLoggedIn()).
		remove(ClientLoggedIn.class);
	}

	public static void setRegistered(World world, int e) {
		world.edit(e).
		add(new ClientRegistered()).
		remove(ClientNotRegistered.class);
	}

	public static void setNotRegistered(World world, int e) {
		world.edit(e).
		add(new ClientNotRegistered()).
		remove(ClientRegistered.class);
	}
	
	public static void setDeclined(World world, int e) {
		world.edit(e).
		add(new ClientDeclined()).
		add(new ClientNotRegistered()).
		add(new ClientNotLoggedIn()).
		remove(ClientRegistered.class).
		remove(ClientLoggedIn.class);
	}

	public static void setPasswordChecked(World world, int e) {
		world.edit(e).
		remove(CheckPassword.class);
	}
	
	public static void setPasswordChanged(World world, int e) {
		world.edit(e).
		remove(ChangePassword.class);
	}
	
	public static void setPasswordCreated(World world, int e) {
		world.edit(e).
		remove(CreatePassword.class);
	}
	
	public static boolean sanityCheckNamePassword(String name, String pw){
		if(name == null) return false;
		if(name.length() < 1) return false;
		if(name.length() > 32) return false;

		if(!ClientUtil.isFilePathValid(ClientUtil.getRelativePathOfUser(name))) return false;
		
		if(pw == null) return false;
		if(pw.length() < 1) return false;
		if(pw.length() > 32) return false;
		
		return true;
	}
	
	public static boolean isClientInDatabase(String name) {
		return new File(ClientUtil.getRelativePathOfUser(name)).isFile();
	}
	
	public static boolean removeClientFromDatabase(String name) {
		if(isClientInDatabase(name))
			new File(getRelativePathOfUser(name)).delete();
		return !isClientInDatabase(name);
	}

	public static boolean isFilePathValid(String path) {
		File file = new File(path);

		boolean success = false;
		try {
			success = file.createNewFile();
		} catch (IOException e) {}
		
		if(success) {
			file.delete();
		}
		
		return success || file.isFile();
	}
	
	public static String getRelativePathOfUser(String name) {
		if(name != null)
			return "users/"+name;
		else
			return null;
	}
	
	public static String getCanonicalPathOfUser(String name) {
		if(name != null && isFilePathValid(getRelativePathOfUser(name)))
			try {
				return new File(getRelativePathOfUser(name)).getCanonicalPath();
			} catch (IOException e) {}
		return null;
	}
}
