package de.vatterger.game.systems.network;

import java.io.File;

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
	
	public static boolean isClientInDatabase(String name) {
		return new File("users/" + name).isFile();
	}
}
