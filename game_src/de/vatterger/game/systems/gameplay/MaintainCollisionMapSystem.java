package de.vatterger.game.systems.gameplay;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.Circle;

import de.vatterger.game.components.gameobject.AbsolutePosition;
import de.vatterger.game.components.gameobject.CollisionRadius;

public class MaintainCollisionMapSystem extends IteratingSystem {

	private ComponentMapper<AbsolutePosition> apm;
	private ComponentMapper<CollisionRadius> crm;
	
	static Circle c0 = new Circle();
	
	static int		size;
	static float[]	data;
	
	
	public MaintainCollisionMapSystem() {
		super(Aspect.all(AbsolutePosition.class, CollisionRadius.class));
	}
	
	@Override
	protected void begin() {
		size = 0;
		data = new float[getEntityIds().size()*3];
	}

	@Override
	protected void process(int entityId) {
		AbsolutePosition ap = apm.get(entityId);
		CollisionRadius cr = crm.get(entityId);
		
		data[size++] = ap.position.x + cr.offsetX;
		data[size++] = ap.position.y + cr.offsetY;
		data[size++] = cr.dst;
	}
	
	public static float[] getData() {
		return data;
	}
	
	public static Circle getCircle(int i) {
		i *= 3;
		if(i >= 0 && i < size) {
			c0.set(data[i++], data[i++], data[i]);
			return c0;
		} else {
			c0.set(0f, 0f, 0f);
			return c0;
		}
	}
	
	public static int getSize() {
		return size > 0 ? size/3 : 0;
	}
}
