package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteFrame extends Component {
	public int currentframe;
	public int numFrames;
	public float currentIntervalLeft;
	public float interval;

	public SpriteFrame() {
		this(0, 1, 20);
	}
	
	public SpriteFrame(int currentFrame, int maxFrame, float interval) {
		this.currentframe = currentFrame;
		this.numFrames = maxFrame;
		this.interval = currentIntervalLeft = interval;
	}
}
