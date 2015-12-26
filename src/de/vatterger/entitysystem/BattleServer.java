package de.vatterger.entitysystem;

import com.artemis.World;
import com.artemis.WorldConfiguration;

import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.interfaces.UpdateableWorld;
import de.vatterger.entitysystem.processors.ReceiveViewportUpdateProcessor;
import de.vatterger.entitysystem.processors.ReceiveEntityAckProcessor;
import de.vatterger.entitysystem.processors.ConnectionProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterRebuildProcessor;
import de.vatterger.entitysystem.processors.GridMapProcessor;
import de.vatterger.entitysystem.processors.WaypointTargetProcessor;
import de.vatterger.entitysystem.processors.experimental.TestPopulationProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterSendProcessor;
import de.vatterger.entitysystem.processors.TaskPreProcessor;
import de.vatterger.entitysystem.processors.DataBucketSendProcessor;
import de.vatterger.entitysystem.processors.DeleteInactiveProcessor;
import de.vatterger.entitysystem.processors.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.CircleCollisionProcessor;
import de.vatterger.entitysystem.processors.VelocityToRotationProcessor;
import de.vatterger.entitysystem.processors.WaypointPathProcessor;

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
		WorldConfiguration worldConfig = new WorldConfiguration();
		
		world = new World(worldConfig);

		/**PREPROCESSOR**/
		world.setSystem(new TaskPreProcessor()); //Runs Tasks that are Scheduled to run before the simulation

		/**INPUT**/
		world.setSystem(new ConnectionProcessor()); //Creates players and manages connections
		world.setSystem(new ReceiveViewportUpdateProcessor()); // Updates the clients input

		/**MOVEMENT**/
		world.setSystem(new WaypointPathProcessor()); // Makes entities select a waypoint on their set path
		world.setSystem(new WaypointTargetProcessor()); // Makes entities move towards the selected waypoints
		world.setSystem(new CircleCollisionProcessor()); //Checks for collision between Circles and handles collision
		world.setSystem(new MovementProcessor()); //Moves entities that have a position and velocity
		world.setSystem(new VelocityToRotationProcessor()); //Changes the entities rotation to their movement direction angle

		/**LIFECYCLE**/
		world.setSystem(new TestPopulationProcessor()); //Places a few edibles every tick and many on world init
		world.setSystem(new DeleteOutOfBoundsProcessor()); //Will delete everything outside of the play-area
		world.setSystem(new DeleteInactiveProcessor()); //Will delete Entities marked as Inactive after a grace period is over

		/**COLLISION**/
		world.setSystem(new GridMapProcessor()); //Sorts entities with a position and collision into a spatial data-structure

		/**GATHERING REMOTEMASTER DATA**/
		world.setSystem(new RemoteMasterRebuildProcessor()); //Fills the RemoteMasters component-bag with relevant component instances
		world.setSystem(new ReceiveEntityAckProcessor()); //Keeps a list of transmitted entities
		world.setSystem(new RemoteMasterSendProcessor()); //Packs RemoteMasterUpdates into the clients Databucket

		/**DATA SENDING**/
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
		ServerNetworkHandler.dispose();
	}
}