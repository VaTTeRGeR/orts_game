package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.handler.network.ClientNetworkHandler;
import de.vatterger.entitysystem.network.packets.EntityAckPacket;
import de.vatterger.entitysystem.util.GameUtil;

@Wire
public class SendEntityAckProcessor extends IntervalEntityProcessingSystem {
	
	private ComponentMapper<RemoteSlave> rsm;
	private Bag<Integer> idBag = new Bag<Integer>(512);
	private boolean readyToSample;
	
	@SuppressWarnings("unchecked")
	public SendEntityAckProcessor() {
		super(Aspect.getAspectForAll(RemoteSlave.class).exclude(Inactive.class), 0.25f);
		readyToSample = true;
	}

	@Override
	protected void begin() {
		if(readyToSample)
			idBag.clear();
	}
	
	@Override
	protected void process(Entity e) {
		if(readyToSample)
			idBag.add(rsm.get(e).masterId);
	}

	@Override
	protected void end() {
		if(idBag.size() > 0) {
			int maxInts = GameConstants.PACKETSIZE_INTERNET/4;
			int[] ids = new int[GameUtil.min(maxInts, idBag.size()+1)];
			for (int i = 1; idBag.size() > 0 && i < maxInts; i++) {
				ids[i] = idBag.removeLast();
			}
			if(readyToSample)
				ids[0] = 1;
			else
				ids[0] = 0;
				
			ClientNetworkHandler.instance().send(new EntityAckPacket(ids), false);

			readyToSample = false;
		} else {
			ClientNetworkHandler.instance().send(new EntityAckPacket(new int[]{1}), false);
		}
		
		if(idBag.size() == 0) {
			readyToSample = true;
		}
	}
}
