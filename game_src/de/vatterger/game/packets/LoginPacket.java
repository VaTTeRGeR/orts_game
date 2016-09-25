package de.vatterger.game.packets;

public class LoginPacket {
	public byte[] nameEncrypted;
	public byte[] passwordEncrypted;
	
	public LoginPacket() {}
	
	public LoginPacket(byte[] nameEncrypted, byte[] passwordEncrypted) {
		this.nameEncrypted = nameEncrypted;
		this.passwordEncrypted = passwordEncrypted;
	}
}
