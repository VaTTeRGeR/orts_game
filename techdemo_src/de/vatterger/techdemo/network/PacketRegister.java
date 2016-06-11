package de.vatterger.techdemo.network;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;

import de.vatterger.engine.handler.network.serializer.BagSerializer;
import de.vatterger.techdemo.components.server.ServerPosition;
import de.vatterger.techdemo.components.server.ServerRotation;
import de.vatterger.techdemo.components.server.ServerTurretRotation;
import de.vatterger.techdemo.components.shared.CircleCollision;
import de.vatterger.techdemo.components.shared.G3DBModelId;
import de.vatterger.techdemo.components.shared.Velocity;
import de.vatterger.techdemo.network.packets.client.EntityAckPacket;
import de.vatterger.techdemo.network.packets.client.SpawnTankUpdate;
import de.vatterger.techdemo.network.packets.client.ViewportUpdate;
import de.vatterger.techdemo.network.packets.server.PacketBundle;
import de.vatterger.techdemo.network.packets.server.RemoteMasterUpdate;
import de.vatterger.techdemo.network.serializer.CircleCollisionSerializer;
import de.vatterger.techdemo.network.serializer.EntityAckPaketSerializer;
import de.vatterger.techdemo.network.serializer.G3DBModelIdSerializer;
import de.vatterger.techdemo.network.serializer.PacketBundleSerializer;
import de.vatterger.techdemo.network.serializer.RemoteMasterUpdateSerializer;
import de.vatterger.techdemo.network.serializer.ServerPositionSerializer;
import de.vatterger.techdemo.network.serializer.ServerRotationSerializer;
import de.vatterger.techdemo.network.serializer.ServerTurretRotationSerializer;
import de.vatterger.techdemo.network.serializer.VelocitySerializer;

public class PacketRegister {

	private PacketRegister() {}

	public static void registerClasses(Kryo kryo) {
		kryo.register(Bag.class, new BagSerializer());
		kryo.register(ServerPosition.class, new ServerPositionSerializer());
		kryo.register(Velocity.class, new VelocitySerializer());
		kryo.register(G3DBModelId.class, new G3DBModelIdSerializer());
		kryo.register(ServerRotation.class, new ServerRotationSerializer());
		kryo.register(ServerTurretRotation.class, new ServerTurretRotationSerializer());
		kryo.register(CircleCollision.class, new CircleCollisionSerializer());
		kryo.register(PacketBundle.class, new PacketBundleSerializer());
		kryo.register(RemoteMasterUpdate.class, new RemoteMasterUpdateSerializer());
		kryo.register(EntityAckPacket.class, new EntityAckPaketSerializer());
		kryo.register(Rectangle.class);
		kryo.register(Vector2.class);
		kryo.register(Vector3.class);
		kryo.register(ViewportUpdate.class);
		kryo.register(SpawnTankUpdate.class);
		kryo.register(Object.class);
		kryo.register(Object[].class);
		kryo.register(String.class);
	}
}
