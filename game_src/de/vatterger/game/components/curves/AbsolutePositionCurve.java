package de.vatterger.game.components.curves;

import com.badlogic.gdx.utils.FloatArray;

public class AbsolutePositionCurve extends CurveComponent {

	public FloatArray xyz = null;
	
	public AbsolutePositionCurve() {
		this(12);
	}
	
	public AbsolutePositionCurve(int capacity) {

		super(capacity, CurveType.Linear);
		
		xyz = new FloatArray(capacity*3);
	}
}
