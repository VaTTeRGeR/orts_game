package de.vatterger.entitysystem.processors;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.entitysystem.components.Flag;
import de.vatterger.entitysystem.components.Position;
import de.vatterger.entitysystem.components.SlimeCollision;
import de.vatterger.entitysystem.gridmapservice.GridFlag;
import de.vatterger.entitysystem.gridmapservice.GridMapService;
import de.vatterger.entitysystem.tools.Profiler;

public class GridMapProcessor extends EntityProcessingSystem {

	private ComponentMapper<Flag> fm;
	private ComponentMapper<SlimeCollision> scm;
	private ComponentMapper<Position> pm;
	private Circle flyWeightCircle = new Circle();
	
	Profiler p;

	@SuppressWarnings("unchecked")
	public GridMapProcessor() {
		super(Aspect.getAspectForAll(Flag.class, Position.class));
	}

	@Override
	protected void initialize() {
		fm = world.getMapper(Flag.class);
		scm = world.getMapper(SlimeCollision.class);
		pm = world.getMapper(Position.class);
	}
	
	@Override
	protected void begin() {
		p = new Profiler("GridMap Clear");
		GridMapService.clear();
		p.log();
		p = new Profiler("GridMap Insert");	}

	@Override
	protected void process(Entity e) {
		GridFlag flag = fm.get(e).flag;
		if(flag.hasAllFlagsOf(GridFlag.COLLISION)) {
			flyWeightCircle.set(pm.get(e).pos,scm.get(e).radius);
			GridMapService.insert(flyWeightCircle, world.getEntity(e.id), flag);
		} else {
			GridMapService.insert(pm.get(e).pos, world.getEntity(e.id), flag);
		}
	}
	
	@Override
	protected void end() {
		p.log();
	}
	
	@Override
	protected void dispose() {
		GridMapService.clear();
	}
}
