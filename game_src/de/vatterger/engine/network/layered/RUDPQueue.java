package de.vatterger.engine.network.layered;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class RUDPQueue extends DatagramChannelQueue {
	
	//HEADER RELIABLE
	
	//PID		2 Byte
	//SEQ		2 Byte
	//
	//->		4 Byte
	
	
	//HEADER UNRELIABLE
	
	//PID		2 Byte
	//
	//->		2 Byte

	
	//HEADER KEEP_ALIVE
	
	//PID		2 Byte
	//ACK		2 Byte
	//BAK		8 Byte
	//
	//->		12 Byte
	
	private static final int	RESET_REQ = -1;	
	private static final int	RESET_ACK = -2;	
	
	private final float		SUC_RATE = 1.00f;
	
	private final short		PID_UNRELIABLE	= 14318;
	private final short		PID_RELIABLE	= 26600;
	private final short		PID_KEEP_ALIVE	= 7303;
	
	
	private Timer		timer;
	private TimerTask	timerTask;
	
	private HashMap<InetSocketAddress, Endpoint>	addressToEndpoint = new HashMap<InetSocketAddress,Endpoint>(256);
	
	private ArrayBlockingQueue<DatagramPacket>		queueReceive = new ArrayBlockingQueue<>(2048);
	
	public RUDPQueue(InetSocketAddress address_bind) {
		super(address_bind);
	}
	
	private void setupTimerTask() {
		timerTask = new TimerTask() {
			@Override
			public void run() {
				long currentMillis = System.currentTimeMillis();
				
				DatagramPacket packet = null;
				while(queueReceive.remainingCapacity() > 0 && (packet = readInternal()) != null) {
					queueReceive.offer(packet);
				}

				LinkedList<Endpoint> killList = new LinkedList<Endpoint>();

				for(Endpoint endpoint : addressToEndpoint.values()) {
					
					endpoint.update(currentMillis);
					
					if(currentMillis - endpoint.T_LAST_KA_RECV > 5000) {
						killList.add(endpoint);
					} else if(endpoint.IS_CONNECTED && (currentMillis - endpoint.T_LAST_KA_SEND >= 250 || endpoint.ACK - endpoint.ACK_AT_KEEP_ALIVE > 16)) {
						
						System.out.println("KEEP ALIVE after " + (currentMillis-endpoint.T_LAST_KA_SEND) + "ms");
						
						sendKeepAlive(endpoint);
						endpoint.T_LAST_KA_SEND		= currentMillis;
						endpoint.ACK_AT_KEEP_ALIVE	= endpoint.ACK;
					}
				}
				
				for (Endpoint endpoint : killList) {
					removeEndpoint(endpoint.ADDRESS);
					System.out.println("Removed Endpoint " + endpoint.ADDRESS);
				}
			}
		};
		
		timer = new Timer("RUDP Timer", false);
		timer.scheduleAtFixedRate(timerTask, 50, 5);
	}
	
	private void sendKeepAlive(Endpoint endpoint) {
		Output out = new Output(12);
		
		out.writeShort(PID_KEEP_ALIVE);
		out.writeShort(endpoint.ACK);
		out.writeLong(endpoint.BAK);
		
		out.close();
		
		if(MathUtils.randomBoolean(SUC_RATE))
			super.write(endpoint.ADDRESS, out.getBuffer());
		else
			System.out.println("Lost KEEP ALIVE packet");
	}
	
	private void sendReset(InetSocketAddress address, int ackID) {
		if(ackID >= 0) {
			throw new IllegalArgumentException("ackID " + ackID + " is illegal, only RESET_ACK or RESET_REQ allowed.");
		}
		
		Output out = new Output(12);
		
		out.writeShort(PID_KEEP_ALIVE);
		out.writeShort(ackID);
		out.writeLong(0);
		
		out.close();
		
		if(MathUtils.randomBoolean(SUC_RATE))
			super.write(address, out.getBuffer());
		else
			System.out.println("Lost RESET packet");
	}
	
	public boolean write(InetSocketAddress address, byte[] data, boolean reliable) {
		if(reliable) {
			
			Endpoint endpoint = getEndpoint(address);
			
			if(endpoint == null || !endpoint.IS_CONNECTED) {
				System.err.println("Connect to Endpoint " + address + " first before using reliable transfer!");
				return false;
			}
			
			if(endpoint.PACKET_LIST.size() >= 64) {
				System.err.println("Reliable Queue is full");
				return false;
			}
			
			Output out = new Output(data.length + 4);

			short SEQ = endpoint.nextSEQ();

			//System.out.println("Writing packet: " + SEQ);
			//System.out.println();
			
			out.writeShort(PID_RELIABLE);			//PID
			out.writeShort(SEQ);					//SEQ
			out.write(data);						//DATA
			
			out.close();
			
			endpoint.addReliablePacket(new ReliablePacket(new DatagramPacket(data, data.length, address), System.currentTimeMillis(), SEQ, out.getBuffer()));
			
			if(MathUtils.randomBoolean(SUC_RATE))
				write(address, out.getBuffer());
			else
				System.out.println("Lost PACKET");
			
		} else {
			Output out = new Output(data.length + 2);
			
			out.writeShort(PID_UNRELIABLE);			//PID
			out.write(data);						//DATA
			
			out.close();
			
			write(address, out.getBuffer());
		}
		
		return true;
	}
	
	@Override
	public DatagramPacket read() {
		return queueReceive.poll();
	}
	
	public DatagramPacket readInternal() {
		DatagramPacket packet = super.read();
		
		if(packet != null) {
			byte[] data = packet.getData();
			
			Endpoint endpoint = addAndGetEndpoint((InetSocketAddress)packet.getSocketAddress());
			
			Input input = new Input(data);
			
			short PID = input.readShort();
			
			if(PID == PID_UNRELIABLE) {
				packet.setData(Arrays.copyOfRange(data, 2, data.length));
			} else if(PID == PID_RELIABLE) {
				endpoint.addACK(input.readShort());

				input.close();
				
				packet.setData(Arrays.copyOfRange(data, 4, data.length));
			} else if(PID == PID_KEEP_ALIVE) {
				//System.out.println("PID_KEEP_ALIVE received from " + endpoint.ADDRESS);
				
				short	ack = input.readShort();
				long	bak = input.readLong();
				
				input.close();
				
				switch (ack) {
				case RESET_REQ:
					
					endpoint.reset();
					sendReset(endpoint.ADDRESS, RESET_ACK);
					endpoint.IS_CONNECTED = true;
					
					break;

				case RESET_ACK:

					endpoint.IS_CONNECTED = true;
					
					break;

				default:
					if(endpoint.IS_CONNECTED) {
						short[] bak_parsed = unpackACKs(ack, bak);
						endpoint.updateReceivedACKs(bak_parsed);
						endpoint.updateLastKeepAliveTime(System.currentTimeMillis());
					}
					break;
				}
				
								
				return this.read();
			}
		}
		return packet;
	}
	
	public boolean connect(InetSocketAddress address, long timeout) {
		long t_start = System.currentTimeMillis();
		
		Endpoint endpoint = addAndGetEndpoint(address);
		endpoint.reset();
		
		do {
			if(System.currentTimeMillis() - t_start > timeout) {
				return false;
			}
			
			sendReset(address, RESET_REQ);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!endpoint.IS_CONNECTED);
		
		return true;
	}
	
	@Override
	public boolean bind() {
		boolean status = super.bind();
		setupTimerTask();
		return status;
	}
	
	@Override
	public void unbind() {
		timer.cancel();
		super.unbind();
	}
	
	private synchronized boolean isRegistered(InetSocketAddress address) {
		return addressToEndpoint.get(address) != null;
	}
	
	private synchronized Endpoint addAndGetEndpoint(InetSocketAddress address) {
		if(!isRegistered(address)) {
			Endpoint endPoint = new Endpoint(address);
			
			addressToEndpoint.put(address, endPoint);
			
			System.out.println("New Endpoint: " + endPoint.ADDRESS);
			
			return endPoint;
		} else {
			return getEndpoint(address);
		}
	}
	
	private short[] unpackACKs(short ACK, long BAK) {
		//System.out.println("BAK as bits: " + Long.toBinaryString(BAK));
		short[] BAK_ARRAY = new short[65];
		BAK_ARRAY[0] = ACK;
		int BAK_ONES_SIZE = 1;
		for (int i = 0; i < 64; i++) {
			if((BAK & (1L<<i)) != 0) {
				BAK_ARRAY[BAK_ONES_SIZE++] = calculateSEQFromBAK(ACK, i + 1);
			}
		}
		return Arrays.copyOf(BAK_ARRAY, BAK_ONES_SIZE);
	}
	
	private short calculateSEQFromBAK(int ACK, int i) {
		if(ACK < i) {
			return (short)(Short.MAX_VALUE - (i - ACK - 1));
		} else {
			return (short)(ACK - i);
		}
	}
	
	private synchronized Endpoint getEndpoint(InetSocketAddress address) {
		return addressToEndpoint.get(address);
	}
	
	private synchronized void removeEndpoint(InetSocketAddress address) {
		if(isRegistered(address)) {
			addressToEndpoint.remove(address);
		}
	}
	
	private class Endpoint {
		private volatile short					SEQ;
		
		private volatile short					ACK;
		
		private volatile long					BAK;
		
		private HashMap<Short,ReliablePacket>	PACKET_LIST;
		
		private final InetSocketAddress			ADDRESS;
		
		private volatile long					T_LAST_KA_RECV;
		
		private volatile short					ACK_AT_KEEP_ALIVE;
		
		private volatile long					T_LAST_KA_SEND;
		
		private volatile boolean				IS_CONNECTED;

		public Endpoint(InetSocketAddress ADDRESS) {
			SEQ 				= 0;
			
			ACK 				= 0;
			BAK 				= 0;
			
			PACKET_LIST			= new HashMap<Short, ReliablePacket>();
			
			T_LAST_KA_RECV		= System.currentTimeMillis();
			T_LAST_KA_SEND		= T_LAST_KA_RECV;
			
			ACK_AT_KEEP_ALIVE	= 0;
			
			IS_CONNECTED		= false;
			
			this.ADDRESS	= ADDRESS;
			if(ADDRESS == null) {
				throw new IllegalArgumentException("ADDRESS may not be null.");
			}
		}
		
		private synchronized void reset() {
			SEQ 				= 0;
			
			ACK 				= 0;
			BAK 				= 0;
			
			PACKET_LIST.clear();
			PACKET_LIST			= new HashMap<Short, ReliablePacket>();
			
			T_LAST_KA_RECV		= System.currentTimeMillis();
			T_LAST_KA_SEND		= T_LAST_KA_RECV;

			ACK_AT_KEEP_ALIVE	= 0;
			
			IS_CONNECTED		= false;
		}
		
		public synchronized void update(long currentMillis) {
			LinkedList<ReliablePacket> resendList = new LinkedList<ReliablePacket>();
			for(ReliablePacket reliablePacket : PACKET_LIST.values()) {
				if(currentMillis - reliablePacket.TIME_SENT > 300) {
					//System.out.println("Timed out packet: " + reliablePacket.SEQ);
					//System.out.println();
					resendList.add(reliablePacket);
				}
			}
			
			for (ReliablePacket reliablePacket : resendList) {
				if(seqADiffB(SEQ, reliablePacket.SEQ) >= 64) {
					if(write((InetSocketAddress)reliablePacket.PACKET.getSocketAddress(), reliablePacket.PACKET.getData(), true)) {
						PACKET_LIST.remove(reliablePacket.SEQ);
						//System.out.println("Repackaged resend of packet: " + reliablePacket.SEQ +" as " + SEQ);
					} else {
						//System.out.println("Repackaged resend of packet: " + reliablePacket.SEQ +" stalled");
					}
				} else {
					reliablePacket.TIME_SENT = System.currentTimeMillis();
					write(ADDRESS, reliablePacket.DATA_WITH_HEADER);
					//System.out.println("Simple resend of packet: " + reliablePacket.SEQ);
				}
			}
		}

		public synchronized void updateLastKeepAliveTime(long currentTimeMillis) {
			T_LAST_KA_RECV = currentTimeMillis;
		}

		public synchronized void updateReceivedACKs(short[] bak_parsed) {
			for (int i = 0; i < bak_parsed.length; i++) {
				short bak_i = bak_parsed[i];
				ReliablePacket reliablePacket = PACKET_LIST.get(bak_i);
				if(reliablePacket != null) {
					PACKET_LIST.remove(bak_i);
					//System.out.println("Got ACK for packet: " + bak_i);
					//System.out.println();
				}
			}
		}

		//Recalculates the ACK and BAK field used by the KEEP_ALIVE packets
		private synchronized void addACK(short newACK) {
			//System.out.println("Received packet: " + newACK);
			if(seqAGreaterB(newACK, ACK)) {
				int d = seqADiffB(newACK,ACK);
				BAK = (BAK << d) | (1L << (d - 1));
				ACK = newACK;
			} else if(seqAGreaterB(ACK, newACK)) {
				int d = ACK - newACK - 1;
				BAK = BAK | (1L<<d);
			}
		}
		
		//Start keeping track of this packet
		private synchronized void addReliablePacket(ReliablePacket reliablePacket) {
			PACKET_LIST.put(reliablePacket.SEQ, reliablePacket);
		}

		private synchronized short nextSEQ() {
			if(SEQ == Short.MAX_VALUE) {
				return SEQ = 0;
			}
			return ++SEQ;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ADDRESS.equals(obj);
		}
		
		@Override
		public int hashCode() {
			return ADDRESS.hashCode();
		}
	}
	
	private class ReliablePacket {
		private DatagramPacket	PACKET;
		private long			TIME_SENT;
		private short			SEQ;
		private byte[]			DATA_WITH_HEADER;
		
		public ReliablePacket(DatagramPacket PACKET, long TIME_SENT, short SEQ, byte[] PACKET_WITH_HEADER) {
			this.PACKET				= PACKET;
			this.TIME_SENT			= TIME_SENT;
			this.SEQ				= SEQ;
			this.DATA_WITH_HEADER	= PACKET_WITH_HEADER;
		}
		
		@SuppressWarnings("unused")
		public int compare(ReliablePacket o1, ReliablePacket o2) {
			long d = o1.TIME_SENT-o2.TIME_SENT;
			if(d == 0)
				return 0;
			else if(d > 0)
				return 1;
			else
				return -1;
		}
	}
	
	private static boolean seqAGreaterB(int A, int B) {
		return	( (A > B) && (A-B < Short.MAX_VALUE/2) ) ||
				( (A < B) && (B-A > Short.MAX_VALUE/2) );
	}

	private static int seqADiffB(int A, int B) {
		int d = A - B;
		if(d > Short.MAX_VALUE/2) {
			A -= Short.MAX_VALUE - 1;
		} else if(d < -Short.MAX_VALUE/2) {
			B -= Short.MAX_VALUE - 1;
		}
		return Math.abs(A - B);
	}

}
