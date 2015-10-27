package de.vatterger.entitysystem.components.server;

import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Component;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.interfaces.Sizeable;
import de.vatterger.entitysystem.netservice.PacketBundle;

public class DataBucket extends Component {
	private Queue<Object> msgUnreliable = new LinkedList<Object>();
	private Queue<Integer> msgUnreliableSize = new LinkedList<Integer>();
	private Queue<Object> msgReliable = new LinkedList<Object>();
	private Queue<Integer> msgReliableSize = new LinkedList<Integer>();

	public DataBucket addData(Sizeable o, boolean reliable){
		if(reliable) {
			msgReliable.add(o);
			msgReliableSize.add(o.getSizeInBytes());
		} else {
			msgUnreliable.add(o);
			msgUnreliableSize.add(o.getSizeInBytes());
		}
		return this;
	}
	
	public void clearData() {
		msgReliable.clear();
		msgReliableSize.clear();
		msgUnreliable.clear();
		msgUnreliableSize.clear();
	}
	
	/**Compiles a Bag of update-bundles **/
	public Bag<PacketBundle> getPacketBundles(int packetSize, int maxNumPackets) {
		Bag<PacketBundle> bundles = new Bag<PacketBundle>(maxNumPackets);
		
		/*gather TCP messages*/
		if (!msgReliable.isEmpty()) {
			PacketBundle bundle = new PacketBundle(packetSize, true);
			while (!msgReliable.isEmpty() && bundles.size() < maxNumPackets) {
				/*If bundle is full, create new one*/
				if (!bundle.hasFreeBytes()) {
					bundle.packets.trim();
					bundles.add(bundle);
					bundle = new PacketBundle(GameConstants.PACKETSIZE_INTERNET, true);
				} else {
					bundle.add(msgReliable.poll(), msgReliableSize.poll());
				}
			}
			/*Only add bundle if it has content*/
			if(!bundle.isEmpty()) {
				bundle.packets.trim();
				bundles.add(bundle);
			}
		}/*gather UDP messages*/
		else if (!msgUnreliable.isEmpty()) {
			PacketBundle bundle = new PacketBundle(packetSize, false);
			while (!msgUnreliable.isEmpty() && bundles.size() < maxNumPackets) {
				/*If bundle is full, create new one*/
				if (!bundle.hasFreeBytes()) {
					bundle.packets.trim();
					bundles.add(bundle);
					bundle = new PacketBundle(GameConstants.PACKETSIZE_INTERNET, false);
				} else {
					bundle.add(msgUnreliable.poll(), msgUnreliableSize.poll());
				}
			}
			/*Only add bundle if it has content*/
			if(!bundle.isEmpty()) {
				bundle.packets.trim();
				bundles.add(bundle);
			}
		}
		
		/*trim bag to eliminate null values*/
		bundles.trim();
		
		return bundles;
	}
	
	/**Returns true if this DataBucket is empty**/
	public boolean isEmpty() {
		return msgUnreliable.isEmpty() && msgReliable.isEmpty();
	}
}
