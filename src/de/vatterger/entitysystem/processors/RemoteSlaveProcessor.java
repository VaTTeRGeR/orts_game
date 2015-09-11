package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.util.Constants.*;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import de.vatterger.entitysystem.components.RemoteSlave;
import de.vatterger.entitysystem.netservice.PacketRegister;
import de.vatterger.entitysystem.networkmessages.ClientViewportUpdate;
import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.networkmessages.RemoteMasterRemove;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;

public class RemoteSlaveProcessor extends EntityProcessingSystem {

	private ComponentMapper<RemoteSlave>	rsm;
	
	private Queue<RemoteMasterUpdate> updateQueue = new ConcurrentLinkedQueue<RemoteMasterUpdate>();
	
	private Bag<RemoteMasterUpdate> updateRegister = new Bag<RemoteMasterUpdate>(1);
	private Bag<Entity> slaveRegister = new Bag<Entity>(1);
	
	private Client client;
	private int packages = 0;
	
	private Camera camera;
	
	private Rectangle viewport = new Rectangle(0,0,0,0);

	@SuppressWarnings("unchecked")
	public RemoteSlaveProcessor(Camera camera) {
		super(Aspect.getAspectForAll(RemoteSlave.class));
		this.camera = camera;
	}

	@Override
	protected void initialize() {
		rsm = world.getMapper(RemoteSlave.class);

		Log.set(Log.LEVEL_NONE);

		client = new Client(QUEUE_BUFFER_SIZE, OBJECT_BUFFER_SIZE);
		
		PacketRegister.registerClasses(client.getKryo());
		
		client.addListener( new Listener(){
			@Override
			public void received(Connection connection, Object object) {
				if(object instanceof PacketBundle){
					packages++;
					Bag<Object> content = ((PacketBundle)object).getContent();
					for (int i = 0; i < content.size(); i++) {
						received(connection, content.get(i));
					}
				} else if(object instanceof RemoteMasterUpdate){
					updateQueue.add((RemoteMasterUpdate)object);
				} else if (object instanceof RemoteMasterRemove) {
					updateRegister.set(((RemoteMasterRemove)object).id, null);
					slaveRegister.remove(((RemoteMasterRemove)object).id);
				} else {
					//System.out.println("Received "+object.toString());
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				Gdx.app.exit();
			}
		});
		
		client.start();
		
		try {
			InetAddress addrLocal = client.discoverHost(26000, 100);
			if(addrLocal == null)
				addrLocal = InetAddress.getByName(JOptionPane.showInputDialog(null, "What is the Remote ip?", "Enter the remote IP", JOptionPane.QUESTION_MESSAGE));
			client.connect(5000, addrLocal, NET_PORT, NET_PORT);
		} catch (Exception e) {
			Gdx.app.exit();
		}
	}
	
	@Override
	protected void begin() {
		while (!updateQueue.isEmpty()) {
			int id = updateQueue.peek().id;
			//System.out.println("Received: "+updateQueue.peek().toString()+". "+updateQueue.size()+" left in queue");
			if(slaveRegister.safeGet(id) == null) {
				slaveRegister.set(id, world.createEntity().edit().add(new RemoteSlave(id)).getEntity());
			}
			updateRegister.set(id, updateQueue.poll());
		}
		client.sendUDP(new ClientViewportUpdate(viewport.set(camera.position.x-camera.viewportWidth/2f,camera.position.y-camera.viewportHeight/2f,camera.viewportWidth,camera.viewportHeight)));
	}
	
	@Override
	protected void process(Entity e) {
		RemoteSlave rs = rsm.get(e);
		RemoteMasterUpdate rmu = updateRegister.get(rs.masterId);
		Entity newEnt = e;
		if (rmu != null) {
			if (rmu.fullUpdate) {
				e.deleteFromWorld();
				newEnt = world.createEntity().edit().add(new RemoteSlave(rmu.id)).getEntity();
				slaveRegister.set(rmu.id, newEnt);
				rs.lastUpdateDelay = 0;
			}

			EntityEdit ed = newEnt.edit();
			for (int i = 0; i < rmu.components.length; i++) {
				if (rmu.components[i] != null) {
					ed.add((Component) rmu.components[i]);
				}
			}
			updateRegister.set(rmu.id, null);
		} else {
			if(rs.lastUpdateDelay>ENTITY_UPDATE_TIMEOUT || slaveRegister.safeGet(rs.masterId) == null) {
				e.deleteFromWorld();
				slaveRegister.set(rs.masterId, null);
			}
			rs.lastUpdateDelay+=world.getDelta();
		}
	}
	
	@Override
	protected void end() {
		if(packages>0) {
			System.out.println("Packetbundles: "+packages);
		}
		packages = 0;
	}
	
	@Override
	protected void dispose() {
		client.close();
		client.stop();
	}
}
