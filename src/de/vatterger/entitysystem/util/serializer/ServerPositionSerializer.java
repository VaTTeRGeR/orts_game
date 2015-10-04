package de.vatterger.entitysystem.util.serializer;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.ServerPosition;

public class ServerPositionSerializer extends Serializer<ServerPosition>{
	float precisionMultiplier = 10f;
	@Override
	public ServerPosition read(Kryo kryo, Input in, Class<ServerPosition> oclass) {
		return new ServerPosition(new Vector3(in.readFloat(precisionMultiplier, true), in.readFloat(precisionMultiplier, true), in.readFloat(precisionMultiplier, true)));
	}
	
	@Override
	public void write(Kryo kryo, Output out, ServerPosition pos) {
		out.writeFloat(pos.pos.x, precisionMultiplier, true);
		out.writeFloat(pos.pos.y, precisionMultiplier, true);
		out.writeFloat(pos.pos.z, precisionMultiplier, true);
	}
}
