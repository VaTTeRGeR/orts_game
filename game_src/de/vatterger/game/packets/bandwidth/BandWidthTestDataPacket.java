package de.vatterger.game.packets.bandwidth;

public class BandWidthTestDataPacket {
	int id;
	static final byte[] data = new byte[1000-16]; //1000byte - ip-header - udp-header
	
	static {
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte)(Byte.MAX_VALUE * Math.random());
		}
	}
	
	public BandWidthTestDataPacket() {
		id = 0;
	}
	
	public BandWidthTestDataPacket(int id) {
		this.id = id;
	}
}
