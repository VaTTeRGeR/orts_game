package de.vatterger.game.systems.network;

import org.mindrot.jbcrypt.BCrypt;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.engine.util.PropertiesHandler;
import de.vatterger.game.components.client.ClientDeclined;
import de.vatterger.game.components.client.ClientLoggedIn;
import de.vatterger.game.components.client.ClientRegistered;
import de.vatterger.game.components.client.CreatePassword;
import de.vatterger.game.components.client.Name;
import de.vatterger.game.systems.network.util.ClientUtil;

public class CreatePasswordSystem extends IteratingSystem {
	
	ComponentMapper<Name> nm;
	ComponentMapper<CreatePassword> crpm;
	ComponentMapper<ClientLoggedIn> clim;
	ComponentMapper<ClientRegistered> clrm;
	
	@SuppressWarnings("unchecked")
	public CreatePasswordSystem() {
		super(Aspect.all(Name.class, CreatePassword.class).exclude(ClientDeclined.class));
	}

	@Override
	protected void begin() {
		PropertiesHandler.clearCache();
	}

	@Override
	protected void process(int e) {
		String name = nm.get(e).name;
		
		String password = crpm.get(e).pw;

		System.out.println("handling registration of user "+name);

		if(!ClientUtil.isClientInDatabase(name) && ClientUtil.sanityCheckNamePassword(name, password)) {
			PropertiesHandler handler = new PropertiesHandler(ClientUtil.getRelativePathOfUser(name));

			String saltedHashedPW = BCrypt.hashpw(password, BCrypt.gensalt(7));

			System.out.println("creating user "+name+" in db with password: " + password);

			handler.setString("shpw", saltedHashedPW);
			handler.save("Salted and Hashed password");
			
			ClientUtil.setLoggedIn(world, e);
			ClientUtil.setRegistered(world, e);
			ClientUtil.setPasswordCreated(world, e);
		} else {
			System.out.println("user "+name+" was declined registration");
			ClientUtil.setDeclined(world, e);
		}
		System.out.println();
	}
}
