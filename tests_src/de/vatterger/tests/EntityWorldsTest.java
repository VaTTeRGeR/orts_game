
package de.vatterger.tests;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.math.MathUtils;

public class EntityWorldsTest {

	static World worldA;
	static World worldB;

	public static void main (String[] args) throws InterruptedException {

		WorldConfiguration wca = new WorldConfiguration();
		wca.setSystem(new SwitchWorldSystem("A", true));

		WorldConfiguration wcb = new WorldConfiguration();
		wcb.setSystem(new SwitchWorldSystem("B", false));

		worldA = new World(wca);
		
		createUuidEntity(worldA);
		createUuidEntity(worldA);
		createUuidEntity(worldA);
		createUuidEntity(worldA);

		worldB = new World(wcb);
		
		while (true) {
			worldA.process();
			worldB.process();
			
			System.out.println();
			
			Thread.sleep(1000);
		}
	}
	
	static int uuidCounter = 0;
	private static void createUuidEntity(World world) {
		int entityId = world.create();
		world.edit(entityId).add(new UuidComponent(uuidCounter++));
	}

	private static class SwitchWorldSystem extends IteratingSystem {

		ComponentMapper<UuidComponent> uuidMapper;
		
		String name;

		boolean isA;

		public SwitchWorldSystem (String name, boolean isA) {

			super(Aspect.all(UuidComponent.class));

			this.name = name;
			this.isA = isA;
		}

		@Override
		protected void inserted (int entityId) {
			System.out.println("World " + name + " received " + uuidMapper.get(entityId).uuid);
		}
		
		@Override
		protected void removed (int entityId) {
			System.out.println("World " + name + " lost " + uuidMapper.get(entityId).uuid);
		}
		
		@Override
		protected void process (int entityId) {

			World own;
			World other;

			if (isA) {
				own = worldA;
				other = worldB;
			} else {
				own = worldB;
				other = worldA;
			}

			if (MathUtils.randomBoolean(0.5f)) {

				System.out.println("Moving " + entityId + " from " + name + " to other world...");
				System.out.println("\t[1] Collecting Components");

				Bag<Component> components = new Bag<>(16);
				own.getComponentManager().getComponentsFor(entityId, components);

				System.out.println("\t[2] Removing Entity");
				own.delete(entityId);
				
				System.out.println("\t[3] Creating new Entity");
				int newEntityId = other.create();

				System.out.println("\t[4] Adding Components to new Entity");
				EntityEdit edit = other.edit(newEntityId);
				int i = 1;
				for (Component component : components) {
					System.out.println("\t\t [4."+(i++)+"] Adding " + component.getClass().getSimpleName());
					edit.add(component);
				}

				System.out.println("\t[5] Done!");
			}
		}
	}
	
	public static class UuidComponent extends Component {
		
		public int uuid;
		
		public UuidComponent () {}
		
		public UuidComponent (int uuid) {
			this.uuid = uuid;
		}
	}

}
