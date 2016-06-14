package de.vatterger.game.components.client;

import com.artemis.Component;

public class PrivateKey extends Component {
	public byte[] v;

	public PrivateKey(byte[] privateKey) {
		this.v = privateKey;
	}
}
