package de.vatterger.entitysystem.network;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryo.Kryo;

import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.server.ServerRotation;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.G3DBModelId;
import de.vatterger.entitysystem.components.shared.Velocity;
import de.vatterger.entitysystem.interfaces.Modifiable;
import de.vatterger.entitysystem.network.packets.ClientViewportUpdate;
import de.vatterger.entitysystem.network.packets.PacketBundle;
import de.vatterger.entitysystem.network.packets.RemoteMasterUpdate;
import de.vatterger.entitysystem.network.serializer.BagSerializer;
import de.vatterger.entitysystem.network.serializer.CircleCollisionSerializer;
import de.vatterger.entitysystem.network.serializer.G3DBModelIdSerializer;
import de.vatterger.entitysystem.network.serializer.PacketBundleSerializer;
import de.vatterger.entitysystem.network.serializer.RemoteMasterUpdateSerializer;
import de.vatterger.entitysystem.network.serializer.ServerPositionSerializer;
import de.vatterger.entitysystem.network.serializer.ServerRotationSerializer;
import de.vatterger.entitysystem.network.serializer.VelocitySerializer;

public class PacketRegister {

	private PacketRegister() {}

	public static void registerClasses(Kryo kryo) {
		kryo.register(Bag.class, new BagSerializer());
		kryo.register(ServerPosition.class, new ServerPositionSerializer());
		kryo.register(Velocity.class, new VelocitySerializer());
		kryo.register(G3DBModelId.class, new G3DBModelIdSerializer());
		kryo.register(ServerRotation.class, new ServerRotationSerializer());
		kryo.register(CircleCollision.class, new CircleCollisionSerializer());
		kryo.register(PacketBundle.class, new PacketBundleSerializer());
		kryo.register(RemoteMasterUpdate.class, new RemoteMasterUpdateSerializer());
		kryo.register(ClientViewportUpdate.class);
		kryo.register(Rectangle.class);
		kryo.register(Modifiable.class);
		kryo.register(Object.class);
		kryo.register(Object[].class);
		kryo.register(String.class);
	}
}
