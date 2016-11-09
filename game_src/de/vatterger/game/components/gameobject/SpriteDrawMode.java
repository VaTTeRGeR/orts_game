package de.vatterger.game.components.gameobject;

import org.lwjgl.opengl.GL11;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;

public class SpriteDrawMode extends Component {

	public Color color = new Color(Color.WHITE);
	public int blend_src = GL11.GL_SRC_ALPHA;
	public int blend_dst = GL11.GL_ONE_MINUS_SRC_ALPHA;

	public SpriteDrawMode() {}
	
	public SpriteDrawMode(float alpha) {
		color.a = alpha;
	}

	public SpriteDrawMode(Color tint) {
		color.set(tint);
	}

	public SpriteDrawMode(Color tint, float alpha) {
		color.set(tint);
		color.a = alpha;
	}

	public SpriteDrawMode(int blend_src, int blend_dst) {
		this.blend_src = blend_src;
		this.blend_dst = blend_dst;
	}
	
	public SpriteDrawMode(Color tint, int blend_src, int blend_dst) {
		color.set(tint);
		this.blend_src = blend_src;
		this.blend_dst = blend_dst;
	}
}
