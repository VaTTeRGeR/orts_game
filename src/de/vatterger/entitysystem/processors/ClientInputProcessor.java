package de.vatterger.entitysystem.processors;

import java.util.HashMap;

import com.artemis.annotations.Wire;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.shared.ViewFrustum;
import de.vatterger.entitysystem.netservice.MessageRemote;
import de.vatterger.entitysystem.netservice.ServerNetworkService;
import de.vatterger.entitysystem.networkmessages.ClientViewportUpdate;

@Wire
public class ClientInputProcessor extends EntityProcessingSystem {

	private ComponentMapper<ViewFrustum> vfm;
	private ComponentMapper<KryoConnection> ccm;
	
	private HashMap<Connection, ClientViewportUpdate> updates = new HashMap<Connection, ClientViewportUpdate>();

	private ServerNetworkService nws = ServerNetworkService.instance();

	@SuppressWarnings("unchecked")
	public ClientInputProcessor() {
		super(Aspect.getAspectForAll(ViewFrustum.class, KryoConnection.class));
	}

	@Override
	protected void initialize() {
	}
	
	@Override
	protected void inserted(Entity e) {
		updates.put(ccm.get(e).connection, new ClientViewportUpdate(vfm.get(e).rect));
	}
	
	@Override
	protected void removed(Entity e) {
		updates.remove(ccm.get(e).connection);
	}
	
	@Override
	protected void begin() {
		MessageRemote msg = null;
		while ((msg = nws.getNextMessage()) != null) {
			if(msg.getObject() instanceof ClientViewportUpdate) {
				updates.put(msg.getConnection(), (ClientViewportUpdate)msg.getObject());
			}
		}
	}

	@Override
	protected void process(Entity e) {
		vfm.get(e).rect.set(updates.get(ccm.get(e).connection).view);
	}
	
	@Override
	protected void dispose() {
	}
}
