package de.vatterger.entitysystem.components;

import java.util.LinkedList;
import java.util.Queue;

import com.artemis.Component;

public class MessageBucket extends Component {
	public Queue<Object> msg = new LinkedList<Object>();
	public Queue<Integer> msgSize = new LinkedList<Integer>();

	public MessageBucket add(Object o, int size){
		msg.add(o);
		msgSize.add(size);
		return this;
	}
}
