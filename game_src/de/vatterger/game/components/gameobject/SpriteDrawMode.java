package de.vatterger.game.components.gameobject;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import org.lwjgl.opengl.GL11;

public class SpriteDrawMode extends Component {

	public Color color = null;
	
	// We use pre-multiplied alhpa!
	// https://www.shawnhargreaves.com/blog/premultiplied-alpha.html
	public int blend_src = GL11.GL_ONE;
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

		if(color == null) {
			color = Color.WHITE.cpy();
		}
		
		color.set(alpha, alpha, alpha, alpha);
		
		return this;
	}
	
	/** Sets the blend function to additive by calling blend(GL11.GL_SRC_COLOR, GL11.GL_ONE)
	 * @return this object for chaining
	 */
	public SpriteDrawMode additiveBlend() {
		return blend(GL11.GL_SRC_COLOR, GL11.GL_ONE);
	}
	
	/** Resets the blend function to the default premultiplied mix mode by calling blend(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR)
	 * @return this object for chaining
	 */
	public SpriteDrawMode normalBlend() {
		return blend(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
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

		if(this.color == null) {
			this.color = color.cpy();
		} else {
			this.color.set(color);
		}
		
		return this;
	}
}
