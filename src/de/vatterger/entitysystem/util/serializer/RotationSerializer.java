package de.vatterger.entitysystem.util.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.ServerRotation;

public class RotationSerializer extends Serializer<ServerRotation>{
	@Override
	public ServerRotation read(Kryo kryo, Input in, Class<ServerRotation> oclass) {
		return new ServerRotation(in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, ServerRotation rot) {
		out.writeFloat(rot.rot);
	}
}
