package de.vatterger.game.components.gameobject;

import com.artemis.Component;

public class MovementParameters extends Component {
	
	/** Speed of turning on the spot in degrees/second **/
	public float	turnSpeed;
	/** Maximum angle-change that may be executed while moving without stopping in degrees **/
	public float	fastTurnMaxAngle;
	/** Speed of moving forwards in meters/second **/
	public float	forwardSpeed;
	/** Speed of moving backwards in meters/second **/
	public float	backwardSpeed;
	/** Speed of moving backwards in (meters/second)/second **/
	public float	acceleration;

	public MovementParameters() {
		turnSpeed		= 45.0f;
		fastTurnMaxAngle= 30.0f;
		forwardSpeed	= 10.0f;
		backwardSpeed	= 5.0f;
		acceleration	= 5.0f;
	}
	
	
}
