package de.vatterger.techdemo.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import de.vatterger.techdemo.components.server.KryoConnection;
import de.vatterger.techdemo.factory.server.TankFactory;
import de.vatterger.techdemo.handler.network.ServerNetworkHandler;
import de.vatterger.techdemo.network.FilteredListener;
import de.vatterger.techdemo.network.KryoNetMessage;
import de.vatterger.techdemo.network.packets.client.SpawnTankUpdate;

@Wire
public class SpawnTankUpdateProcessor extends IteratingSystem {

	private ComponentMapper<KryoConnection> kcm;
	
	private FilteredListener<SpawnTankUpdate> listener= new FilteredListener<SpawnTankUpdate>(SpawnTankUpdate.class);

	public SpawnTankUpdateProcessor() {
		super(Aspect.all(KryoConnection.class));
	}

	@Override
	protected void initialize() {
		ServerNetworkHandler.instance().addListener(listener);
	}
	
	@Override
	protected void process(int entityId) {
		KryoNetMessage<SpawnTankUpdate> update = null;
		while((update = listener.getNext(kcm.get(entityId).connection)) != null) {
			TankFactory.createTank(world, update.getObject().vec);
		}
	}
}
