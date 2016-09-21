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
	ComponentMapper<CheckPassword> cpm;
	
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
		System.out.println("checking user "+nm.get(e).name);
		
		if(ClientUtil.isClientInDatabase(nm.get(e).name)) {
			PropertiesHandler handler = new PropertiesHandler("users/" + nm.get(e).name);
			
			String saltedHashedPW = handler.getString("shpw", null);
			String plainPW = cpm.get(e).pw;

			System.out.println("user "+nm.get(e).name+" found in db");
			
			if(BCrypt.checkpw(plainPW, saltedHashedPW)) {
				System.out.println("user "+nm.get(e).name+" accepted with password: "+plainPW);
				ClientUtil.setLoggedIn(world, e);
				ClientUtil.setPasswordChecked(world, e);
			} else {
				System.out.println("user "+nm.get(e).name+" declined with password: "+plainPW);
				ClientUtil.setNotLoggedIn(world, e);
				ClientUtil.setPasswordChecked(world, e);
			}
		} else {
			System.out.println("user "+nm.get(e).name+" not found in db");
			ClientUtil.setDeclined(world, e);
		}
	}
}
