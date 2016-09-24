package de.vatterger.game.systems.network;

import org.mindrot.jbcrypt.BCrypt;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.client.CheckPassword;
import de.vatterger.game.components.client.ClientDeclined;
import de.vatterger.game.components.client.Name;

public class LoginClientSystem extends IteratingSystem {
	
	ComponentMapper<Name> nm;
	ComponentMapper<CheckPassword> chpm;
	
	@SuppressWarnings("unchecked")
	public LoginClientSystem() {
		super(Aspect.all(Name.class, CheckPassword.class).exclude(ClientDeclined.class));
	}

	@Override
	protected void begin() {
		PropertiesHandler.clearCache();
	}

	@Override
	protected void process(int e) {
		String name = nm.get(e).name;
		String password = chpm.get(e).pw;

		System.out.println("checking user "+name);
		
		if(ClientUtil.isClientInDatabase(name) && ClientUtil.sanityCheckNamePassword(name, password)) {
			PropertiesHandler handler = new PropertiesHandler(ClientUtil.getRelativePathOfUser(name));
			
			String saltedHashedPW = handler.getString("shpw", null);
			
			System.out.println("user "+name+" found in db");
			
			if(BCrypt.checkpw(password, saltedHashedPW)) {
				System.out.println("user "+name+" accepted with password: "+password);
				ClientUtil.setLoggedIn(world, e);
				ClientUtil.setPasswordChecked(world, e);
			} else {
				System.out.println("user "+name+" declined with password: "+password);
				ClientUtil.setNotLoggedIn(world, e);
				ClientUtil.setPasswordChecked(world, e);
			}
		} else {
			System.out.println("user "+name+" not found in db");
			ClientUtil.setDeclined(world, e);
		}
		System.out.println();
	}
}
