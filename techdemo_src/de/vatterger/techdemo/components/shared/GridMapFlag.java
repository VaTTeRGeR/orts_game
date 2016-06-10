package de.vatterger.techdemo.components.shared;


import com.artemis.Component;

import de.vatterger.techdemo.handler.gridmap.GridMapBitFlag;

public class GridMapFlag extends Component {

	public GridMapBitFlag flag;

	public GridMapFlag() {
		flag = new GridMapBitFlag();
	}
	
	public GridMapFlag(GridMapBitFlag gf) {
		flag = gf;
	}
}
