package de.vatterger.entitysystem.network.serializer;

import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.server.ServerPosition;

public class ServerPositionSerializer extends Serializer<ServerPosition>{
	@Override
	public ServerPosition read(Kryo kryo, Input in, Class<ServerPosition> oclass) {
		return new ServerPosition(new Vector3(in.readFloat(), in.readFloat(), in.readFloat()));
	}
	
	@Override
	public void write(Kryo kryo, Output out, ServerPosition pos) {
		out.writeFloat(pos.pos.x);
		out.writeFloat(pos.pos.y);
		out.writeFloat(pos.pos.z);
	}
}
