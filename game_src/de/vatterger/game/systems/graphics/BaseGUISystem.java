package de.vatterger.game.systems.graphics;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;

import de.vatterger.engine.handler.asset.AtlasHandler;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.screen.manager.ScreenManager;
import de.vatterger.game.ui.ClickListener;

public class BaseGUISystem extends BaseSystem {
	
	@Wire(name = "stage")
	private Stage stage;

	@Wire(name = "skin")
	private Skin skin;
	
	private Table tableMain;
	private static Button buttonExitGame;
	private static Button buttonTestGame;

	@Override
	protected void initialize() {
		
		if(stage == null) return;
		
		tableMain = new Table(skin);
		tableMain.setFillParent(true);
		tableMain.top();
		
		stage.addActor(tableMain);

		Table tableSub0 = new Table(skin);
		tableMain.add(tableSub0).expandX().fillX().right();

		buttonTestGame = new TextButton("TEST", skin);
		buttonTestGame.setDisabled(true);

		buttonExitGame = new TextButton("EXIT", skin);
		buttonExitGame.addListener(new ClickListener() {
			@Override
			public void run() {
				ScreenManager.setScreen(ScreenManager.MAIN);
			}
		});

		tableSub0.add(buttonTestGame).padTop(4).padLeft(4).space(4).expandX().fillX();
		tableSub0.add(buttonExitGame).padTop(4).padRight(4).space(4).width(50);
		
		Window window = new Window("CHKBXW", skin);
		window.setResizable(false);
		window.setKeepWithinStage(true);
		
		window.addAction(new Action() {
			float x = Float.MIN_VALUE;
			float y = x;
			
			@Override
			public boolean act(float delta) {
				if(x == Float.MIN_VALUE) {
					x = 50f;
					y = 50f;
				}
				
				//actor.moveBy(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f));
				
				float dx = actor.getX() - x;
				float dy = actor.getY() - y;
				
				if(!Gdx.input.isTouched()) {
					actor.moveBy(-dx*10f*Gdx.graphics.getDeltaTime(), -dy*10f*Gdx.graphics.getDeltaTime());					
				}
				
				dx = Math.abs(dx)/50;
				dy = Math.abs(dy)/50;
				
				float dMax = MathUtils.clamp(Math.max(dx, dy), 0f, 1f);
				
				actor.setColor(1f, 1f, 1f, 1f - dMax);
				
				if(dMax > 0.99f) {
					actor.setVisible(false);
				}

				if(Gdx.input.isKeyJustPressed(Keys.L)) {
					window.setVisible(!window.isVisible());
				}
				
				return false;
			}
		});
		
		CheckBox checkBox = null;
		
		Action checkBoxAction1 = new Action() {
			
			@Override
			public boolean act(float delta) {
				CheckBox checkBox = null;
				if(actor != null) {
					checkBox = ((CheckBox)actor);
					if(checkBox.isChecked()) {
						checkBox.remove();
						return true;
					}
				}
				return false;
			}
		};
		Action checkBoxAction2 = new Action() {
			
			@Override
			public boolean act(float delta) {
				CheckBox checkBox = null;
				if(actor != null) {
					checkBox = ((CheckBox)actor);
					if(checkBox.isChecked()) {
						checkBox.remove();
						return true;
					}
				}
				return false;
			}
		};
		Action checkBoxAction3 = new Action() {
			
			@Override
			public boolean act(float delta) {
				CheckBox checkBox = null;
				if(actor != null) {
					checkBox = ((CheckBox)actor);
					if(checkBox.isChecked()) {
						checkBox.remove();
						return true;
					}
				}
				return false;
			}
		};
		
		checkBox = new CheckBox("CH1", skin);
		checkBox.addAction(checkBoxAction1);
		window.add(checkBox).space(5).align(Align.top).row();
		
		checkBox = new CheckBox("CH2", skin);
		checkBox.addAction(checkBoxAction2);
		window.add(checkBox).space(5).align(Align.top).row();
		
		Image tankIcon = new Image(AtlasHandler.getSharedSpriteFromId(AtlasHandler.getIdFromName("pz1b_h"), Math2D.angleToIndex(135f, 16)));
		window.add(tankIcon).space(5).align(Align.top).row();
		
		checkBox = new CheckBox("CH2", skin);
		checkBox.addAction(checkBoxAction3);
		window.add(checkBox).space(5).align(Align.bottom).expand().row();
		
		tableMain.row();
		tableMain.add(window).size(100, 200).expand().pad(50).align(Align.bottomLeft).row();
		
		tableMain.validate();
	}
	
	@Override
	protected void processSystem() {
		
	}
	
	@Override
	protected void dispose() {
		
	}
}
