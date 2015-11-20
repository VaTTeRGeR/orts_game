package de.vatterger.entitysystem.network.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.CircleCollision;

public class CircleCollisionSerializer extends Serializer<CircleCollision>{
	@Override
	public CircleCollision read(Kryo kryo, Input in, Class<CircleCollision> oClass) {
		return new CircleCollision(in.readFloat(10, true), null);
	}
	
	@Override
	public void write(Kryo kryo, Output out, CircleCollision cc) {
		out.writeFloat(cc.radius, 10, true);
	}
}
