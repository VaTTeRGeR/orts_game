package de.vatterger.game.systems.network;

import org.mindrot.jbcrypt.BCrypt;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.client.ChangePassword;
import de.vatterger.game.components.client.ClientDeclined;
import de.vatterger.game.components.client.ClientLoggedIn;
import de.vatterger.game.components.client.ClientRegistered;
import de.vatterger.game.components.client.Name;

public class ChangePasswordSystem extends IteratingSystem {
	
	ComponentMapper<Name> nm;
	ComponentMapper<ChangePassword> chpm;
	ComponentMapper<ClientLoggedIn> clim;
	ComponentMapper<ClientRegistered> clrm;
	
	@SuppressWarnings("unchecked")
	public ChangePasswordSystem() {
		super(Aspect.all(Name.class, ChangePassword.class).exclude(ClientDeclined.class));
	}

	@Override
	protected void process(int e) {
		String name = nm.get(e).name;
		
		String password = chpm.get(e).pw;

		System.out.println("Handling password change of user "+name);

		if(ClientUtil.isClientInDatabase(name) && ClientUtil.sanityCheckNamePassword(name, password)) {
			PropertiesHandler handler = new PropertiesHandler(ClientUtil.getRelativePathOfUser(name));
			
			String saltedHashedPW = BCrypt.hashpw(password, BCrypt.gensalt());

			System.out.println("Setting password of user "+name+": "+password);

			handler.setString("shpw", saltedHashedPW);
			handler.save("Salted and Hashed password");
			
			ClientUtil.setPasswordChanged(world, e);
		} else {
			System.out.println("User "+name+" was declined password change");
			ClientUtil.setDeclined(world, e);
		}
		System.out.println();
	}
}
