package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.interfaces.SavableWorld;
import de.vatterger.entitysystem.processors.CircleContainmentProcessor;
import de.vatterger.entitysystem.processors.ContainmentProcessor;
import de.vatterger.entitysystem.processors.MovementProcessor;
import de.vatterger.entitysystem.processors.SaveEntityProcessor;
import de.vatterger.entitysystem.processors.SlimeAbsorbProcessor;
import de.vatterger.entitysystem.tools.EntitySerializationBag;
import de.vatterger.entitysystem.tools.Profiler;
import de.vatterger.entitysystem.tools.SlimeSlickFactory;

public class SlimeSlickServer implements SavableWorld{

	/**The Artemis-odb world object*/
	private World world;
	/**The maxiumum x and y values that the playable area extends to from [0,0]*/
	private final static int XY_BOUNDS = 1024;

	public SlimeSlickServer() {
	}
	
	@Override
	public void create() throws Exception {

		world = new World();

		
		world.setSystem(new MovementProcessor());//Moves entities as long as they have a position and velocity
		world.setSystem(new CircleContainmentProcessor());//Checks for collision between circles
		world.setSystem(new SlimeAbsorbProcessor());//Slimes will eat each other
		world.setSystem(new ContainmentProcessor(XY_BOUNDS, XY_BOUNDS));//Will delete everything outside of the Rectangle [0,0,RANGE,RANGE]

		world.initialize();

		//load();
	}
	
	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();
		
		final int n = 0;
		for (int i = 0; i < n; i++) {
			SlimeSlickFactory.createSlime(world, new Vector3(MathUtils.random(0,XY_BOUNDS), MathUtils.random(0,XY_BOUNDS), 0));
		}
		for (int i = 0; i < n; i++) {
			SlimeSlickFactory.createSmallEdible(world, new Vector3(MathUtils.random(0,XY_BOUNDS), MathUtils.random(0,XY_BOUNDS), 0));
		}
		
		if(MathUtils.random(0f, 1f) > 0.8f) {
			System.out.println("Entities: "+world.getEntityManager().getActiveEntityCount());
		}
	}

	@Override
	public void dispose() {
		//save();
		world.dispose();
	}
	
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
	
	@Override
	public void save() {
		if(world.getSystem(SaveEntityProcessor.class) != null) {
			world.getSystem(SaveEntityProcessor.class).process();
		} else {
			System.out.println("Did not save state, no SaveEntityProcessor found!");
		}
	}
}