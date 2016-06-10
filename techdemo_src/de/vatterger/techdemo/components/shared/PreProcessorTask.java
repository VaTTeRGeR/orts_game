package de.vatterger.techdemo.components.shared;

import com.artemis.Component;

public class PreProcessorTask extends Component {
	public Runnable runnable;
	public PreProcessorTask(Runnable r) {
		this.runnable = r;
	}
}
