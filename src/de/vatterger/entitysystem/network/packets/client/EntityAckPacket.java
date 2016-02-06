package de.vatterger.entitysystem.network.packets.client;

public class EntityAckPacket {
	public int[] received;

	public EntityAckPacket(int[] received) {
		this.received = received;
	}
}