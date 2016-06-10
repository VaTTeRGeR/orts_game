package de.vatterger.techdemo.components.shared;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.techdemo.interfaces.Sizeable;
import de.vatterger.techdemo.interfaces.Versionable;

public class Velocity extends Component implements Versionable, Sizeable{
	public Vector3 vel = new Vector3(Vector3.Zero);

	public Velocity(){}

	public Velocity(Vector3 vel) {
		this.vel = vel;
	}
	
	private int v = 0;
	@Override
	public void newVersion() {
		v++;
		if(v == Integer.MAX_VALUE) {
			v = 0;
		}
	}

	@Override
	public int getVersion() {
		return v;
	}

	@Override
	public boolean compareVersion(int v2) {
		return v != v2;
	}

	@Override
	public int getSizeInBytes() {
		return 12 + 1; //3 x float + overhead
	}
}
