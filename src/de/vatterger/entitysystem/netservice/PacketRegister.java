package de.vatterger.entitysystem.netservice;

import com.artemis.utils.Bag;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.networkmessages.RemoteMasterUpdate;
import de.vatterger.entitysystem.tools.serializer.BagSerializer;
import de.vatterger.entitysystem.tools.serializer.CircleSerializer;
import de.vatterger.entitysystem.tools.serializer.Vector2Serializer;
import de.vatterger.entitysystem.tools.serializer.Vector3Serializer;

public class PacketRegister {

	private PacketRegister() {}

	public static void registerClasses(Kryo kryo) {
		kryo.register(Vector3.class, new Vector3Serializer());
		kryo.register(Vector2.class, new Vector2Serializer());
		kryo.register(Circle.class, new CircleSerializer());
		kryo.register(Bag.class, new BagSerializer());
		kryo.register(Object[].class);
		kryo.register(String.class);
		kryo.register(CircleCollision.class);
		kryo.register(Position.class);
		kryo.register(Velocity.class);
		kryo.register(RemoteMasterUpdate.class);
	}
}
