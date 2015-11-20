package de.vatterger.entitysystem.components;


import java.util.PriorityQueue;

import com.artemis.Component;

public class NetPriorityQueue extends Component {
	public PriorityQueue<Integer> queue = new PriorityQueue<Integer>();
}
