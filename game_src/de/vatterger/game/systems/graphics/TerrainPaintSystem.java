package de.vatterger.game.systems.graphics;

import java.util.concurrent.TimeUnit;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.Culled;
import de.vatterger.game.components.gameobject.TerrainHeightField;

public class TerrainPaintSystem extends IteratingSystem {
	
	private ComponentMapper<AbsolutePosition>	apm;
	private ComponentMapper<TerrainHeightField>	thfm;
	
	@Wire(name="camera")
	private OrthographicCamera camera;
	
	private Profiler profiler = new Profiler("TerrainPaintSystem", TimeUnit.MICROSECONDS);

	boolean subtractKeyPressed = false;
	boolean addKeyPressed = false;
	boolean num5KeyJustPressed = false;
	
	float[] levels = new float[] {0f,0.35f,0.45f,0.60f,0.8f,1.0f};
	
	Vector3 v0 = new Vector3();

	Rectangle r0 = new Rectangle();
	Rectangle r1 = new Rectangle();
	
	@SuppressWarnings("unchecked")
	public TerrainPaintSystem() {
		super(Aspect.all(AbsolutePosition.class,TerrainHeightField.class).exclude(Culled.class));
	}
	
	@Override
	protected void initialize() {
		GraphicalProfilerSystem.registerProfiler("TerrainPaintSystem", Color.BROWN, profiler);
	}

	@Override
	protected void begin() {
		
		profiler.start();

		addKeyPressed = Gdx.input.isKeyPressed(Keys.NUMPAD_8);
		subtractKeyPressed = Gdx.input.isKeyPressed(Keys.NUMPAD_2);

		num5KeyJustPressed = Gdx.input.isKeyJustPressed(Keys.NUMPAD_5);
	}

	@Override
	protected void process(int entityId) {

		Vector3 ap = apm.get(entityId).position;
		
		TerrainHeightField thf = thfm.get(entityId);
		
		Math2D.castMouseRay(v0, camera);
		
		r0.set(ap.x, ap.y, thf.getWidth(), thf.getHeight());
		
		float multiplier = 2f;
		
		float gridSize = thf.grid_size;

		float maxRange = gridSize * multiplier;
		
		r1.set(v0.x - 0.5f * maxRange, v0.y - 0.5f * maxRange, maxRange, maxRange);
		
		if(r0.overlaps(r1)) {
			
			for (int i = 0; i < thf.height.length; i++) {
				for (int j = 0; j < thf.height[i].length; j++) {
					
					float x = ap.x + gridSize * j;
					float y = ap.y + gridSize * ( thf.height.length - 1 - i );
					
					float distNormalized = v0.dst(x, y, 0f) / maxRange * 2;
					
					distNormalized = MathUtils.clamp(distNormalized, 0f, 1f);
					
					float influence = 1f - distNormalized * distNormalized;
					
					if(r1.contains(x, y)) {
						
						thf.needsMeshRebuild = true;
						
						if(subtractKeyPressed) {
							thf.height[i][j] -= 0.5f * world.delta * influence;
						} else if(addKeyPressed) {
							thf.height[i][j] += 0.5f * world.delta * influence;
						} else if(num5KeyJustPressed) {
							thf.height[i][j] = levels[MathUtils.random(levels.length - 1)];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_1)) {
							thf.height[i][j] = levels[0];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_2)) {
							thf.height[i][j] = levels[1];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_3)) {
							thf.height[i][j] = levels[2];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_4)) {
							thf.height[i][j] = levels[3];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_5)) {
							thf.height[i][j] = levels[4];
						} else if(Gdx.input.isKeyPressed(Keys.NUM_6)) {
							thf.height[i][j] = levels[5];
						} else {
							thf.needsMeshRebuild = false;
						}
						
						thf.height[i][j] = MathUtils.clamp(thf.height[i][j], 0f, 1f);
					}
				}
			}
		}
	}
	
	@Override
	protected void end() {
		profiler.stop();
	}
	
	@Override
	protected void dispose() {}
}
