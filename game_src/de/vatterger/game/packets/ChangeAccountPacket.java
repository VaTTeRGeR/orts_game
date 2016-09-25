package de.vatterger.game.packets;

public class ChangeAccountPacket {
	public String nameEncrypted;
	public String passwordEncrypted;
	
	public ChangeAccountPacket() {}
	
	public ChangeAccountPacket(String nameEncrypted, String passwordEncrypted) {
		this.nameEncrypted = nameEncrypted;
		this.passwordEncrypted = passwordEncrypted;
	}
}
