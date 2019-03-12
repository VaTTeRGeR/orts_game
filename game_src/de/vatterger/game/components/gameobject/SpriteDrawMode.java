package de.vatterger.game.components.gameobject;

import org.lwjgl.opengl.GL11;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;

public class SpriteDrawMode extends Component {

	public Color color =  new Color(Color.WHITE);
	
	public int blend_src = GL11.GL_SRC_ALPHA;
	public int blend_dst = GL11.GL_ONE_MINUS_SRC_ALPHA;

	
	/**
	 * Initializes a new SpriteDrawMode component.
	 * Setup this object with the chainable setter functions.
	 */
	public SpriteDrawMode() {}
	
	/** Sets the alpha component of the color stored in this SpriteDrawMode object
	 * @param alpha The alpha value for the internal Color object
	 * @return this object for chaining
	 */
	public SpriteDrawMode alpha(float alpha) {

		this.color.a = alpha;
		
		return this;
	}
	
	/** Sets the blend functions
	 * @param blend_src The OpenGL source blend function
	 * @param blend_dst The OpenGL destination blend function
	 * @return this object for chaining
	 */
	public SpriteDrawMode blend(int blend_src, int blend_dst) {

		this.blend_src = blend_src;
		this.blend_dst = blend_dst;
		
		return this;
	}
	
	/** Sets the internal Color objects values to the values in the "color" parameter.
	 * Important: THIS WILL OVERWRITE THE PREVIOUSLY SET ALPHA VALUE!
	 * @param color The Color object which values will be copied
	 * @return this object for chaining
	 */
	public SpriteDrawMode color(Color color) {

		this.color.set(color);
		
		return this;
	}
}
