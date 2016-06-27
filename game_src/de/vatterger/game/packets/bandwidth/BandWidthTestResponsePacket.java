package de.vatterger.game.packets.bandwidth;

public class BandWidthTestResponsePacket {
	
	private int packetsReceived;
	private float receiveIntervalAVG;
	
	public BandWidthTestResponsePacket() {
	}

	public float getReceiveInterval() {
		return receiveIntervalAVG;
	}

	public void setReceiveInterval(float receiveInterval) {
		this.receiveIntervalAVG = receiveInterval;
	}

	public int getPacketsReceived() {
		return packetsReceived;
	}

	public void setPacketsReceived(int packetsReceived) {
		this.packetsReceived = packetsReceived;
	}
}
