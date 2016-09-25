package de.vatterger.game.packets.registers;

import com.esotericsoftware.kryo.Kryo;

import de.vatterger.engine.handler.network.PacketRegister;
import de.vatterger.game.packets.ChangeAccountPacket;
import de.vatterger.game.packets.CreateAccountPacket;
import de.vatterger.game.packets.LoginPacket;
import de.vatterger.game.packets.PublicKeyPacket;

public class AccountPacketRegister implements PacketRegister {

	@Override
	public void register(Kryo kryo) {
		kryo.register(byte[].class);
		kryo.register(ChangeAccountPacket.class);
		kryo.register(CreateAccountPacket.class);
		kryo.register(LoginPacket.class);
		kryo.register(PublicKeyPacket.class);
	}

}
