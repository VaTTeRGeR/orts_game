package de.vatterger.entitysystem.util.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.Rotation;

public class RotationSerializer extends Serializer<Rotation>{
	@Override
	public Rotation read(Kryo kryo, Input in, Class<Rotation> oclass) {
		return new Rotation(in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, Rotation rot) {
		out.writeFloat(rot.rot);
	}
}
