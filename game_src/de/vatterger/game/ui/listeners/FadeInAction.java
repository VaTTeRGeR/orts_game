package de.vatterger.game.ui.listeners;

import com.badlogic.gdx.scenes.scene2d.Action;

public class FadeInAction extends Action {
	
	float alpha;
	float scl;
	boolean wait;
	
	public FadeInAction(float time) {
		scl = 1f/time;
		alpha = 0f;
		wait = true;
	}
	
	@Override
	public boolean act(float delta) {
		actor.getColor().a = alpha;
		alpha += delta * scl;
		if(alpha >= 1) {
			actor.getColor().a = 1f;
			run();
			return true;
		}
		return false;
	}
	
	public void run() {}
}
