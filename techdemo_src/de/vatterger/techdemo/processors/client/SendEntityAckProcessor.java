package de.vatterger.techdemo.processors.client;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IntervalIteratingSystem;
import com.artemis.utils.Bag;

import de.vatterger.engine.handler.network.ClientNetworkHandler;
import de.vatterger.engine.util.GameUtil;
import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.client.RemoteSlave;
import de.vatterger.techdemo.components.shared.Inactive;
import de.vatterger.techdemo.network.packets.client.EntityAckPacket;

@Wire
public class SendEntityAckProcessor extends IntervalIteratingSystem {
	
	private ComponentMapper<RemoteSlave> rsm;
	private Bag<Integer> idBag = new Bag<Integer>(512);
	private boolean readyToSample;
	
	@SuppressWarnings("unchecked")
	public SendEntityAckProcessor() {
		super(Aspect.all(RemoteSlave.class).exclude(Inactive.class), 0.25f);
		readyToSample = true;
	}

	@Override
	protected void begin() {
		if(readyToSample)
			idBag.clear();
	}
	
	@Override
	protected void process(int e) {
		if(readyToSample)
			idBag.add(rsm.get(e).masterId);
	}

	@Override
	protected void end() {
		if(idBag.size() > 0) {
			int maxInts = GameConstants.PACKETSIZE_INTERNET/4;
			int[] ids = new int[GameUtil.min(maxInts, idBag.size()+1)];
			for (int i = 1; i < maxInts &! idBag.isEmpty(); i++) {
				ids[i] = idBag.removeLast();
			}
			if(readyToSample)
				ids[0] = 1;
			else
				ids[0] = 0;
			/*System.out.print("{");
			for (int i = 0; i < ids.length; i++) {
				System.out.print(ids[i]+", ");
			}
			System.out.println("}");*/
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
