package de.vatterger.entitysystem.tools.serializer;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.networkmessages.PacketBundle;

public class PacketBundleSerializer extends Serializer<PacketBundle> {

	BagSerializer bs = new BagSerializer();
	
	@SuppressWarnings("unchecked")
	@Override
	public PacketBundle read(Kryo kryo, Input in, Class<PacketBundle> oclass) {
		PacketBundle pb = new PacketBundle();
		pb.packets = kryo.readObject(in, Bag.class);
		return pb;
	}

	@Override
	public void write(Kryo kryo, Output out, PacketBundle pb) {
		kryo.writeObject(out, pb.packets);
	}

}
