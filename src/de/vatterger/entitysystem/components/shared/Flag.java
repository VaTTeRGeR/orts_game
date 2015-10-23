package de.vatterger.entitysystem.components.shared;


import com.artemis.Component;

import de.vatterger.entitysystem.gridmapservice.BitFlag;

public class Flag extends Component {

	public BitFlag flag;

	public Flag() {
		flag = new BitFlag();
	}
	
	public Flag(BitFlag gf) {
		flag = gf;
	}
}
