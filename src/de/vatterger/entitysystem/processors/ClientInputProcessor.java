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
import de.vatterger.entitysystem.network.MessageRemote;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.ServerNetworkService;
import de.vatterger.entitysystem.network.messages.ClientViewportUpdate;

@Wire
public class ClientInputProcessor extends EntityProcessingSystem {

	private ComponentMapper<ViewFrustum> vfm;
	private ComponentMapper<KryoConnection> ccm;
	
	private HashMap<Connection, ClientViewportUpdate> updates = new HashMap<Connection, ClientViewportUpdate>();

	private FilteredListener<ClientViewportUpdate> listener= new FilteredListener<ClientViewportUpdate>(ClientViewportUpdate.class);

	@SuppressWarnings("unchecked")
	public ClientInputProcessor() {
		super(Aspect.getAspectForAll(ViewFrustum.class, KryoConnection.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkService.instance().addListener(listener);
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
		MessageRemote<ClientViewportUpdate> update = null;
		while((update = listener.getNext()) != null) {
			updates.put(update.getConnection(),update.getObject());
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
