package de.vatterger.entitysystem.processors;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import de.vatterger.entitysystem.components.RemoteSlave;
import de.vatterger.entitysystem.netservice.PacketRegister;
import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.networkmessages.RemoteMasterRemove;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;
import static de.vatterger.entitysystem.tools.GameConstants.*;

public class RemoteSlaveProcessor extends EntityProcessingSystem {

	private ComponentMapper<RemoteSlave>	rsm;
	private Queue<RemoteMasterUpdate> updateQueue = new ConcurrentLinkedQueue<RemoteMasterUpdate>();
	private Bag<RemoteMasterUpdate> updateRegister = new Bag<RemoteMasterUpdate>(1000);
	private Client client;

	@SuppressWarnings("unchecked")
	public RemoteSlaveProcessor() {
		super(Aspect.getAspectForAll(RemoteSlave.class));
	}

	@Override
	protected void initialize() {
		rsm = world.getMapper(RemoteSlave.class);

		Log.set(Log.LEVEL_INFO);

		client = new Client(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		
		PacketRegister.registerClasses(client.getKryo());
		
		client.addListener(new Listener(){
			@Override
			public void received(Connection connection, Object object) {
				if(object instanceof PacketBundle){
					Bag<Object> content = ((PacketBundle)object).getContent();
					for (int i = 0; i < content.size(); i++) {
						received(connection, content.get(i));
					}
				} else if(object instanceof RemoteMasterUpdate){
					updateQueue.add((RemoteMasterUpdate)object);
				} else if (object instanceof RemoteMasterRemove) {
					updateRegister.set(((RemoteMasterRemove)object).id, null);
				} else {
					System.out.println("Received something else lol");
				}
			}
			
			@Override
			public void disconnected(Connection arg0) {
				Gdx.app.exit();
			}
		});
		
		client.start();
		
		try {
			client.connect(100, client.discoverHost(NET_PORT, 1000), NET_PORT, NET_PORT);
		} catch (Exception e) {
			Gdx.app.exit();
		}
	}
	
	@Override
	protected void begin() {
		while (!updateQueue.isEmpty()) {
			int id = updateQueue.peek().id;
			if(updateRegister.safeGet(id) == null) {
				world.createEntity().edit().add(new RemoteSlave(id));
			}
			updateRegister.set(id, updateQueue.poll());
		}
	}
	
	@Override
	protected void process(Entity e) {
		RemoteSlave rs = rsm.get(e);
		RemoteMasterUpdate rmu = updateRegister.get(rs.masterId);
		Entity ent = e;
		if (rmu != null) {
			if (rmu.fullUpdate) {
				e.deleteFromWorld();
				ent = world.createEntity().edit().add(new RemoteSlave(rmu.id)).getEntity();
			}

			EntityEdit ed = ent.edit();
			for (int i = 0; i < rmu.components.size(); i++) {
				if (rmu.components.get(i) != null) {
					ed.add((Component) rmu.components.get(i));
				}
			}
			updateRegister.set(rmu.id, null);
		}
		
	}
	
	@Override
	protected void dispose() {
		client.close();
		client.stop();
	}
}
