package de.vatterger.entitysystem.components;

import com.artemis.Component;

public final class RemoteSlave extends Component {

	public int masterId = -1;
	
	public RemoteSlave() {
	}

	public RemoteSlave(int masterId) {
		this.masterId = masterId;
	}
}
