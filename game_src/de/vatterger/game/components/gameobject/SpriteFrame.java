package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class SpriteFrame extends Component {
	public int currentframe;
	public int numFrames;
	public float currentIntervalLeft;
	public float interval;
	public boolean loop;

	public SpriteFrame() {
		this(0, 1, 20, true);
	}
	
	public SpriteFrame(int currentFrame, int maxFrame, float interval, boolean loop) {
		this.currentframe = currentFrame;
		this.numFrames = maxFrame;
		this.interval = currentIntervalLeft = interval;
		this.loop = loop;
	}
}
