package de.vatterger.entitysystem.components;


import com.artemis.Component;
import com.esotericsoftware.kryonet.Connection;

public class ClientConnection extends Component {
	/**The KryoNet Connection Instance*/
	public Connection connection;
	
	public ClientConnection(Connection connection) {
		this.connection = connection;
	}
}
