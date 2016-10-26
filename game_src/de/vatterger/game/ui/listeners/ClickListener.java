package de.vatterger.game.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class ClickListener implements EventListener {
	
	Actor actor;
	
	public ClickListener(Actor actor) {
		this.actor = actor;
	}
	
	@Override
	public boolean handle(Event event) {
		if(Gdx.input.justTouched()) {
			run();
			return true;
		}
		return false;
	}
	
	public abstract void run();
}
