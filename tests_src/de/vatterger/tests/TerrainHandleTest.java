package de.vatterger.tests;

import java.io.IOException;

import de.vatterger.engine.handler.terrain.TerrainHandle;

public class TerrainHandleTest {

	public static void main (String[] args) throws IOException, InterruptedException {
		TerrainHandle handle = new TerrainHandle("assets/terrain/test", 0f, 0f);
		
		handle.load(0);
		handle.load(1);
		
		handle.finishLoading();
		
		System.out.println("Tile-1: " + handle.getTile(1));
		
		Thread.sleep(10);
		
		handle.finishLoading();
		
		System.out.println("Tile-1: " + handle.getTile(1));
		
		handle.unload(0, true);
		handle.unload(1, true);
		
		handle.dispose();
	}
}
