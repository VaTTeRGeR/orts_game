package de.vatterger.entitysystem.factory.shared;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.utils.Bag;

import de.vatterger.entitysystem.components.shared.GridMapFlag;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

public class EntityModifyFactory {
	
	private EntityModifyFactory() {}
	
	public static void deactivateEntity(Entity e) {
		GridMapFlag gridMapFlag = e.getComponent(GridMapFlag.class);
		if(gridMapFlag != null) {
			gridMapFlag.flag.removeFlag(GridMapBitFlag.ACTIVE);
		}
		e.edit().add(new Inactive());
	}
	
	@SafeVarargs
	public static void stripComponentsExcept(Entity e, Class<? extends Component> ...exceptClazz) {
		if(exceptClazz != null && e != null) {
			EntityEdit ed = e.edit();
			Bag<Component> components = new Bag<Component>(8);
			for (int i = 0; i < components.size(); i++) {
				Component c = components.get(i);
				if (exceptClazz != null) {
					boolean remove = true;
					for (int j = 0; j < exceptClazz.length; j++) {
						if (exceptClazz[j].isInstance(c)) {
							remove = false;
						}
					}
					if (remove) {
						ed.remove(c);
					}
				} else {
					ed.remove(c);
				}
			}
		}
	}
	
	public static void stripComponents(Entity e) {
		EntityEdit ed = e.edit();
		Bag<Component> components = new Bag<Component>(8);
		e.getComponents(components);
		for (int i = 0; i < components.size(); i++) {
			ed.remove(components.get(i));
		}
	}
}
