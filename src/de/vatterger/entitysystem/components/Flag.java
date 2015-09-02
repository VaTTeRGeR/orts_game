package de.vatterger.entitysystem.components;


import com.artemis.Component;

import de.vatterger.entitysystem.gridmapservice.GridFlag;

public class Flag extends Component {

	public GridFlag flag;

	public Flag() {
		flag = new GridFlag();
	}
	
	public Flag(GridFlag gf) {
		flag = gf;
	}
}
