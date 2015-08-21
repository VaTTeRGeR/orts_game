package de.vatterger.entitysystem.tools.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.SlimeCollision;

public class CircleCollisionSerializer extends Serializer<SlimeCollision>{
	@Override
	public SlimeCollision read(Kryo kryo, Input in, Class<SlimeCollision> oClass) {
		return new SlimeCollision(in.readFloat(), null);
	}
	
	@Override
	public void write(Kryo kryo, Output out, SlimeCollision circle) {
		out.writeFloat(circle.circle.radius);
	}
}
