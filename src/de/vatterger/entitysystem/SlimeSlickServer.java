package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import de.vatterger.entitysystem.components.CircleCollision;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.interfaces.SavableWorld;
import de.vatterger.entitysystem.tools.EntitySerializationBag;
import de.vatterger.entitysystem.tools.Profiler;
import de.vatterger.entitysystem.tools.SlimeSlickFactory;
import de.vatterger.entitysytem.processors.CircleContainmentProcessor;
import de.vatterger.entitysytem.processors.ContainmentProcessor;
import de.vatterger.entitysytem.processors.MovementProcessor;
import de.vatterger.entitysytem.processors.SaveEntityProcessor;
import de.vatterger.entitysytem.processors.SlimeAbsorbProcessor;

public class SlimeSlickServer implements SavableWorld{

	/**The Artemis-odb world object*/
	private World world;
	private final static int RANGE = 5000; 

	public SlimeSlickServer() {
	}
	
	@Override
	public void create() throws Exception {

		world = new World();

		
		world.setSystem(new MovementProcessor());
		world.setSystem(new CircleContainmentProcessor());
		world.setSystem(new SlimeAbsorbProcessor());
		world.setSystem(new ContainmentProcessor(RANGE, RANGE));
				
		world.initialize();
		
		for (int i = 0; i < 30000; i++) {
			Entity e = SlimeSlickFactory.createSlime(world, new Vector3(MathUtils.random(0f,RANGE),MathUtils.random(0f,RANGE),0f));
			e.getComponent(Velocity.class).vel.set(MathUtils.random(-100, 100), MathUtils.random(-100, 100), 0);
			e.getComponent(CircleCollision.class).circle.radius = MathUtils.random(0.5f, 10);
		}

		//load();
	}
	
	@Override
	public void update(final float delta) {
		world.setDelta(delta);
		world.process();
		
		if(MathUtils.random(0f, 1f) > 0.9f) {
			System.out.println("Entities: "+world.getEntityManager().getActiveEntityCount());
		}
	}

	@Override
	public void dispose() {
		save();
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
			System.out.println("Could not save state, no SaveEntityProcessor found!");
		}
	}
}