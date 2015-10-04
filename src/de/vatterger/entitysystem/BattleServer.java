package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.EntityObserver;
import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.interfaces.UpdateableWorld;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.processors.ClientInputProcessor;
import de.vatterger.entitysystem.processors.ConnectionProcessor;
import de.vatterger.entitysystem.util.EntitySerializationBag;
import de.vatterger.entitysystem.util.profile.Profiler;
import de.vatterger.entitysystem.processors.RemoteMasterRebuildProcessor;
import de.vatterger.entitysystem.processors.GridMapProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterSendProcessor;
import de.vatterger.entitysystem.processors.DataBucketSendProcessor;
import de.vatterger.entitysystem.processors.DeleteInactiveProcessor;
import de.vatterger.entitysystem.processors.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.SaveEntityProcessor;
import de.vatterger.entitysystem.processors.CircleCollisionProcessor;
import de.vatterger.entitysystem.processors.SteeringProcessor;
import de.vatterger.entitysystem.processors_test.TestPopulationProcessor;

/**
 * The slime world
 * @author Florian Schmickmann
 **/
public class BattleServer implements UpdateableWorld{

	/**The Artemis-odb world object*/
	private World world;

	public BattleServer() {
	}

	@Override
	public void create() throws Exception {
		world = new World();

		world.setSystem(new ConnectionProcessor()); //Creates players and manages connections
		world.setSystem(new ClientInputProcessor()); // Updates the clients input


		world.setSystem(new CircleCollisionProcessor()); //Checks for collision between Slimes and handles collision
		world.setSystem(new SteeringProcessor()); //Changes the entities path
		world.setSystem(new MovementProcessor()); //Moves entities that have a position and velocity

		world.setSystem(new TestPopulationProcessor()); //Places a few edibles every tick and many on world init
		world.setSystem(new DeleteOutOfBoundsProcessor()); //Will delete everything outside of the play-area
		world.setSystem(new DeleteInactiveProcessor()); //Will delete Entities marked as Inactive after a grace period is over

		world.setSystem(new GridMapProcessor()); //Sorts entities with a position and collision into a spatial data-structure

		world.setSystem(new RemoteMasterRebuildProcessor()); //Fills the RemoteMasters component-bag with relevant component instances
		world.setSystem(new RemoteMasterSendProcessor()); //Packs RemoteMasterUpdates into the clients Databucket

		world.setSystem(new DataBucketSendProcessor()); //Sends Packets to clients at a steady rate

		world.initialize();
	}

	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();
	}

	@Override
	public void dispose() {
		world.dispose();
		NetworkService.dispose();
	}
}