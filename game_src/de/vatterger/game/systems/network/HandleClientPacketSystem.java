package de.vatterger.game.systems.network;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;

import de.vatterger.engine.handler.encryption.RSADecryptionManager;
import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.engine.network.FilteredListener;
import de.vatterger.engine.network.KryoNetMessage;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.client.CheckPassword;
import de.vatterger.game.components.client.ClientDeclined;
import de.vatterger.game.components.client.ConnectionID;
import de.vatterger.game.components.client.CreatePassword;
import de.vatterger.game.components.client.Name;
import de.vatterger.game.packets.ChangeAccountPacket;
import de.vatterger.game.packets.CreateAccountPacket;
import de.vatterger.game.packets.LoginPacket;

public class HandleClientPacketSystem extends IteratingSystem {
	
	ServerNetworkHandler snh;
	FilteredListener<LoginPacket> loginListener = new FilteredListener<LoginPacket>(LoginPacket.class);
	FilteredListener<CreateAccountPacket> createListener = new FilteredListener<CreateAccountPacket>(CreateAccountPacket.class);
	FilteredListener<ChangeAccountPacket> changeListener = new FilteredListener<ChangeAccountPacket>(ChangeAccountPacket.class);
	
	private ComponentMapper<ConnectionID> ccm;

	@SuppressWarnings("unchecked")
	public HandleClientPacketSystem() {
		super(Aspect.all(ConnectionID.class).exclude(ClientDeclined.class));
	}
	
	@Override
	protected void initialize() {
		snh = ServerNetworkHandler.get(26005);
		snh.addListener(loginListener);
		snh.addListener(createListener);
		//snh.addListener(changeListener);
	}
	
	@Override
	protected void process(int e) {
		boolean print = false;
		Profiler p = new Profiler("processing messages of client "+e);
		KryoNetMessage<LoginPacket> knm;
		while((knm = loginListener.getNext(ccm.get(e).cid)) != null) {
			String name = RSADecryptionManager.decryptString(knm.getObject().nameEncrypted);
			String password = RSADecryptionManager.decryptString(knm.getObject().passwordEncrypted);
			world.edit(e).add(new Name(name)).add(new CheckPassword(password));
			System.out.println("Client "+ccm.get(e).cid+" wants to login name: "+name+" pw: "+password);
			print = true;
		}

		KryoNetMessage<CreateAccountPacket> craccpm;
		while((craccpm = createListener.getNext(ccm.get(e).cid)) != null) {
			String name = RSADecryptionManager.decryptString(craccpm.getObject().nameEncrypted);
			String password = RSADecryptionManager.decryptString(craccpm.getObject().passwordEncrypted);
			world.edit(e).add(new Name(name)).add(new CreatePassword(password));
			System.out.println("Client "+ccm.get(e).cid+" wants to create account name: "+name+" pw: "+password);
			print = true;
		}
		if(print)
			p.log();
	}
	
	@Override
	protected void end() {
		loginListener.clear();
		createListener.clear();
		//changeListener.clear();
	}

	@Override
	protected void dispose() {
		ServerNetworkHandler.dispose(26005);
	}
}