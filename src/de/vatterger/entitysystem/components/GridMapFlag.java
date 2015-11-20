package de.vatterger.entitysystem.components;


import com.artemis.Component;

import de.vatterger.entitysystem.handler.gridmap.GridMapBitFlag;

public class GridMapFlag extends Component {

	public GridMapBitFlag flag;

	public GridMapFlag() {
		flag = new GridMapBitFlag();
	}
	
	public GridMapFlag(GridMapBitFlag gf) {
		flag = gf;
	}
}
