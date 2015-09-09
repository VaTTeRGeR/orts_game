package de.vatterger.entitysystem.netservice;

import com.esotericsoftware.kryonet.Connection;

/**
 * A wrapper class to store objects together with a connection
 * @author Florian Schmickmann
 **/
public class MessageReceived{
	
	private Object o;
	private Connection c;
	
	/**
	 * @param o The content of this message
	 * @param c The Connection tied to this message
	 * @return The connection this message is tied to
	 **/
	public MessageReceived(Object o, Connection c) {
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
	public Object getObject() {
		return o;
	}
}
