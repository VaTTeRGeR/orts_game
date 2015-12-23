package de.vatterger.entitysystem.components.shared;

import com.artemis.Component;

public class PreProcessorTask extends Component {
	public Runnable runnable;
	public PreProcessorTask(Runnable r) {
		this.runnable = r;
	}
}
