package de.vatterger.entitysystem;

import static de.vatterger.entitysystem.GameConstants.*;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryonet.Connection;

import de.vatterger.entitysystem.components.ActiveCollision;
import de.vatterger.entitysystem.components.DataBucket;
import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.G3DBModelId;
import de.vatterger.entitysystem.components.Name;
import de.vatterger.entitysystem.components.PassiveCollision;
import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.KryoConnection;
import de.vatterger.entitysystem.components.ServerPosition;
import de.vatterger.entitysystem.components.RemoteMaster;
import de.vatterger.entitysystem.components.RemoteMasterInvalidated;
import de.vatterger.entitysystem.components.Saveable;
import de.vatterger.entitysystem.components.ServerRotation;
import de.vatterger.entitysystem.components.Inactive;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.components.ViewFrustum;
import de.vatterger.entitysystem.gridmapservice.BitFlag;

public class EntityFactory {
	
	private EntityFactory() {}
	
	public static Entity createTank(World world, Vector2 position) {
		Entity e = world.createEntity();
		float vx = MathUtils.random(-1f, 1f), vy = MathUtils.random(-1f, 1f);
		return e.edit()
			.add(new ServerPosition(new Vector3(position.x, position.y, 0f)))
			.add(new Velocity(new Vector3(vx, vy, 0f).nor().scl(MathUtils.random(5f, 10f))))
			.add(new CircleCollision(TANK_COLLISION_RADIUS, e))
			.add(new ActiveCollision())
			.add(new G3DBModelId(0))
			.add(new ServerRotation())
			.add(new RemoteMaster(ServerPosition.class, G3DBModelId.class, ServerRotation.class))
			.add(new RemoteMasterInvalidated())
			.add(new Flag(new BitFlag(BitFlag.COLLISION|BitFlag.NETWORKED|BitFlag.ACTIVE)))
		.getEntity();
	}

	public static Entity createStaticTank(World world, Vector2 position) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new ServerPosition(new Vector3(position.x, position.y, 0f)))
			.add(new CircleCollision(TANK_COLLISION_RADIUS, e))
			.add(new PassiveCollision())
			.add(new G3DBModelId(0))
			.add(new ServerRotation(0f))
			.add(new RemoteMaster(ServerPosition.class, G3DBModelId.class, ServerRotation.class))
			.add(new RemoteMasterInvalidated())
			.add(new Flag(new BitFlag(BitFlag.COLLISION|BitFlag.NETWORKED|BitFlag.STATIC|BitFlag.ACTIVE)))
		.getEntity();
	}

	public static void deactivateEntity(Entity e) {
		Flag flag = e.getComponent(Flag.class);
		if(flag != null) {
			flag.flag.removeFlag(BitFlag.ACTIVE);
		}
		e.edit().add(new Inactive());
	}
	
	@SafeVarargs
	public static void stripComponentsExcept(Entity e, Class<? extends Component> ...exceptClazz) {
		EntityEdit ed = e.edit();
		Bag<Component> components = new Bag<Component>(8);
		for (int i = 0; i < components.size(); i++) {
			Component c = components.get(i);
			if(exceptClazz != null) {
				boolean remove = true;
				for (int j = 0; j < exceptClazz.length; j++) {
					if (exceptClazz[j].isInstance(c)) {
						remove = false;
					}
				}
				if (remove) {
					ed.remove(c);
				}
			} else {
				ed.remove(c);
			}
		}
	}
	
	public static void stripComponents(Entity e) {
		EntityEdit ed = e.edit();
		Bag<Component> components = new Bag<Component>(8);
		e.getComponents(components);
		for (int i = 0; i < components.size(); i++) {
			ed.remove(components.get(i));
		}
	}
	
	public static boolean hasComponent(Entity e, Class<? extends Component> clazz) {
		return e.getComponent(clazz) != null;
	}
	
	public static Entity createPlayer(World world, Connection c) {
		return world.createEntity().edit()
			.add(new KryoConnection(c))
			.add(new DataBucket())
			.add(new Name("#Player "+c))
			.add(new ViewFrustum(new Rectangle()))
		.getEntity();
	}

	public static Entity createBulletEffect(World world, Vector3 position, Vector3 speed) {
		Entity e = world.createEntity();
		return e.edit()
			.add(new ServerPosition(new Vector3(position)))
			.add(new Velocity(new Vector3(speed)))
			.add(new ServerRotation(0f))
			.add(new G3DBModelId(0))
			.add(new Flag(new BitFlag(BitFlag.ACTIVE)))
		.getEntity();
	}
}
