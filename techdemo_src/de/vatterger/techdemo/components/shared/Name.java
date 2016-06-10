package de.vatterger.techdemo.components.shared;


import com.artemis.Component;

public class Name extends Component {
	/**The Name of the Entity*/
	public String name;

	public Name() {
		name = "unnamed";
	}
	
	public Name(String name) {
		this.name = name;
	}
}
