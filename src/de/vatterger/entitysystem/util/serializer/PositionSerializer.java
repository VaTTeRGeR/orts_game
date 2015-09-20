package de.vatterger.entitysystem.util.serializer;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.Position;

public class PositionSerializer extends Serializer<Position>{
	@Override
	public Position read(Kryo kryo, Input in, Class<Position> oclass) {
		return new Position(new Vector3(in.readFloat(), in.readFloat(), in.readFloat()));
	}
	
	@Override
	public void write(Kryo kryo, Output out, Position pos) {
		out.writeFloat(pos.pos.x);
		out.writeFloat(pos.pos.y);
		out.writeFloat(pos.pos.z);
	}
}
