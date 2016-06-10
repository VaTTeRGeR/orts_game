package de.vatterger.techdemo.components.server;


import com.artemis.Component;
import com.esotericsoftware.kryonet.Connection;

public class KryoConnection extends Component {
	/**The KryoNet Connection Instance*/
	public Connection connection;
	
	public KryoConnection(Connection connection) {
		this.connection = connection;
	}
}
