package de.vatterger.entitysystem.components.client;

import com.artemis.Component;

public final class RemoteSlave extends Component {

	public int masterId = -1;
	public float lastUpdateDelay = 0f;
	
	public RemoteSlave() {
	}

	public RemoteSlave(int masterId) {
		this.masterId = masterId;
	}
}
