package de.vatterger.entitysystem.components;

import com.artemis.Component;

public final class RemoteSlave extends Component {

	public int masterId = -1;
	public float lastUpdateDelay = 0;
	
	public RemoteSlave() {
	}

	public RemoteSlave(int masterId) {
		this.masterId = masterId;
	}
}
