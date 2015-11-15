package de.vatterger.entitysystem.network;

import com.esotericsoftware.kryonet.Connection;

/**
 * A wrapper class to store objects together with a connection
 * @author Florian Schmickmann
 **/
public class MessageRemote<T>{
	
	private T o;
	private Connection c;
	
	/**
	 * @param o The content of this message
	 * @param c The Connection tied to this message
	 * @return The connection this message is tied to
	 **/
	public MessageRemote(T o, Connection c) {
		this.o = o;
		this.c = c;
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
}
