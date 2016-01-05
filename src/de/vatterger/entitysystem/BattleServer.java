package de.vatterger.entitysystem;

import com.artemis.World;
import com.artemis.WorldConfiguration;

import de.vatterger.entitysystem.handler.network.ServerNetworkHandler;
import de.vatterger.entitysystem.interfaces.UpdateableWorld;
import de.vatterger.entitysystem.processors.experimental.TestPopulationProcessor;
import de.vatterger.entitysystem.processors.server.CircleCollisionProcessor;
import de.vatterger.entitysystem.processors.server.ConnectionProcessor;
import de.vatterger.entitysystem.processors.server.DataBucketSendProcessor;
import de.vatterger.entitysystem.processors.server.GridMapProcessor;
import de.vatterger.entitysystem.processors.server.ReceiveEntityAckProcessor;
import de.vatterger.entitysystem.processors.server.ReceiveViewportUpdateProcessor;
import de.vatterger.entitysystem.processors.server.RemoteMasterRebuildProcessor;
import de.vatterger.entitysystem.processors.server.RemoteMasterSendProcessor;
import de.vatterger.entitysystem.processors.server.TaskPreProcessor;
import de.vatterger.entitysystem.processors.server.TurretFindTargetProcessor;
import de.vatterger.entitysystem.processors.server.TurretLoseTargetProcessor;
import de.vatterger.entitysystem.processors.server.TurretRotateToTargetProcessor;
import de.vatterger.entitysystem.processors.server.PingProcessor;
import de.vatterger.entitysystem.processors.server.VelocityToRotationProcessor;
import de.vatterger.entitysystem.processors.server.WaypointPathProcessor;
import de.vatterger.entitysystem.processors.server.WaypointTargetProcessor;
import de.vatterger.entitysystem.processors.shared.DeleteInactiveProcessor;
import de.vatterger.entitysystem.processors.shared.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.shared.MovementProcessor;

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

		/**NETWORK INPUT**/
		world.setSystem(new ConnectionProcessor()); //Creates players and manages connections
		world.setSystem(new PingProcessor()); //Creates players and manages connections
		world.setSystem(new ReceiveViewportUpdateProcessor()); // Updates the clients input

		/**MOVEMENT**/
		world.setSystem(new WaypointPathProcessor()); // Makes entities select a waypoint on their set path
		world.setSystem(new WaypointTargetProcessor()); // Makes entities move towards the selected waypoints
		world.setSystem(new CircleCollisionProcessor()); //Checks for collision between Circles and handles collision
		world.setSystem(new MovementProcessor()); //Moves entities that have a position and velocity
		world.setSystem(new VelocityToRotationProcessor()); //Changes the entities rotation to their movement direction angle
		
		/**TURRET**/
		world.setSystem(new TurretLoseTargetProcessor()); //...
		world.setSystem(new TurretFindTargetProcessor()); //...
		world.setSystem(new TurretRotateToTargetProcessor()); //...

		/**LIFECYCLE**/
		world.setSystem(new TestPopulationProcessor()); //Places a few edibles every tick and many on world init
		world.setSystem(new DeleteOutOfBoundsProcessor()); //Will delete everything outside of the play-area
		world.setSystem(new DeleteInactiveProcessor()); //Will delete Entities marked as Inactive after a grace period is over

		/**COLLISION**/
		world.setSystem(new GridMapProcessor()); //Sorts entities with a position and collision into a spatial data-structure

		/**GATHERING REMOTEMASTER DATA**/
		world.setSystem(new RemoteMasterRebuildProcessor()); //Fills the RemoteMasters component-bag with relevant component instances
		world.setSystem(new ReceiveEntityAckProcessor()); //Keeps a list of transmitted entities for every player
		world.setSystem(new RemoteMasterSendProcessor()); //Packs RemoteMasterUpdates into the clients Databucket to be sent by the DataBucketSendProcessor

		/**DATA SENDING**/
		world.setSystem(new DataBucketSendProcessor()); //Sends Packets of Data to clients at a steady rate of 22KByte/s*PACKETS_PER_TICK
		
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