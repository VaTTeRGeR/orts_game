package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.pathfinding.PathFindingRequest;
import de.vatterger.engine.handler.pathfinding.PathFindingWorker;
import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.Turrets;

public class AssignRandomPathsSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>		apm;

	private Vector3 mousePositionWorld = new Vector3();
	
	private PathFindingWorker pathFinder = new PathFindingWorker();
	
	private Camera camera = null;
	
	public AssignRandomPathsSystem(Camera camera) {
		super(Aspect.all(AbsolutePosition.class, Turrets.class));
		this.camera = camera;
	}
	
	@Override
	protected void begin() {
		if(Gdx.input.isButtonPressed(Buttons.LEFT) && Gdx.input.justTouched()) {
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
			
			pathFinder.offer(new PathFindingRequest(e, position, mousePositionWorld.add(MathUtils.random(-0f, 0f), MathUtils.random(-0f, 0f), 0f)).withTimeout(50).withFinishCallback(path -> {
				if(path.size() > 1) {
					MoveCurve moveCurve = new MoveCurve(path.toArray(new Vector3[path.size()]), MathUtils.random(20, 30));
					if(world.getEntityManager().isActive(e))
						world.edit(e).add(moveCurve);
				}
			}));
		}
	}
	
	protected void dispose() {
		pathFinder.stop();
	}
}
