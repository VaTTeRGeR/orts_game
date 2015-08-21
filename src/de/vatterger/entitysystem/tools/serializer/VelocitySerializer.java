package de.vatterger.entitysystem.tools.serializer;

import com.badlogic.gdx.math.Vector2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.Velocity;

public class VelocitySerializer extends Serializer<Velocity>{
	@Override
	public Velocity read(Kryo kryo, Input in, Class<Velocity> oclass) {
		return new Velocity(new Vector2(in.readFloat(), in.readFloat()));
	}
	
	@Override
	public void write(Kryo kryo, Output out, Velocity vel) {
		out.writeFloat(vel.vel.x);
		out.writeFloat(vel.vel.y);
	}
}
