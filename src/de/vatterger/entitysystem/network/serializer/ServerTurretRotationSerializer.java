package de.vatterger.entitysystem.network.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.server.ServerTurretRotation;

public class ServerTurretRotationSerializer extends Serializer<ServerTurretRotation>{
	@Override
	public ServerTurretRotation read(Kryo kryo, Input in, Class<ServerTurretRotation> oclass) {
		return new ServerTurretRotation(in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, ServerTurretRotation rot) {
		out.writeFloat(rot.rot);
	}
}
