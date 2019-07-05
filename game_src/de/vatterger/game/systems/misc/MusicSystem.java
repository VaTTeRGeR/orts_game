package de.vatterger.game.systems.misc;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class MusicSystem extends BaseSystem {

	private Music sons = null;
	private Music hold = null;
	
	public MusicSystem() {
		
		sons = Gdx.audio.newMusic(new FileHandle("assets/sound/sons.mp3"));
		hold = Gdx.audio.newMusic(new FileHandle("assets/sound/hold.mp3"));
		
		sons.setLooping(true);
		hold.setLooping(true);
		
		sons.setVolume(1f);
		hold.setVolume(1f/32f);
	}
	
	@Override
	protected void processSystem() {

		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			
			if(sons.isPlaying()) {
				
				sons.pause();
				hold.pause();
				
			} else {
				
				sons.play();
				hold.play();
			}
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.UP) && sons.getVolume() < 1f) {
			
			sons.setVolume(Math.min(1f, sons.getVolume() * 2f));
			hold.setVolume(Math.max(0f, hold.getVolume() / 2f));
			
			System.out.println("Volume-sons: " + sons.getVolume());
			System.out.println("Volume-hold: " + hold.getVolume());
		}

		if(Gdx.input.isKeyJustPressed(Keys.DOWN) && hold.getVolume() < 1f) {

			sons.setVolume(Math.max(0f, sons.getVolume() / 2f));
			hold.setVolume(Math.min(1f, hold.getVolume() * 2f));
			
			System.out.println("Volume-sons: " + sons.getVolume());
			System.out.println("Volume-hold: " + hold.getVolume());
		}
	}
	
	@Override
	protected void dispose() {
		
	}
}
