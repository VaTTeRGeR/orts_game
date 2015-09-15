package de.vatterger.entitysystem.components;

import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.networkmessages.PacketBundle;

public class DataBucket extends Component {
	private Queue<Object> msg = new LinkedList<Object>();
	private Queue<Integer> msgSize = new LinkedList<Integer>();
	private int sumSize = 0;

	public DataBucket addData(Object o, int size){
		msg.add(o);
		msgSize.add(size);
		sumSize += size;
		return this;
	}
	
	public void clearData() {
		msg.clear();
		msgSize.clear();
		sumSize = 0;
	}
	
	public Bag<PacketBundle> getPacketBundles(int size, int maxNumberOf) {
		Bag<PacketBundle> bundles = new Bag<PacketBundle>(500);
		PacketBundle bundle = new PacketBundle(size);
		while(!msg.isEmpty() && bundles.size() < maxNumberOf) {
			if(bundle.hasFreeBytes()) {
				bundle.add(msg.poll(), msgSize.peek());
				sumSize-=msgSize.poll();
			} else {
				bundle.packets.trim();
				if(bundle.packets.size() > 0) {
					bundles.add(bundle);
					bundle = new PacketBundle(size);
				}
			}
		}
		if(!bundle.packets.isEmpty()) {
			bundles.add(bundle);
		}
		bundles.trim();
		return bundles;
	}
	
	public int getObjectSize() {
		return sumSize;
	}
	
	public boolean isEmpty() {
		return msg.isEmpty() && msgSize.isEmpty();
	}
}
