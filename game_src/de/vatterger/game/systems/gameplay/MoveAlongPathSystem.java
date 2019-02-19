package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.AbsoluteRotation;
import de.vatterger.game.components.gameobject.MoveCurve;

public class MoveAlongPathSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition>		apm;
	private ComponentMapper<AbsoluteRotation>		arm;
	private ComponentMapper<MoveCurve>				mcm;

	private Vector3 v0 = new Vector3();
	
	private long	time;
	
	public MoveAlongPathSystem() {
		super(Aspect.all(AbsolutePosition.class, AbsoluteRotation.class, MoveCurve.class));
	}
	
	@Override
	protected void begin() {
		time = TimeSystem.getCurrentTimeMillis();
	}

	@Override
	protected void process(int e) {
		
		Vector3		position = apm.get(e).position;
		MoveCurve	mcc = mcm.get(e);
		
		while(mcc.pathTimes.size > 1 && mcc.pathTimes.get(1) <= time) {
			mcc.pathPoints.removeIndex(0);
			mcc.pathTimes.removeIndex(0);
		}
		
		int size = mcc.pathPoints.size;
		
		if(size > 1) {
			long sourceTime = mcc.pathTimes.get(0);
			long targetTime = mcc.pathTimes.get(1);
			
			float alpha = (time - sourceTime)/(float)(targetTime-sourceTime);
			
			v0.set(mcc.pathPoints.get(0)).sub(mcc.pathPoints.get(1)).nor();
			arm.get(e).rotation = Math2D.atan2d(v0.y, v0.x);
		
			position.set(mcc.pathPoints.get(0)).interpolate(mcc.pathPoints.get(1), alpha, Interpolation.linear);
		} else if(size == 1) {
			position.set(mcc.pathPoints.first());
			world.edit(e).remove(MoveCurve.class);
		}
	}
}
