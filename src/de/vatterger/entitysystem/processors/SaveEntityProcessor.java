package de.vatterger.entitysystem.processors;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.Saveable;
import de.vatterger.entitysystem.util.EntitySerializationBag;
import de.vatterger.entitysystem.util.profile.Profiler;

public class SaveEntityProcessor extends IntervalEntityProcessingSystem {

	EntitySerializationBag bag = new EntitySerializationBag();
	Kryo kryo;
	int count;

	@SuppressWarnings("unchecked")
	public SaveEntityProcessor(float interval) {
		super(Aspect.getAspectForAll(Saveable.class), interval);
	}
	
	@Override
	protected void inserted(Entity e) {
		count++;
	}
	
	@Override
	protected void removed(Entity e) {
		count--;
	}

	@Override
	protected void initialize() {
		count = 0;
		kryo = new Kryo();
		load();
	}
	
	@Override
	protected void begin() {
		bag = new EntitySerializationBag(count);
	}
	
	@Override
	protected void process(Entity e) {
		bag.saveEntity(e, world);
	}
	
	@Override
	protected void end() {
		try {
			Output out = new Output(new GZIPOutputStream(new FileOutputStream("data/kryo.gzip"), 64 * 1024));
			kryo.writeObject(out, bag);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void load() {
		if (new FileHandle("data/kryo.gzip").exists()) {
			try {
				Input in = new Input(new GZIPInputStream(new FileInputStream("data/kryo.gzip")));
				EntitySerializationBag entities = kryo.readObject(in, EntitySerializationBag.class);
				System.out.println("Entities loaded: "+entities.size());
				entities.loadEntities(world);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
