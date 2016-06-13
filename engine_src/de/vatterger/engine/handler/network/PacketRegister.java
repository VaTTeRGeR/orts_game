package de.vatterger.engine.handler.network;

import com.esotericsoftware.kryo.Kryo;

public interface PacketRegister {
	public void register(Kryo kryo);
}
