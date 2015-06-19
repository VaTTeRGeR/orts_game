package de.vatterger.entitysystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonet.Server;

import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.Velocity;
import de.vatterger.entitysystem.interfaces.World;
import de.vatterger.threadedSim.entitysytem.processors.MovementProcessor;
import de.vatterger.threadedSim.entitysytem.processors.TestComponentPerformanceProcessor;
import de.vatterger.threadedSim.tools.Profiler;

public class EntitySystemWorld implements World{

	private Engine engine;
	private Kryo kryo;
	private Server server;
	
	@Override
	public void create() throws Exception {
		engine = new Engine();
		kryo = new Kryo();
		server = new Server(256,256);
		try {
			server.bind(26000);
		} catch (Exception e) {
			e.printStackTrace();
			server.close();
			System.exit(1);
		}
		
		final Profiler p = new Profiler("Loading Entities.");

		try {
			Input in = new Input(new GZIPInputStream(new FileInputStream("data/kryo.gzip")));
			engine.addEntities(kryo.readObject(in, Entity[].class));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		p.logTimeElapsed();
		
		//engine.addSystem(new MovementProcessor());
		engine.addSystem(new TestComponentPerformanceProcessor());

		engine.removeAllEntities();
		final int n = 100000;
		for (int i = 0; i < n; i++) {
			engine.addEntity(new Entity().add(new Position()).add(new Velocity()));
		}
		
		System.out.println("Entities loaded: "+engine.getEntities().size()+"\n");
	}
	
	@Override
	public void update(float delta) {
		final int n = 0;
		for (int i = 0; i < n; i++) {
			engine.addEntity(new Entity().add(new Position()).add(new Velocity()));
		}
		engine.update(delta);
	}

	@Override
	public void dispose() {
		server.close();
		final Profiler p = new Profiler("Entities saved: "+engine.getEntities().size());
		try {
			Output out = new Output(new GZIPOutputStream(new FileOutputStream("data/kryo.gzip")));
			kryo.writeObject(out, engine.getEntities().toArray());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		p.logTimeElapsed();
		engine.removeAllEntities();
	}
}
