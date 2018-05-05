package de.vatterger.game.ui;

import com.badlogic.gdx.scenes.scene2d.Action;

public class FadeAction extends Action {
	
	private final float change_alpha;
	private final float target_alpha;
	private float current_alpha;
	
	public FadeAction(float time, float start_alpha, float target_alpha) {
		
		start_alpha = Math.max(0f, start_alpha);
		start_alpha = Math.min(1f, start_alpha);
		
		target_alpha = Math.max(0f, target_alpha);
		target_alpha = Math.min(1f, target_alpha);
		
		this.target_alpha = target_alpha;
		
		change_alpha = (target_alpha - start_alpha) / time;
		
		current_alpha = start_alpha;
	}
	
	@Override
	public boolean act(float delta) {
		actor.getColor().a = current_alpha;
		
		current_alpha += delta * change_alpha;
		
		if(change_alpha > 0 ? current_alpha >= target_alpha : current_alpha <= target_alpha) {
			actor.getColor().a = target_alpha;
			
			run();
			return true;
		}
		
		idle();
		return false;
	}
	
	public void run() {}
	
	public void idle() {}
}
