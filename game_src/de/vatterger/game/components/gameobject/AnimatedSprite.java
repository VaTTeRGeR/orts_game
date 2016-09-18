package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.math.Interpolation;

public class AnimatedSprite extends Component {
	public float scaleTime;
	public Interpolation interpolation;

	public float scaleProgress;

	public AnimatedSprite(float scaleTime, Interpolation interpolation) {
		this.scaleTime = scaleTime;
		this.interpolation = interpolation;
		this.scaleProgress = 0;
	}
}
