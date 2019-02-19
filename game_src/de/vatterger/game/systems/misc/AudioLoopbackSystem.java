package de.vatterger.game.systems.misc;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;

public class AudioLoopbackSystem extends BaseSystem {

	final AudioRecorder	recorder = Gdx.audio.newAudioRecorder(24000, true);
	final AudioDevice	device = Gdx.audio.newAudioDevice(24000, true);

	final Thread thread;
	
	public AudioLoopbackSystem() {
		thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				final short[] samples = new short[512];
				
				while(!Thread.interrupted()) {
					try {
						
						recorder.read(samples, 0, samples.length);
						
						device.setVolume(0.25f);
						device.writeSamples(samples, 0, samples.length);

					} catch (Exception e) {
						break;
					}
				}
			}
		});
		thread.start();
	}

	@Override
	protected void processSystem() {}
	
	@Override
	protected void dispose() {
		thread.interrupt();
		try {
			thread.join(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		recorder.dispose();
		device.dispose();
	}
}
