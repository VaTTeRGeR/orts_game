package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.entitysystem.components.server.KryoConnection;
import de.vatterger.entitysystem.components.shared.NetSynchedArea;
import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.KryoNetMessage;
import de.vatterger.entitysystem.network.packets.client.ViewportUpdate;

@Wire
public class ReceiveViewportUpdateProcessor extends EntityProcessingSystem {

	private ComponentMapper<NetSynchedArea> nsam;
	private ComponentMapper<KryoConnection> kcm;
	
	private FilteredListener<ViewportUpdate> listener= new FilteredListener<ViewportUpdate>(ViewportUpdate.class);

	public ReceiveViewportUpdateProcessor() {
		super(Aspect.all(NetSynchedArea.class, KryoConnection.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkHandler.instance().addListener(listener);
	}
	
	@Override
	protected void process(Entity e) {
		KryoNetMessage<ViewportUpdate> update = null;
		while((update = listener.getNext(kcm.get(e).connection)) != null) {
			nsam.get(e).rect.set(update.getObject().view);
		}
	}
}
