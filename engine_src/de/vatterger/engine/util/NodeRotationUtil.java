package de.vatterger.engine.util;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;

import de.vatterger.game.components.gameobject.Rotation;

public class NodeRotationUtil {
	public static  void setRotationByName(ModelInstance instance, Rotation rot) {
		if(rot.v2 == null) {
			Node node = instance.nodes.first();
			node.rotation.set(rot.v1[0]);
			while (node.hasChildren()) {
				node = node.getChild(0);
				node.rotation.idt();
				node.localTransform.idt();
			}
		} else {
			Node node;
			for (int i = 0; i < rot.v2.length; i++) {
				node = instance.getNode(rot.v2[i]);
				if(node != null)
					node.rotation.set(rot.v1[i]);
			}
		}
	}
}
