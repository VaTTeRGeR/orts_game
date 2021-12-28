
package de.vatterger.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.VisUI;

import de.vatterger.engine.util.Metrics;
import de.vatterger.engine.util.Profiler;

public abstract class StageScreen implements Screen {

	private Profiler					profiler;

	protected InputMultiplexer		inputMultiplexer;
	
	protected Stage					stage;
	protected Skin						skin;

	public StageScreen() {
		
		profiler = new Profiler("loop");
		
		inputMultiplexer = new InputMultiplexer();
		
		setupStage();
	}

	protected Table tableMain;
	
	private void setupStage() {
		
		if(!VisUI.isLoaded()) {
			VisUI.load();
		}
		
		skin = VisUI.getSkin();
		
		//skin = new Skin(Gdx.files.internal("assets/visui/assets/uiskin.json"));
		
		stage = new Stage();
		
		//stage.setDebugAll(true);
		
		tableMain = new Table(skin);
		tableMain.center();
		tableMain.setFillParent(true);
		stage.addActor(tableMain);

		fillStage(stage, skin);
		
		inputMultiplexer.addProcessor(stage);
	}
	
	protected abstract void fillStage(Stage stage, Skin skin);

	@Override
	public void render(float delta) {
		
		profiler.start();
		
		stage.act(delta);
		stage.draw();
		
		if(Gdx.input.isKeyJustPressed(Keys.F1) && Gdx.graphics.supportsDisplayModeChange()) {
			if(Gdx.graphics.isFullscreen())
				Gdx.graphics.setWindowedMode(640, 480);
			else
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		}
		
		profiler.stop();
}

	@Override
	public void resize(int width, int height) {
		int wv = Metrics.wv = width;
		int hv = Metrics.hv = height;
		
		stage.getViewport().setWorldSize(wv, hv);
		stage.getViewport().update(wv, hv, true);
		
		tableMain.layout();
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(inputMultiplexer);
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

}
