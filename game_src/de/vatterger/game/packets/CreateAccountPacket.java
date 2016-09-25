package de.vatterger.game.packets;

public class CreateAccountPacket {
	public byte[] nameEncrypted;
	public byte[] passwordEncrypted;
	
	public CreateAccountPacket() {}
	
	public CreateAccountPacket(byte[] nameEncrypted, byte[] passwordEncrypted) {
		this.nameEncrypted = nameEncrypted;
		this.passwordEncrypted = passwordEncrypted;
	}
}
