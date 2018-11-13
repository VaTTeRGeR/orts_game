package de.vatterger.engine.network.layered;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.math.MathUtils;

public class DatagramChannelQueue {
	
	//channel
	
	private		DatagramChannel						datagramChannel;
	
	//settings
	
	private		InetSocketAddress					address_bind;
	
	private		int									buffer_size;
	
	private		volatile	int						datarate_max;
	
	private		volatile	int						updateInterval;

	private		volatile	float					packetDropTime;
	
	//queues
	
	protected	ArrayBlockingQueue<DatagramPacket>	queue_outgoing;
	protected	ArrayBlockingQueue<DatagramPacket>	queue_incoming;
	
	//thread
	
	private		Thread								updateThread;
	
	private		volatile	boolean					isAlive;
	
	//status
	
	private		volatile	int						bytesPerSecond;

	private		volatile	AtomicInteger			bytesInQueue;
	
	public DatagramChannelQueue(InetSocketAddress address_bind) {
		this(address_bind, 100*1024*1024/8); // 100 MBit/s datarate limit
	}
	
	public DatagramChannelQueue(InetSocketAddress address_bind, int datarate_max) {
		this(address_bind, datarate_max, 5*1024*1024); // 5 MByte Socket buffers
	}
	
	public DatagramChannelQueue(InetSocketAddress address_bind, int datarate_max, int buffer_size) {
		this(address_bind, datarate_max, buffer_size, 5, 0.250f); // 5ms update interval aka 200Hz and 250ms packetDropTime
	}
	
	public DatagramChannelQueue(InetSocketAddress address_bind, int datarate_max, int buffer_size, int updateInterval, float packetDropTime) {
		this.address_bind		= address_bind;
		this.datarate_max		= datarate_max;
		this.buffer_size		= buffer_size;
		this.updateInterval		= updateInterval;
		this.packetDropTime		= packetDropTime;
		
		isAlive = false;
	}

	public boolean bind() {
		if(isAlive) return true;
		
		boolean successful = true;
		
		queue_incoming = new ArrayBlockingQueue<DatagramPacket>(32*1024);
		queue_outgoing = new ArrayBlockingQueue<DatagramPacket>(32*1024);
		
		try {
			datagramChannel = DatagramChannel.open();
			
			datagramChannel.configureBlocking(false);
			
			datagramChannel.setOption(StandardSocketOptions.SO_RCVBUF, buffer_size); //25MByte
			datagramChannel.setOption(StandardSocketOptions.SO_SNDBUF, buffer_size); //25MByte
			
			if(buffer_size != datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF)) {
				System.err.println("Couldn't set SO_RCVBUF to " + buffer_size);
				System.err.println("Buffer size is now " + (datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF)) + " Byte");
			}
			
