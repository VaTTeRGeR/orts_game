package de.vatterger.entitysystem.netservice;

import com.esotericsoftware.kryonet.Connection;

public class Message{
	private Object o;
	private Connection c;
	
	public Message(Object o, Connection c) {
		this.o = o;
		this.c = c;
	}
	
	public Connection getConnection() {
		return c;
	}
	
	public Object getObject() {
		return o;
	}
}
