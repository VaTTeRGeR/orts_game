package de.vatterger.entitysystem.util.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer;

import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;

public class RemoteMasterUpdateSerializer extends Serializer<RemoteMasterUpdate>{
	
	ObjectArraySerializer oas = new ObjectArraySerializer();
	
	@Override
	public RemoteMasterUpdate read(Kryo kryo, Input in, Class<RemoteMasterUpdate> rmuClass) {
		return new RemoteMasterUpdate(in.readInt(), in.readBoolean(), kryo.readObject(in, Object[].class, oas));
	}
	
	@Override
	public void write(Kryo kryo, Output out, RemoteMasterUpdate rmu) {
		out.writeInt(rmu.id);
		out.writeBoolean(rmu.fullUpdate);
		kryo.writeObject(out, rmu.components);
	}
}
