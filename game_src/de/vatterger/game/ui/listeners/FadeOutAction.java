package de.vatterger.game.ui.listeners;

import com.badlogic.gdx.scenes.scene2d.Action;

public class FadeOutAction extends Action {
	
	float alpha;
	float scl;
	
	public FadeOutAction(float time) {
		scl = 1f/time;
		alpha = 1f;
	}
	
	@Override
	public boolean act(float delta) {
		actor.getColor().a = alpha;
		alpha -= delta * scl;
		if(alpha <= 0) {
			run();
			return true;
		}
		return false;
	}
	
	public void run() {}
}
