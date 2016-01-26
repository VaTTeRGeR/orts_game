package de.vatterger.entitysystem.processors.server;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import de.vatterger.entitysystem.components.server.ServerPosition;
import de.vatterger.entitysystem.components.shared.CircleCollision;
import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;
import de.vatterger.entitysystem.handler.gridmap.GridMapHandler;

@Wire
public class GridMapProcessor extends EntityProcessingSystem {

	private ComponentMapper<GridMapFlag> gfm;
	private ComponentMapper<CircleCollision> ccm;
	private ComponentMapper<ServerPosition> spm;
	
	private Circle flyWeightCircle = new Circle();
	private Vector2 flyweightVector2 = new Vector2();

	@SuppressWarnings("unchecked")
	public GridMapProcessor() {
		super(Aspect.getAspectForAll(ServerPosition.class, GridMapFlag.class));
	}
	
	@Override
	protected void begin() {
		GridMapHandler.clear();
	}

	@Override
	protected void process(Entity e) {
		GridMapBitFlag bfc = gfm.get(e).flag;
		ServerPosition spc = spm.get(e);
		if(bfc.isContaining(GridMapBitFlag.COLLISION)) {
			flyWeightCircle.set(spc.pos.x,spc.pos.y, ccm.get(e).radius);
			GridMapHandler.insert(flyWeightCircle, e.id, bfc);
		} else {
			GridMapHandler.insert(flyweightVector2.set(spc.pos.x, spc.pos.y), e.id, bfc);
		}
	}
}
