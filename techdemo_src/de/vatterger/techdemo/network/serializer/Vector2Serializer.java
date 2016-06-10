package de.vatterger.techdemo.network.serializer;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Vector2Serializer extends Serializer<Vector2>{
	@Override
	public Vector2 read(Kryo kryo, Input in, Class<Vector2> vectorClass) {
		return new Vector2(in.readFloat(), in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, Vector2 vec) {
		out.writeFloat(vec.x);
		out.writeFloat(vec.y);
	}
}
