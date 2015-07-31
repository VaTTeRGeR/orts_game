package de.vatterger.entitysytem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.esotericsoftware.kryonet.Server;

import de.vatterger.entitysystem.components.KryoConnection;
import de.vatterger.entitysystem.components.MessageBucket;

public class PacketProcessor extends EntityProcessingSystem {
	
	private Server server;

	private Bag<Object> sendFlyWeight = new Bag<Object>(64);
	
	private ComponentMapper<MessageBucket> mbm;
	private ComponentMapper<KryoConnection> kkm;
	
	private static final int MAX_PACKET_SIZE = 1024;
	
	@SuppressWarnings("unchecked")
	public PacketProcessor(Server server) {
		super(Aspect.getAspectForAll(MessageBucket.class));
		this.server = server;
	}
	
	@Override
	protected void inserted(Entity e) {
	}
	
	@Override
	protected void removed(Entity e) {
	}

	@Override
	protected void initialize() {
		mbm = world.getMapper(MessageBucket.class);
		kkm = world.getMapper(KryoConnection.class);
	}
	
	@Override
	protected void begin() {
	}
		
	@Override
	protected void process(Entity e) {
		MessageBucket mb = mbm.get(e);
		
		sendFlyWeight.fastClear();
		int size = 0;

		while(!mb.msg.isEmpty()) {
			
			sendFlyWeight.add(mb.msg.poll());
			size+=mb.msgSize.poll();
			
			if(size > MAX_PACKET_SIZE || mb.msg.isEmpty()) {
				
				server.sendToTCP(kkm.get(e).connection.getID(), sendFlyWeight);
				
				sendFlyWeight.fastClear();
				size = 0;
			}
		}
	}
	
	@Override
	protected void end() {
		
	}
}
