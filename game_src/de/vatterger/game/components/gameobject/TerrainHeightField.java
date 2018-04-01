package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class TerrainHeightField extends Component {
	public float height[][];
	public float grid_size;
	public float scale;

	public TerrainHeightField() {
		height = new float[0][0];
		grid_size = 0f;
		scale = 1f;
	}
	
	public TerrainHeightField(float height[][]) {
		this(height, 10f, 1f);
	}
	
	public TerrainHeightField(float height[][], float grid_size, float scale) {
		int a = height.length;
		int b = height[0].length;

		this.height = new float[a][b];

		for (int i = 0; i < a; i++) {
			System.arraycopy(height[i], 0, this.height[i], 0, b);
		}
		
		this.grid_size = grid_size;
		this.scale = scale;
	}
	
	public float getWidth() {
		return grid_size*(height[0].length-1);
	}

	public float getHeight() {
		return grid_size*(height.length-1);
	}
}
