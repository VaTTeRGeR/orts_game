package de.vatterger.game.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

import de.vatterger.game.screens.manager.ScreenManager;

public abstract class FadeActorListener implements EventListener {
	
	Actor actor;
	
	public FadeActorListener(Actor actor) {
		this.actor = actor;
	}
	
	@Override
	public boolean handle(Event event) {
		if(Gdx.input.justTouched()) {
			actor.addAction(new Action() {
				float alpha = 1f;
				float scl = 5f;
				@Override
				public boolean act(float delta) {
					actor.getColor().a = alpha;
					alpha -= delta * scl;
					if(alpha <= 0) {
						actor.getColor().a = 1f;
						run();
						return true;
					}
					return false;
				}
			});
			return true;
		}
		return false;
	}
	
	public abstract void run();
}
