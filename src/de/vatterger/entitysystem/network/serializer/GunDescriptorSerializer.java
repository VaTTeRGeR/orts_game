package de.vatterger.entitysystem.network.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.server.GunDesciptor;

public class GunDescriptorSerializer extends Serializer<GunDesciptor>{
	@Override
	public GunDesciptor read(Kryo kryo, Input in, Class<GunDesciptor> vectorClass) {
		GunDesciptor gd = new GunDesciptor();
		gd.firing = in.readBoolean();
		gd.gunTargetId = in.readInt(false);
		return gd;
	}
	
	@Override
	public void write(Kryo kryo, Output out, GunDesciptor gd) {
		out.writeBoolean(gd.firing);
		out.writeInt(gd.gunTargetId,false);
	}
}
