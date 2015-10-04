package de.vatterger.entitysystem.util.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.ServerRotation;

public class ServerRotationSerializer extends Serializer<ServerRotation>{
	@Override
	public ServerRotation read(Kryo kryo, Input in, Class<ServerRotation> oclass) {
		return new ServerRotation(in.readFloat(0.1f,true));
	}
	
	@Override
	public void write(Kryo kryo, Output out, ServerRotation rot) {
		out.writeFloat(rot.rot, 0.1f, true);
	}
}
