package de.vatterger.game.components.client;

import com.artemis.Component;

public class PublicKey extends Component {
	public byte[] v;

	public PublicKey(byte[] publicKey) {
		this.v = publicKey;
	}
}
