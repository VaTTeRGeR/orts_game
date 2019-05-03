package de.vatterger.game.systems.gameplay;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.pathfinding.PathFindingRequest;
import de.vatterger.engine.handler.pathfinding.PathFindingWorker;
import de.vatterger.engine.network.io.RingBuffer;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Profiler;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.MovementParameters;
import de.vatterger.game.components.gameobject.Turrets;
import de.vatterger.game.systems.graphics.GraphicalProfilerSystem;

public class PathFindingSystem extends IteratingSystem implements InputProcessor {

	@Wire(name="camera")
	private Camera camera;
	
	@Wire(name="input")
	private InputMultiplexer inputMultiplexer;
	
	private boolean touched = false;
	
	private Profiler profiler = new Profiler("AsgnRandomPaths");
	
	private ComponentMapper<AbsolutePosition>		apm;

	private Vector3 mousePositionWorld = new Vector3();
	
	private PathFindingWorker pathFinder = new PathFindingWorker();
	
	private static RingBuffer<PathFindingRequest> results = new RingBuffer<>(2048);
	
	public PathFindingSystem() {
		
		super(Aspect.all(AbsolutePosition.class, Turrets.class));
		
		GraphicalProfilerSystem.registerProfiler("AssignRandomPathsSystem", Color.BLUE, profiler);
	}
	
	@Override
	protected void initialize() {
		inputMultiplexer.addProcessor(this);
	}
	
	@Override
	protected void begin() {
		
		profiler.start();
		
		if(Gdx.input.isButtonPressed(Buttons.LEFT) && touched) {
			touched = false;
			mousePositionWorld = Math2D.castMouseRay(new Vector3(), camera);
		} else {
			mousePositionWorld = null;
		}
	}

	@Override
	protected void process(int e) {
		
		if(mousePositionWorld != null) {
			
			Vector3 position = apm.get(e).position;
			
			world.edit(e).remove(MoveCurve.class);
			
			PathFindingRequest req = new PathFindingRequest(e, position, mousePositionWorld.add(MathUtils.random(-0f, 0f), MathUtils.random(-0f, 0f), 0f)).withTimeout(150).withReturnQueue(results);
			
			pathFinder.put(req);
		}
	}
	
	private void createPath(PathFindingRequest req) {
		
		if(!req.isFinished()) {
			return;
		}
		
		ArrayList<Vector3> path = req.getPath();
		
		if(path.size() >= 1) {
			
			MoveCurve moveCurve = new MoveCurve(path.toArray(new Vector3[path.size()]), new MovementParameters());
			
			if(world.getEntityManager().isActive(req.getEntityId())) {
				world.edit(req.getEntityId()).add(moveCurve);
			}
		}
	}
	
	@Override
	protected void end() {
		
		while(results.has()) {
			createPath(results.get());
		}
		
		profiler.stop();
	}
	
	protected void dispose() {
		pathFinder.stop();
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		touched = true;
		return true;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		touched = false;
		return true;
	}
}
