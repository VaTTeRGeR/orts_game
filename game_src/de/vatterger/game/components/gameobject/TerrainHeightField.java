package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class TerrainHeightField extends Component {
	public float height[][];
	public float grid_size;

	public TerrainHeightField() {
		height = new float[0][0];
		grid_size = 0f;
	}
	
	public TerrainHeightField(float height[][]) {
		this(height, 10f);
	}
	
	public TerrainHeightField(float height[][], float grid_size) {
		int a = height.length;
		int b = height[0].length;

		this.height = new float[a][b];

		for (int i = 0; i < a; i++) {
			System.arraycopy(height[i], 0, this.height[i], 0, b);
		}
		
		this.grid_size = grid_size;
	}
}
