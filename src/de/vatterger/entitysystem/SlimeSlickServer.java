package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.interfaces.SavableWorld;
import de.vatterger.entitysystem.netservice.NetworkService;
import de.vatterger.entitysystem.processors.ConnectionProcessor;
import de.vatterger.entitysystem.processors.TestPopulationProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterRebuildProcessor;
import de.vatterger.entitysystem.processors.SlimeCollisionProcessor;
import de.vatterger.entitysystem.processors.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.DataBucketSendProcessor;
import de.vatterger.entitysystem.processors.RemoteMasterMappingProcessor;
import de.vatterger.entitysystem.processors.SaveEntityProcessor;
import de.vatterger.entitysystem.tools.EntitySerializationBag;
import de.vatterger.entitysystem.tools.GameConstants;
import de.vatterger.entitysystem.tools.Profiler;
import de.vatterger.entitysystem.tools.SlimeSlickFactory;
import de.vatterger.entitysystem.tools.Timer;

/**
 * The slime world
 * @author Florian Schmickmann
 **/
public class SlimeSlickServer implements SavableWorld {

	/**The Artemis-odb world object*/
	private World world;
	/**The Entity-Count is printed once every second*/
	private Timer printEC_Counter = new Timer(5f);
	
	public SlimeSlickServer() {
	}

	@Override
	public void create() throws Exception {
		world = new World();

		world.setSystem(new TestPopulationProcessor());//Places a few edibles every tick and many on world init
		world.setSystem(new MovementProcessor());//Moves entities that have a position and velocity
		world.setSystem(new DeleteOutOfBoundsProcessor());//Will delete everything outside of the play-area
		world.setSystem(new SlimeCollisionProcessor());//Checks for collision between Slimes and handles absorbtion
		
		world.setSystem(new RemoteMasterRebuildProcessor());//Fills the RemoteMasters component-bag with relevant component instances
		world.setSystem(new RemoteMasterMappingProcessor());//Sorts Networked-Entities into a spatial data-structure according to their state
		world.setSystem(new DataBucketSendProcessor());//Sends Networked entities to the individual Clients
		world.setSystem(new ConnectionProcessor());//Creates players and manages connections

		world.initialize();
	}

	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();
		
		for (int i = 0; i < GameConstants.EDIBLE_CREATE_PER_TICK; i++) {
			SlimeSlickFactory.createSmallEdible(world, new Vector2(MathUtils.random(0, GameConstants.XY_BOUNDS),MathUtils.random(0, GameConstants.XY_BOUNDS)));
		}
		
		if(printEC_Counter.tick(delta)) {
			System.out.println("Entities: "+world.getEntityManager().getActiveEntityCount());
			printEC_Counter.reset();
		}
	}

	@Override
	public void dispose() {
		//save();
		world.dispose();
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