			if(buffer_size != datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF)) {
				System.err.println("Couldn't set SO_SNDBUF to "+ buffer_size);
				System.err.println("Buffer size is now " + (datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF)) + " Byte");
			}
			
			datagramChannel.bind(address_bind);
			
			updateThread = new Thread(new Runnable() {
				
				ByteBuffer inBuffer = ByteBuffer.allocate(1500);
				int bytesSentCorrectionFactor = 0;
				
				@Override
				public void run() {
					try {
						long time_measured = updateInterval*1000000;
						
						bytesPerSecond = 0;
						bytesInQueue = new AtomicInteger(0);
						
						while (!updateThread.isInterrupted()) {
							long time = System.nanoTime();
							
							int bytesSent = 0;
							int bytesSentMax = (int)( ((double)datarate_max) / 1000000000d * (double)time_measured );
							
							int maxBytesInQueue = (int)( (datarate_max * packetDropTime)); // drop packets if it takes more than 250ms to push them out
							
							//Drop packets if they cannot be pushed out within 250ms
							while(!queue_outgoing.isEmpty() && bytesInQueue.get() > maxBytesInQueue) {
								bytesInQueue.getAndAdd(-queue_outgoing.take().getLength());
							}
							
							//Send packets until the datarate constraints have been met or all packets are sent
							while (!queue_outgoing.isEmpty() && bytesSent <= bytesSentMax + bytesSentCorrectionFactor && !updateThread.isInterrupted()) {
								DatagramPacket bundle = queue_outgoing.element();

								int successful = datagramChannel.send(ByteBuffer.wrap(bundle.getData()), bundle.getSocketAddress());
								
								if(successful > 0) {
									bytesInQueue.getAndAdd(-bundle.getLength());
									bytesSent += bundle.getLength();									
									queue_outgoing.take();
								}
							}
							
							//Update the measured datarate
							bytesPerSecond = (int)( (((long)bytesSent*1000000000l)/Math.max(time_measured,updateInterval/4)) )*1/10 + bytesPerSecond*9/10;// /10 + (bytesPerSecond*5)/10;
							
							//Allow unused capacity to be used in the next send period
							if(queue_outgoing.isEmpty()) {
								bytesSentCorrectionFactor = 0;
							} else {
								bytesSentCorrectionFactor += bytesSentMax - bytesSent;
							}
							
							//receive all packets
							InetSocketAddress address_receive;
							while((address_receive = (InetSocketAddress)datagramChannel.receive(inBuffer)) != null && !updateThread.isInterrupted()) {
								byte[] data = Arrays.copyOf(inBuffer.array(), inBuffer.position());
								if(queue_incoming.remainingCapacity() == 0) {
									queue_incoming.clear(); // better flush when it's overloaded, full queue costs lots of cpu.
								}
								queue_incoming.put(new DatagramPacket(data, data.length, address_receive));
								inBuffer.clear();
							}
							
							//sleep a bit
							Thread.sleep(MathUtils.clamp(updateInterval*2 - time_measured/1000000, 0, updateInterval));
							
							//measure the time it took to run the lsat iteration
							time_measured = time = System.nanoTime() - time;
						}
					} catch (InterruptedException | ClosedByInterruptException e) {
						System.err.println("Stopped Thread!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			updateThread.start();
			
		} catch (IOException e) {
			successful = false;
			System.err.println("Failed to initialize DatagramChannelQueue for " + address_bind.getHostName() + ":" + address_bind.getPort());
			e.printStackTrace();
		}
		
		if(successful) {
			System.out.println("Successfully initialized DatagramChannelQueue for " + address_bind.getHostName() + ":" + address_bind.getPort());
		}
		
		return (isAlive = successful);
	}
	
	public void unbind() {
		if(!isAlive) return;
		
		isAlive = false;
		
		try {
			updateThread.interrupt();
			updateThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		updateThread = null;

		queue_incoming.clear();
		queue_outgoing.clear();

		try {
			datagramChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public void write(SocketAddress address, byte[] data) {
		if(!isAlive) return;
		if(queue_outgoing.offer(new DatagramPacket(data, data.length, address))) {
			bytesInQueue.getAndAdd(data.length);
		}
	}
	
	public DatagramPacket read() {
		if(!isAlive) return null;
		return queue_incoming.poll();
	}
	
	
	//DATARATE
	
	public int getDataRateMax() {
		return datarate_max;
	}
	
	public void setDataRateMax(int datarate_max) {
		this.datarate_max = datarate_max;
	}
	
	//BUFFERSIZE
	
	public int getBufferSize() {
		return buffer_size;
	}
	
	public void setBufferSize(int buffersize) {
		this.buffer_size = buffersize;
		if(isAlive) {
			unbind();
			bind();
		}
	}
	
	//UPDATE INTERVAL
	
	public int getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}
	
	//PACKETDROPTIME

	public void setPacketDropTime(float packetDropTime) {
		this.packetDropTime = packetDropTime;
	}
	
	public float getPacketDropTime() {
		return packetDropTime;
	}
	
	//CURRENT BYTES PER SECOND
	
	public int getBytesPerSecond() {
		return bytesPerSecond;
	}
	
	//CURRENT LOAD PERCENTAGE
	
	public float getLoadPercentage() {
		return MathUtils.clamp(((float)bytesPerSecond)/(float)datarate_max, 0f, 1f);
	}
}
