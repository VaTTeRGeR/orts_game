package de.vatterger.game.packets;

public class PublicKeyPacket {
	public byte[] publicKeyBytes;
	
	public PublicKeyPacket() {}
	
	public PublicKeyPacket(byte[] publicKeyBytes) {
		this.publicKeyBytes = publicKeyBytes;
	}
}
