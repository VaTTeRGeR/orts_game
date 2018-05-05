package de.vatterger.game.ui;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public abstract class ClickListener extends InputListener {
	
	protected	Actor	listenerActor;
	private		int		button;
	
	public ClickListener() {
		this(Buttons.LEFT);
	}
	
	public ClickListener(int button) {
		button = Math.min(4, button);
		button = Math.max(0, button);
		
		this.button = button;
		
		listenerActor = null;
	}
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		return true;
	}
	
	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		listenerActor = event.getListenerActor();
		if(button == this.button && listenerActor != null) {
			run();
		}
	}
	
	public abstract void run();
}
