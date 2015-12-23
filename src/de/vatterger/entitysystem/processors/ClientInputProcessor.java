package de.vatterger.entitysystem.processors;

import java.util.HashMap;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.ClientViewportUpdate;

@Wire
public class ClientInputProcessor extends EntityProcessingSystem {

	private ComponentMapper<NetSynchedArea> nsam;
	private ComponentMapper<KryoConnection> kcm;
	
	private HashMap<Connection, ClientViewportUpdate> updates = new HashMap<Connection, ClientViewportUpdate>();

	private FilteredListener<ClientViewportUpdate> listener= new FilteredListener<ClientViewportUpdate>(ClientViewportUpdate.class);

	@SuppressWarnings("unchecked")
	public ClientInputProcessor() {
		super(Aspect.getAspectForAll(NetSynchedArea.class, KryoConnection.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkHandler.instance().addListener(listener);
	}
	
	@Override
	protected void inserted(Entity e) {
		updates.put(kcm.get(e).connection, new ClientViewportUpdate(nsam.get(e).rect));
	}
	
	@Override
	protected void removed(Entity e) {
		updates.remove(kcm.get(e).connection);
	}
	
	@Override
	protected void begin() {
		KryoNetMessage<ClientViewportUpdate> update = null;
		while((update = listener.getNext()) != null) {
			updates.put(update.getConnection(),update.getObject());
		}
	}

	@Override
	protected void process(Entity e) {
		nsam.get(e).rect.set(updates.get(kcm.get(e).connection).view);
	}
}
