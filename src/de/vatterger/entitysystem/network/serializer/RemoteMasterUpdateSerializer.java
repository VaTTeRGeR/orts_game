package de.vatterger.entitysystem.network.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers.ObjectArraySerializer;

import de.vatterger.entitysystem.network.packets.server.RemoteMasterUpdate;

public class RemoteMasterUpdateSerializer extends Serializer<RemoteMasterUpdate>{
	
	ObjectArraySerializer oas = new ObjectArraySerializer();
	public RemoteMasterUpdateSerializer() {
		oas.setElementsAreSameType(false);
		oas.setAcceptsNull(false);
	}
	
	@Override
	public RemoteMasterUpdate read(Kryo kryo, Input in, Class<RemoteMasterUpdate> rmuClass) {
		int id = in.readInt();
		byte flags = in.readByte();
		Object[] components;
		if(flags < 4)
			components = kryo.readObject(in, Object[].class, oas);
		else
			components = new Object[0];
		return new RemoteMasterUpdate(id, flags == 0 || flags == 4, components);
	}
	
	@Override
	public void write(Kryo kryo, Output out, RemoteMasterUpdate rmu) {
		out.writeInt(rmu.id);
		out.writeByte(rmu.flags);
		if(rmu.flags < 4)
			kryo.writeObject(out, rmu.components, oas);
	}
}
