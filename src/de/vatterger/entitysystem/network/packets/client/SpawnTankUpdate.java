package de.vatterger.entitysystem.network.packets.client;

import com.badlogic.gdx.math.Vector2;

public class SpawnTankUpdate {
	public Vector2 vec;
	public SpawnTankUpdate() {
	}
	
	public SpawnTankUpdate(Vector2 vec) {
		this.vec = vec;
	}
}