package de.vatterger.entitysytem.networkmessages;

import com.badlogic.gdx.math.Vector2;

public class D_MoveDir {
	/**The Direction the player wants to let his blob move*/
	public Vector2 moveDir;
	
	public D_MoveDir() {
	}
	
	public D_MoveDir(Vector2 moveDir) {
		this.moveDir = moveDir;
	}
}