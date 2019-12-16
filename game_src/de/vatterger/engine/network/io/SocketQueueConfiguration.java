package de.vatterger.engine.network.io;

public class SocketQueueConfiguration {

	/**The timeout in milliseconds for connecting to a remote socket.*/
	public int CONNECT_TIMEOUT = 5000;

	/**Number of {@link SocketQueuePacket} objects available from the fixed size pre-populated pool inside {@link SocketQueue}.
	 * Set this to approximately how many packets you need to have in flight in both send and receive direction.*/
	public int PACKET_POOL_SIZE = 32;
	
	/**The size of the internal TCP receive buffer that stores your incoming data.
	 * Increase this if you need to have more data in flight.*/
	public int TCP_RX_BUFFER_SIZE = 64*1024;

	/**The size of the internal TCP send buffer that stores your outgoing data.
	 * Increase this if you need to have more data in flight.*/
	public int TCP_TX_BUFFER_SIZE = 64*1024;

	/**The maximum amount of data per {@link SocketQueuePacket}. Values below 8192 Bytes are recommended
	 * for best performance since buffer sizes greater than 8192 byte will not fit into the local stack
	 * for socketWrite0() and thus create additional overhead. If you want to have the entire payload fit
	 * into a single IP packet choose a size of 1400 bytes or less.*/
	public int PACKET_BUFFER_SIZE = 4096;

	/**The packet-write-thread inside {@link SocketQueue} sleeps when no packets are being sent, the sleep
	 * interval (milliseconds) increases linearly beginning with this value after a packet has just been sent.*/
	public int WRITE_THREAD_SLEEP_MIN = 1;

	/**The packet-write-thread inside {@link SocketQueue} sleeps when no packets are being sent, the sleep
	 * interval (milliseconds) increases linearly ending with this value.*/
	public int WRITE_THREAD_SLEEP_MAX = 5;
	
	/**The packet-write-thread inside {@link SocketQueue} sleeps when no packets are being sent, the sleep
	 * interval (milliseconds) increases linearly by this amount.*/
	public int WRITE_THREAD_SLEEP_GAIN = 1;
}
