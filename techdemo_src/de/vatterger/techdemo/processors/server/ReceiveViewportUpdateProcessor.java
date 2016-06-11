package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;

import de.vatterger.engine.handler.network.ServerNetworkHandler;
import de.vatterger.engine.network.FilteredListener;
import de.vatterger.engine.network.KryoNetMessage;
import de.vatterger.techdemo.components.server.KryoConnection;
import de.vatterger.techdemo.components.shared.NetSynchedArea;
import de.vatterger.techdemo.network.packets.client.ViewportUpdate;

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
