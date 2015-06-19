package de.vatterger.entitysystem.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class Velocity implements Component{
	public Vector3 vel = new Vector3(Vector3.Zero);
}
