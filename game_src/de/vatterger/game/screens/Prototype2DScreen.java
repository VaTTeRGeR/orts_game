
package de.vatterger.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.vatterger.engine.handler.asset.AtlasHandler;

public class Prototype2DScreen implements Screen {

	Camera			camera;
	Viewport		viewport;
	SpriteBatch		spriteBatch;
	
	final float ymod = (float)Math.sqrt(0.5f);
	final float sssp = 1000;
	final float sssm = 200;
	final float ppm = sssp/sssm;
	final float mpp = 1f/ppm;
	float ratio = Gdx.graphics.getWidth()/Gdx.graphics.getHeight();

	int wv = Gdx.graphics.getWidth();
	int hv = Gdx.graphics.getHeight();

	float zoom = 1f;
	float ww = wv*mpp*zoom;
	float hw = hv*mpp*zoom;
	
	float angle = 0f;
	Vector3 offset = new Vector3(0f, 0f, 0f);
	Vector3 temp = new Vector3();
	
	public Prototype2DScreen() {
		setupCamera();
		
		AtlasHandler.initialize(sssm);
		AtlasHandler.registerTank("tank", 1);
		AtlasHandler.registerMisc("tile");
		
		spriteBatch = new SpriteBatch(4096);
	}

	private void setupCamera() {
		camera = new OrthographicCamera();
		viewport = new ScalingViewport(Scaling.fit, ww , hw, camera);
	}
	
	@Override
	public void render(float delta) {
		if(Gdx.input.isKeyPressed(Keys.W)) {
			camera.position.y += 100*delta;
		}
		
		if(Gdx.input.isKeyPressed(Keys.S)) {
			camera.position.y -= 100*delta;
		}
		
		if(Gdx.input.isKeyPressed(Keys.A)) {
			camera.position.x -= 100*delta;
		}
		
		if(Gdx.input.isKeyPressed(Keys.D)) {
			camera.position.x += 100*delta;
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.Q)) {
			zoom -= 1f;
			zoom = Math.max(1f, zoom);
			resize(wv, hv);
		}
		if(Gdx.input.isKeyJustPressed(Keys.E)) {
			zoom += 1f;
			zoom = Math.min(4f, zoom);
			resize(wv, hv);
		}
		
		Vector3 camPos = camera.position.cpy();
		camera.position.x = (int)camPos.x;
		camera.position.y = (int)camPos.y;
		camera.position.z = (int)camPos.z;
		camera.update();
		camera.position.set(camPos);
		
		Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()) + " - " + (int)((1f/Gdx.graphics.getDeltaTime()) + 0.5f));
		Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		angle += 90 * delta;
		if(angle >= 360f)
			angle -= 360f;
		
		int frame = (int)(angle*8f/360f);
		
		Sprite sprite0 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("tile"));
		Sprite sprite1 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("tank_h"), frame);
		Sprite sprite2 = AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("tank_t0"), (frame+1)%8);
		
		temp.set(offset).rotate(Vector3.Z, -frame*360f/8f);
		
		sprite1.setPosition(-sprite1.getWidth()/2f,-sprite1.getHeight()/2f);
		sprite2.setPosition(-sprite2.getWidth()/2f + temp.x,-sprite2.getHeight()/2f + temp.y*ymod);

		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.enableBlending();
		spriteBatch.begin();
		
		for (int i = 0; i <= 1000; i+=10) {
			for (int j = 0; j <= 1000; j+=10) {
				sprite0.setPosition(i-sprite2.getWidth()/2f,j*ymod-sprite2.getHeight()/2f);
				sprite0.draw(spriteBatch);
			}
		}
		
		sprite1.draw(spriteBatch);
		sprite2.draw(spriteBatch);
		
		spriteBatch.end();
		
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}

	@Override
	public void resize(int width, int height) {
		wv = Gdx.graphics.getWidth();
		hv = Gdx.graphics.getHeight();
		ratio = wv/hv;
		ww = wv*mpp*zoom;
		hw = hv*mpp*zoom;
		
		viewport.setWorldSize(ww , hw);
		viewport.update(width, height, false);
	}

	@Override
	public void show() {
		System.out.println("SHOW");
	}

	@Override
	public void pause() {
		System.out.println("PAUSE");
	}

	@Override
	public void resume() {
		System.out.println("RESUME");
	}

	@Override
	public void hide() {
		System.out.println("HIDE");
	}

	@Override
	public void dispose() {
		AtlasHandler.dispose();
		spriteBatch.dispose();
	}
}
