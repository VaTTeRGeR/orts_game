package de.vatterger.techdemo.network.serializer;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class Vector3Serializer extends Serializer<Vector3>{
	@Override
	public Vector3 read(Kryo kryo, Input in, Class<Vector3> vectorClass) {
		return new Vector3(in.readFloat(), in.readFloat(), in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, Vector3 vec) {
		out.writeFloat(vec.x);
		out.writeFloat(vec.y);
		out.writeFloat(vec.z);
	}
}
