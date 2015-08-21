package de.vatterger.entitysystem.netservice;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class KryoSendRunnable implements Runnable {

	/**To be sent messages are stored in this queue*/
	private BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<Message>();
	
	@Override
	public void run() {
		Message m = null;
		try {
			while (true) {
				m = sendQueue.take();
				m.getConnection().sendTCP(m.getObject());
			}
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
	
	public void enqueue(Message m) {
		sendQueue.add(m);
	}
}
