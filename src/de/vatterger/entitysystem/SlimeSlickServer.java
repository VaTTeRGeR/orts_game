package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.interfaces.SavableWorld;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.processors.ClientInputProcessor;
import de.vatterger.entitysystem.processors.ConnectionProcessor;
import de.vatterger.entitysystem.processors.TestPopulationProcessor;
import de.vatterger.entitysystem.util.EntitySerializationBag;
import de.vatterger.entitysystem.util.Timer;
import de.vatterger.entitysystem.util.profile.Profiler;
import de.vatterger.entitysystem.processors.RemoteMasterRebuildProcessor;
import de.vatterger.entitysystem.processors.GridMapProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterSendProcessor;
import de.vatterger.entitysystem.processors.DataBucketSendProcessor;
import de.vatterger.entitysystem.processors.DeleteInactiveProcessor;
import de.vatterger.entitysystem.processors.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.SaveEntityProcessor;
import de.vatterger.entitysystem.processors.SlimeCollisionProcessor;

/**
 * The slime world
 * @author Florian Schmickmann
 **/
public class SlimeSlickServer implements SavableWorld {

	/**The Artemis-odb world object*/
	private World world;
	/**The Entity-Count is printed once every second*/
	private Timer printEC_Timer = new Timer(1f);

	public SlimeSlickServer() {
	}

	@Override
	public void create() throws Exception {

		world = new World();

		world.setSystem(new ConnectionProcessor());//Creates players and manages connections
		world.setSystem(new ClientInputProcessor());// Updates the clients input

		world.setSystem(new TestPopulationProcessor());//Places a few edibles every tick and many on world init
		world.setSystem(new SlimeCollisionProcessor());//Checks for collision between Slimes and handles absorbtion
		world.setSystem(new MovementProcessor());//Moves entities that have a position and velocity
		world.setSystem(new DeleteOutOfBoundsProcessor());//Will delete everything outside of the play-area
		world.setSystem(new DeleteInactiveProcessor());//Will delete Entities marked as Inactive after a grace period is over

		world.setSystem(new GridMapProcessor());//Sorts entities with a position and collision into a spatial data-structure

		world.setSystem(new RemoteMasterRebuildProcessor());//Fills the RemoteMasters component-bag with relevant component instances
		world.setSystem(new RemoteMasterSendProcessor());//Packs RemoteMasterUpdates into the clients Databucket

		world.setSystem(new DataBucketSendProcessor());//Sends Packets to clients at a steady rate

		world.initialize();
	}

	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();

		if(printEC_Timer.tick(delta)) {
			Main.printConsole("Entities: "+world.getEntityManager().getActiveEntityCount());
			printEC_Timer.reset();
		}
	}

	@Override
	public void dispose() {
		//save();
		world.dispose();
		NetworkService.dispose();
	}

	/**
	 * Loads the gzip compressed system-state from "[PROJECT FOLDER]/data/kryo.gzip"
	 **/
	@Override
	public void load() {
		if (new FileHandle("data/kryo.gzip").exists()) {
			Kryo kryo = new Kryo();
			final Profiler p = new Profiler("Loading Entities.");
			try {
				Input in = new Input(new GZIPInputStream(new FileInputStream("data/kryo.gzip")));
				EntitySerializationBag entities = kryo.readObject(in, EntitySerializationBag.class);
				System.out.println("Entities loaded: "+entities.size());
				entities.loadEntities(world);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			p.log();
		}
	}
	
	/**
	 * Saves a gzip compressed system-state at "[PROJECT FOLDER]/data/kryo.gzip"
	 **/
	@Override
	public void save() {
		if(world.getSystem(SaveEntityProcessor.class) != null) {
			world.getSystem(SaveEntityProcessor.class).process();
		} else {
			System.out.println("Did not save state, no SaveEntityProcessor found!");
		}
	}
}