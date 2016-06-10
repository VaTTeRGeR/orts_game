package de.vatterger.techdemo.application;

import com.artemis.World;
import com.artemis.WorldConfiguration;

import de.vatterger.techdemo.handler.network.ServerNetworkHandler;
import de.vatterger.techdemo.interfaces.CreateUpdateDisposeRoutine;
import de.vatterger.techdemo.processors.experimental.TestPopulationProcessor;
import de.vatterger.techdemo.processors.server.CircleCollisionProcessor;
import de.vatterger.techdemo.processors.server.ConnectionProcessor;
import de.vatterger.techdemo.processors.server.DataBucketSendProcessor;
import de.vatterger.techdemo.processors.server.GridMapProcessor;
import de.vatterger.techdemo.processors.server.PingProcessor;
import de.vatterger.techdemo.processors.server.ReceiveEntityAckProcessor;
import de.vatterger.techdemo.processors.server.ReceiveViewportUpdateProcessor;
import de.vatterger.techdemo.processors.server.RemoteMasterGracePeriodProcessor;
import de.vatterger.techdemo.processors.server.RemoteMasterRebuildProcessor;
import de.vatterger.techdemo.processors.server.RemoteMasterSendProcessor;
import de.vatterger.techdemo.processors.server.SpawnTankUpdateProcessor;
import de.vatterger.techdemo.processors.server.TaskPreProcessor;
import de.vatterger.techdemo.processors.server.TurretFindTargetProcessor;
import de.vatterger.techdemo.processors.server.TurretLoseTargetProcessor;
import de.vatterger.techdemo.processors.server.TurretRotateToTargetProcessor;
import de.vatterger.techdemo.processors.server.VelocityToRotationProcessor;
import de.vatterger.techdemo.processors.server.WaypointPathProcessor;
import de.vatterger.techdemo.processors.server.WaypointTargetProcessor;
import de.vatterger.techdemo.processors.shared.DeleteInactiveProcessor;
import de.vatterger.techdemo.processors.shared.DeleteOutOfBoundsProcessor;
import de.vatterger.techdemo.processors.shared.MovementProcessor;

/**
 * The slime world
 * @author Florian Schmickmann
 **/
public class BattleServer implements CreateUpdateDisposeRoutine {

	/**The Artemis-odb world object*/
	private World world;
	
	public BattleServer() {}

	@Override
	public void create() {
		WorldConfiguration worldConfig = new WorldConfiguration();

		/**PREPROCESSOR**/
		worldConfig.setSystem(new TaskPreProcessor()); //Runs Tasks that are Scheduled to run before the simulation

		/**NETWORK INPUT**/
		worldConfig.setSystem(new ConnectionProcessor()); //Creates players and manages connections
		//worldConfig.setSystem(new CVRRegisterProcessor()); //Registers the ComponentVersioningRegister of the clients for entity clearing
		worldConfig.setSystem(new PingProcessor()); // Updates the clients ping measurement
		worldConfig.setSystem(new ReceiveViewportUpdateProcessor()); // Updates the clients input
		worldConfig.setSystem(new SpawnTankUpdateProcessor()); // Creates tanks upon user input

		/**MOVEMENT**/
		worldConfig.setSystem(new WaypointPathProcessor()); // Makes entities select a waypoint on their set path
		worldConfig.setSystem(new WaypointTargetProcessor()); // Makes entities move towards the selected waypoints
		worldConfig.setSystem(new CircleCollisionProcessor()); //Checks for collision between Circles and handles collision
		worldConfig.setSystem(new MovementProcessor()); //Moves entities that have a position and velocity
		worldConfig.setSystem(new VelocityToRotationProcessor()); //Changes the entities rotation to their movement direction angle
		
		/**TURRET**/
		worldConfig.setSystem(new TurretLoseTargetProcessor()); //...
		worldConfig.setSystem(new TurretFindTargetProcessor()); //...
		worldConfig.setSystem(new TurretRotateToTargetProcessor()); //...

		/**LIFECYCLE**/
		worldConfig.setSystem(new TestPopulationProcessor()); //Places a few edibles every tick and many on world init
		worldConfig.setSystem(new DeleteOutOfBoundsProcessor()); //Will delete everything outside of the play-area
		worldConfig.setSystem(new DeleteInactiveProcessor()); //Will delete Entities marked as Inactive after a grace period is over

		/**COLLISION**/
		worldConfig.setSystem(new GridMapProcessor()); //Sorts entities with a position and collision into a spatial data-structure

		/**GATHERING REMOTEMASTER DATA**/
		worldConfig.setSystem(new RemoteMasterGracePeriodProcessor()); // Unlocks delta updates after a certain time per entity
		worldConfig.setSystem(new RemoteMasterRebuildProcessor()); //Fills the RemoteMasters component-bag with relevant component instances
		worldConfig.setSystem(new ReceiveEntityAckProcessor()); //Keeps a list of transmitted entities for every player
		worldConfig.setSystem(new RemoteMasterSendProcessor()); //Packs RemoteMasterUpdates into the clients Databucket to be sent by the DataBucketSendProcessor

		/**DATA SENDING**/
		worldConfig.setSystem(new DataBucketSendProcessor()); //Sends Packets of Data to clients at a rate of 22KByte/s * GameConstants.PACKETS_PER_TICK or less

		world = new World(worldConfig);
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