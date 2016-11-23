package de.vatterger.game.ui.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class ClickListener implements EventListener {
	
	protected final Actor actor;
	private int button;
	
	public ClickListener(Actor actor) {
		this(actor, Buttons.LEFT);
	}
	
	public ClickListener(Actor actor, int button) {
		this.actor = actor;
		
		button = Math.min(4, button);
		button = Math.max(0, button);
		
		this.button = button;
	}
	
	@Override
	public boolean handle(Event event) {
		if(Gdx.input.justTouched() && Gdx.input.isButtonPressed(button)) {
			run();
			return true;
		}
		return false;
	}
	
	public abstract void run();
}
