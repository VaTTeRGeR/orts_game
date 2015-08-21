package de.vatterger.entitysystem.netservice;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryo.Kryo;

import de.vatterger.entitysystem.components.SlimeCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.networkmessages.PacketBundle;
import de.vatterger.entitysystem.networkmessages.RemoteMasterRemove;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;
import de.vatterger.entitysystem.tools.serializer.BagSerializer;
import de.vatterger.entitysystem.tools.serializer.CircleCollisionSerializer;
import de.vatterger.entitysystem.tools.serializer.PacketBundleSerializer;
import de.vatterger.entitysystem.tools.serializer.PositionSerializer;
import de.vatterger.entitysystem.tools.serializer.RemoteMasterUpdateSerializer;
import de.vatterger.entitysystem.tools.serializer.VelocitySerializer;

public class PacketRegister {

	private PacketRegister() {}

	public static void registerClasses(Kryo kryo) {
		kryo.register(PacketBundle.class,new PacketBundleSerializer());
		kryo.register(Bag.class, new BagSerializer());
		kryo.register(Position.class, new PositionSerializer());
		kryo.register(Velocity.class, new VelocitySerializer());
		kryo.register(SlimeCollision.class, new CircleCollisionSerializer());
		kryo.register(RemoteMasterUpdate.class, new RemoteMasterUpdateSerializer());
		kryo.register(RemoteMasterRemove.class);
		kryo.register(Object.class);
		kryo.register(Object[].class);
		kryo.register(String.class);
	}
}
