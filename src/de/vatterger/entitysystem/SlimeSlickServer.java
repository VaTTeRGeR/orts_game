package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.interfaces.SavableWorld;
import de.vatterger.entitysystem.processors.CircleContainmentProcessor;
import de.vatterger.entitysystem.processors.DeleteOutOfBoundsProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.SaveEntityProcessor;
import de.vatterger.entitysystem.processors.SlimeAbsorbProcessor;
import de.vatterger.entitysystem.tools.EntitySerializationBag;
import de.vatterger.entitysystem.tools.Profiler;
import de.vatterger.entitysystem.tools.SlimeSlickFactory;
import de.vatterger.entitysystem.tools.Timer;

import static com.badlogic.gdx.math.MathUtils.*;

/**
 * The slime world
 * @author Florian Schmickmann
 **/
public class SlimeSlickServer implements SavableWorld {

	/**The Artemis-odb world object*/
	private World world;
	/**The maxiumum x and y values that the playable area extends to from [0,0]*/
	public final static int XY_BOUNDS = 512;
	/**The Entity-Count is printed once every second*/
	private Timer printEC_Counter = new Timer(1f);

	public SlimeSlickServer() {
	}

	@Override
	public void create() throws Exception {

		world = new World();

		world.setSystem(new MovementProcessor());//Moves entities as long as they have a position and velocity
		world.setSystem(new CircleContainmentProcessor());//Checks for collision between circles
		world.setSystem(new SlimeAbsorbProcessor());//Slimes will eat each other
		world.setSystem(new DeleteOutOfBoundsProcessor());//Will delete everything outside of the Rectangle [0,0,RANGE,RANGE]

		world.initialize();

		for (int i = 0; i < 50000; i++) {
			SlimeSlickFactory.createSmallEdible(world, new Vector3(random(0, XY_BOUNDS), random(0, XY_BOUNDS), 0));
		}
		//load();
	}

	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();
		
		final int n = 0;
		for (int i = 0; i < n; i++) {
			SlimeSlickFactory.createSlime(world, new Vector3(random(0,XY_BOUNDS), random(0,XY_BOUNDS), 0));
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
			p.logTimeElapsed();
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