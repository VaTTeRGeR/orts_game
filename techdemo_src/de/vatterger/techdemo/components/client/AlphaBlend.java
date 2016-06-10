package de.vatterger.techdemo.components.client;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;

public class AlphaBlend extends Component {
	public BlendingAttribute blendAttr;

	public AlphaBlend(BlendingAttribute blendAttr) {
		this.blendAttr = blendAttr;
	}
}
