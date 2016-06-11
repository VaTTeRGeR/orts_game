package de.vatterger.engine.network;

import com.esotericsoftware.kryonet.Connection;

/**
 * A wrapper class to store objects together with a connection
 * @author Florian Schmickmann
 **/
public class KryoNetMessage <T> {
	
	private T o;
	private Connection c;
	private boolean reliable;
	
	/**
	 * @param o The content of this message
	 * @param c The Connection tied to this message
	 * @return The connection this message is tied to
	 **/
	public KryoNetMessage(T o, Connection c, boolean reliable) {
		this.o = o;
		this.c = c;
		this.reliable = reliable;
	}
	
	/**
	 * @param o The content of this message
	 * @param c The Connection tied to this message
	 * @return The connection this message is tied to
	 **/
	public KryoNetMessage(T o, Connection c) {
		this.o = o;
		this.c = c;
		this.reliable = false;
	}
	
	/**
	 * @return The connection this message is tied to
	 **/
	public Connection getConnection() {
		return c;
	}
	
	/**
	 * @return The content of this Message
	 **/
	public T getObject() {
		return o;
	}
	
	public boolean getReliable() {
		return reliable;
	}

	public void send() {
		if(reliable) {
			c.sendTCP(o);
		} else {
			c.sendUDP(o);
		}
	}
}
