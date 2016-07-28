package de.vatterger.engine.util;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;

import de.vatterger.game.components.gameobject.Rotation;

public class NodeRotationUtil {
	public static  void setRotationByName(ModelInstance instance, Rotation rot){
		if(rot.v2 == null) {
			instance.nodes.first().rotation.set(rot.v1[0]);
		} else {
			Node node;
			for (int i = 0; i < rot.v2.length; i++) {
				node = instance.getNode(rot.v2[i]);
				node.rotation.set(rot.v1[i]);
			}
		}
	}
}
