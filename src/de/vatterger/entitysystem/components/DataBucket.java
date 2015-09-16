package de.vatterger.entitysystem.components;

import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.util.GameConstants;

public class DataBucket extends Component {
	private Queue<Object> msgUnreliable = new LinkedList<Object>();
	private Queue<Integer> msgUnreliableSize = new LinkedList<Integer>();
	private Queue<Object> msgReliable = new LinkedList<Object>();
	private Queue<Integer> msgReliableSize = new LinkedList<Integer>();

	public DataBucket addData(Object o, boolean reliable, int size){
		if(reliable) {
			msgReliable.add(o);
			msgReliableSize.add(size);
		} else {
			msgUnreliable.add(o);
			msgUnreliableSize.add(size);
		}
		return this;
	}
	
	public void clearData() {
		msgReliable.clear();
		msgReliableSize.clear();
		msgUnreliable.clear();
		msgUnreliableSize.clear();
	}
	
	public Bag<PacketBundle> getPacketBundles(int size, int maxNumberOf) {
		Bag<PacketBundle> bundles = new Bag<PacketBundle>(maxNumberOf);
		
		if (!msgReliable.isEmpty()) {
			PacketBundle bundle = new PacketBundle(size, true);
			while (!msgReliable.isEmpty() && bundles.size() < maxNumberOf) {
				if (!bundle.hasFreeBytes()) {
					bundles.add(bundle);
					bundle = new PacketBundle(GameConstants.PACKETSIZE_INTERNET, true);
				} else {
					bundle.add(msgReliable.poll(), msgReliableSize.poll());
				}
			}
			if(!bundle.isEmpty()) {
				bundles.add(bundle);
			}
		} else if (!msgUnreliable.isEmpty()) {
			PacketBundle bundle = new PacketBundle(size, false);
			while (!msgUnreliable.isEmpty() && bundles.size() < maxNumberOf) {
				if (!bundle.hasFreeBytes()) {
					bundles.add(bundle);
					bundle = new PacketBundle(GameConstants.PACKETSIZE_INTERNET, false);
				} else {
					bundle.add(msgUnreliable.poll(), msgUnreliableSize.poll());
				}
			}
			if(!bundle.isEmpty()) {
				bundles.add(bundle);
			}
		}
		
		bundles.trim();
		
		return bundles;
	}
	
	public boolean isEmpty() {
		return msgUnreliable.isEmpty() && msgReliable.isEmpty();
	}
}
