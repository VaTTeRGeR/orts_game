package de.vatterger.game.components.curves;

import java.util.Arrays;

import com.artemis.Component;
import com.badlogic.gdx.utils.LongArray;

public class CurveComponent extends Component {

	public enum CurveType { Linear , Step , Point }
	
	public CurveType	curveType	= null;
	
	public LongArray	timeDeltas	= null;

	private int[]		indices		= null;

	public CurveComponent() {
		this(8, CurveType.Linear);
	}
	
	public CurveComponent(int capacity, CurveType curveType) {
		
		this.curveType = curveType;
		
		// 64 byte target size!
		this.indices = new int[Math.min(64/4 - 12/4, capacity)];
		
		this.timeDeltas = new LongArray(true, Math.max(capacity, 0));
	}
	
	public int[] getIndexesInInterval(long intervalStart, long intervalEnd) {
		
		if(timeDeltas == null) {
			
			Arrays.fill(indices, -1);
			
			return indices;
		}
		

		int k = 0;
		
		for (int i = 0; i < timeDeltas.size; i++) {
			
			if(k > indices.length - 1) {
				indices = Arrays.copyOf(indices, (int)(indices.length * 1.5f + 0.5f));
			}
			
			if(timeDeltas.items[i] < intervalStart) continue;
			
			if(timeDeltas.items[i] > intervalEnd) break;
			
			indices[k++] = i;
		}
		
		
		Arrays.fill(indices, k, indices.length, -1);
		
		
		return indices;
	}
}
