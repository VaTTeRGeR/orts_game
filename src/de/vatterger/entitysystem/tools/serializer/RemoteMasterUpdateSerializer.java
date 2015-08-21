package de.vatterger.entitysystem.tools.serializer;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;

public class RemoteMasterUpdateSerializer extends Serializer<RemoteMasterUpdate>{
	
	@SuppressWarnings("unchecked")
	@Override
	public RemoteMasterUpdate read(Kryo kryo, Input in, Class<RemoteMasterUpdate> rmuClass) {
		return new RemoteMasterUpdate(in.readInt(), in.readBoolean(), kryo.readObject(in, Bag.class));
	}
	
	@Override
	public void write(Kryo kryo, Output out, RemoteMasterUpdate rmu) {
		out.writeInt(rmu.id);
		out.writeBoolean(rmu.fullUpdate);
		kryo.writeObject(out, rmu.components);
	}
}
