package de.vatterger.engine.handler.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;

public class PathFinder {

	private static final float RADIUS = 5f;
	private static final float COST_SCALE = 0.15f;
	private static final int REDUCE_ITERATIONS = 10;
	
	
	private Vector2 vBegin = new Vector2();
	private Vector2 vEnd = new Vector2();
	
	private Circle c1 = new Circle();
	
	private IntArray nodePath = new IntArray(true,256);
	private ArrayList<Vector3>	path = new ArrayList<Vector3>(64);
	
	
	
	private PriorityQueue<Integer> waitListPrio;
	
	private IntArray badList;
	
	
	//private int[]	ni = new int[2048];			// index
	
	private float[]	nx = new float[2048];		// y
	private float[]	ny = new float[2048];		// x
	private float[]	nr = new float[2048];		// radius
	
	private float[]	ndx = new float[2048];		// dir-x
	private float[]	ndy = new float[2048];		// dir-y

	private float[]	nc = new float[2048];		// cost

	private int[]	np = new int[2048];			// parent
	
	private int nextNodeIndex = 0;
	
	
	Circle ccp0 = new Circle();
	
	
	public ArrayList<Vector3> createPath(Vector3 start, Vector3 target, long maxTimeMillis) {
		
		vBegin.set(start.x, start.y);
		vEnd.set(target.x, target.y);
		
		c1.set(target.x, target.y, RADIUS);
		
		badList		= new IntArray(true,512);
		nodePath	= new IntArray(true,256);
		
		path		= new ArrayList<Vector3>(128);
		
		nnn_size	= 0;
		
		
		Comparator<Integer> priorityComparator = new Comparator<Integer>() {
			
			private Vector2 v0 = new Vector2();
			
			@Override
			public int compare(Integer n1, Integer n2) {
				
				int i1 = n1.intValue();
				int i2 = n2.intValue();
				
				float dist1 = v0.set(vEnd.x, vEnd.y).sub(nx[i1],ny[i1]).len();
				float dist2 = v0.set(vEnd.x, vEnd.y).sub(nx[i2],ny[i2]).len();
				
				if(dist1 == dist2)
					return 0;
				
				return dist1 + nc[i1]*COST_SCALE < dist2 + nc[i2]*COST_SCALE ? -1 : 1;
			}
		};
		
		waitListPrio = new PriorityQueue<Integer>(64, priorityComparator);
		
		int startNode = createFirstNode();
		
		ccp0.set(nx[startNode], ny[startNode], nr[startNode]);
		
		int currentNode = startNode;
		
		long t_begin = System.currentTimeMillis();
		boolean timeout = false;
		
		while (!ccp0.overlaps(c1) && !timeout) {

			if(System.currentTimeMillis() - t_begin >= maxTimeMillis) {
				timeout = true;
			}
			
			generateNextNodes(currentNode);
			
			//shapeRenderer.setColor(Color.YELLOW);
			//shapeRenderer.line(new Vector2(nx[np[currentNode]], ny[np[currentNode]]), new Vector2(nx[currentNode], ny[currentNode]));
			
			while(nnn_size > 0) {
				
				waitListPrio.add(nnn[nnn_size-1]);
				badList.add(nnn[nnn_size-1]);
				
				nnn_size--;
			}
			
			
			if(!waitListPrio.isEmpty()) {
				
				currentNode = waitListPrio.poll();
				
				ccp0.set(nx[currentNode], ny[currentNode], nr[currentNode]);
				
				//shapeRenderer.setColor(Color.GREEN);
				//shapeRenderer.circle(nx[currentNode], ny[currentNode], nr[currentNode], 16);
				
			} else {
				break;
			}
		}
		
		
		Vector2 v0 = new Vector2();
		
		float oldMinDist = Float.MAX_VALUE;
		
		badList.shrink();
		for (int i : badList.items) {
			float newDist = v0.set(vEnd.x, vEnd.y).sub(nx[i],ny[i]).len();
			if(newDist < oldMinDist) {
				currentNode = i;
				oldMinDist = newDist;
			}
		}
		
		
		if(!timeout) {
			
			while (currentNode != np[currentNode]) {
				nodePath.add(currentNode);
				currentNode = np[currentNode];
			}
			
			
			/// ??? IRGENDEINE HALB ANGEFANGENE OPTIMIERUNG ???
			/*for (int i = 0; i < nodePath.size - 1; i++) {
				
				int node1 = nodePath.get(i);
				int node2 = nodePath.get(i + 1);
				
			}*/
			
			for (int i = 0; i < REDUCE_ITERATIONS; i++) {
				
				if(nodePath.size < 3) break;
				
				int n0 = nodePath.get(0 + (i % 2));
				
				int j = 2 + (i % 2);
				while(j < nodePath.size) {
					
					int n1 = nodePath.get(j);
					
					//Cannot jump from n0 to n1!
					if(isCollidingLine(n0, n1)) {
						j += 2;
					//Can jump from n0 to n1!
					} else {
						nodePath.removeIndex(j-1);
						j += 1;
					}
					
					n0 = n1;
				}
			}
			
			for (int i = 0; i < nodePath.size-1-i; i++) {
				nodePath.swap(i, nodePath.size-1-i);
			}
			
			
			path.add(new Vector3(vBegin.x, vBegin.y, 0f));
			
			nodePath.shrink();
			for (int node : nodePath.items) {
				
				path.add(new Vector3(nx[node], ny[node], 0f));
				
			}
			

			//System.out.println(Arrays.toString(path.toArray()));
			
		}
		
		
		return path;
	}
	
	private final static  boolean overlaps(float x1,float y1, float r1, float x2,float y2, float r2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float distance = dx * dx + dy * dy;
		
		float radiusSum = r1+r2;
		return distance < radiusSum * radiusSum;
	}
	
	private final boolean isCollidingCircle(int testNode) {
		
		float[] data = MaintainCollisionMapSystem.getData();
		
		float nxt = nx[testNode];
		float nyt = ny[testNode];
		float nrt = nr[testNode];
		
		int imax = ((int)data[0]) * 3;
		for (int i = 1; i < imax + 1;) {
			
			if(overlaps(nxt, nyt, nrt, data[i++], data[i++], data[i++])) {
				return true;
			}
		}
		
		float radius095 = nr[testNode] * 0.95f;
		
		for (int i = badList.size - 1; i >= 0 && badList.size-1-i < 512; i--) {
			int badNode = badList.items[i];
			if(overlaps(nxt, nyt, radius095, nx[badNode], ny[badNode], nr[badNode])) {
				return true;
			}
		}
		
		return false;
	}

	private final boolean isCollidingLine(int testNode1, int testNode2) {
		Vector2 testNode1Vector = new Vector2(nx[testNode1], ny[testNode1]);
		Vector2 testNode2Vector = new Vector2(nx[testNode2], ny[testNode2]);

		Vector2 offset90 = new Vector2(testNode2Vector).sub(testNode1Vector).nor().rotate(90f).scl(nr[testNode1] * 0.5f);
		
		Vector2 circleCenter = new Vector2();
		
		//shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);
		
		float[] data = MaintainCollisionMapSystem.getData();
		int size = (int)data[0];
		
		for (int i = 0; i < size; i++) {
			if(testCircleFromData(data, i, circleCenter, testNode1Vector, testNode2Vector)) {
				return true;
			}
		}
		
		testNode1Vector.add(offset90);
		testNode2Vector.add(offset90);

		//shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);

		for (int i = 0; i < size; i++) {
			if(testCircleFromData(data, i, circleCenter, testNode1Vector, testNode2Vector)) {
				return true;
			}
		}
		
		offset90.scl(-RADIUS);

		testNode1Vector.add(offset90);
		testNode2Vector.add(offset90);
		
		//shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);


		for (int i = 0; i < size; i++) {
			if(testCircleFromData(data, i, circleCenter, testNode1Vector, testNode2Vector)) {
				return true;
			}
		}

		return false;
	}
	
	private static final boolean testCircleFromData(float[] data, int index, Vector2 circleCenter, Vector2 seg0, Vector2 seg1) {

		index = (index * 3) + 1;
		
		circleCenter.set(data[index++], data[index++]);
		
		return Intersector.intersectSegmentCircle(seg0, seg1, circleCenter, data[index] * data[index]);
	}
		
	private int createFirstNode() {
		
		nextNodeIndex = 0;
		
		nx[nextNodeIndex] = vBegin.x;
		ny[nextNodeIndex] = vBegin.y;
		nr[nextNodeIndex] = RADIUS;
		
		Vector2 vcn0 = new Vector2(vEnd.x, vEnd.y).sub(vBegin.x, vBegin.y).nor();
		
		ndx[nextNodeIndex] = vcn0.x;
		ndy[nextNodeIndex] = vcn0.y;

		np[nextNodeIndex] = nextNodeIndex;
		
		nc[nextNodeIndex] = 0;
		
		return nextNodeIndex++;
	}
	
	private int createNode(float x, float y, float r, float dx, float dy, int prev) {
		
		if(nextNodeIndex == nx.length) {
			resizeNodeArray((nextNodeIndex * 3) / 2);
		}
		
		int i = nextNodeIndex;
		
		nx[i] = x;
		ny[i] = y;
		nr[i] = r;
		ndx[i] = dx;
		ndy[i] = dy;
		np[i] = prev;
		
		if(prev >= 0)
			nc[i] = nc[prev] + r + r;
		else
			nc[i] = 0;
		
		return nextNodeIndex++;
	}
	
	private void resizeNodeArray(int newSize) {
		
		newSize = Math.max(newSize, nextNodeIndex);
		
		//ni = Arrays.copyOf(ni, newSize);
		nx = Arrays.copyOf(nx, newSize);
		ny = Arrays.copyOf(ny, newSize);
		nr = Arrays.copyOf(nr, newSize);
		ndx = Arrays.copyOf(ndx, newSize);
		ndy = Arrays.copyOf(ndy, newSize);
		np = Arrays.copyOf(np, newSize);
		nc = Arrays.copyOf(nc, newSize);
	}
	
	private Circle cgnn0 = new Circle();
	private Vector2 vgnn0 = new Vector2();
	
	private int[] nnn = new int[360/15];
	private int   nnn_size = 0;
	
	public void generateNextNodes(int nodeId) {
		
		nnn_size = 0;
		
		//boolean stop = false;
		boolean foundLeft = false;
		boolean foundRight = false;
		
		cgnn0.set(nx[nodeId], ny[nodeId], nr[nodeId]);
		vgnn0.set(ndx[nodeId], ndy[nodeId]);
		
		Vector2 dirTarget = new Vector2(vEnd.x,vEnd.y).sub(cgnn0.x, cgnn0.y).nor();
		
		int angle = 0;
		
		while(angle <= 180 - 45 && (!foundLeft || !foundRight) ) {
			if(!foundRight) {
				Vector2 vNextDir = new Vector2(vgnn0);
				vNextDir.rotate(angle);
				
				//float diffAngle = Math.abs(vNextDir.angle() - dirTarget.angle());
				
				vNextDir.nor().scl(2f * nr[np[nodeId]]);
				vNextDir.add(cgnn0.x, cgnn0.y);
				
				Circle cTest = new Circle(vNextDir, nr[nodeId]);
				vNextDir.set(ndx[nodeId], ndy[nodeId]).rotate(angle);
				int cNode = createNode(cTest.x, cTest.y, cTest.radius, vNextDir.x, vNextDir.y, nodeId);
				//cNode.cost = 2f*this.c.radius*(diffAngle/90f);

				if(!isCollidingCircle(cNode)) {
					nnn[nnn_size++] = cNode;

					//shapeRenderer.setColor(Color.GREEN);
					//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));

					foundRight = true;

					if(angle == 0) {
						
						foundLeft = true;
						
						vNextDir.set(ndx[cNode], ndy[cNode]).nor().setAngle(dirTarget.angle());
						
						ndx[cNode] = vNextDir.x;
						ndy[cNode] = vNextDir.y;
					}
					
				} else {
					
					nextNodeIndex--;
					
					//shapeRenderer.setColor(Color.RED);
					//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));
				}
			}
			
			if(angle > 0 && !(foundRight && angle == 0)) {
				Vector2 vNextDir = new Vector2(vgnn0);
				vNextDir.rotate(-angle);

				//float diffAngle = Math.abs(vNextDir.angle() - dirTarget.angle());
				
				vNextDir.nor().scl(2f * nr[np[nodeId]]);
				vNextDir.add(cgnn0.x, cgnn0.y);
				
				Circle cTest = new Circle(vNextDir.x, vNextDir.y, nr[nodeId]);
				vNextDir.set(ndx[nodeId], ndy[nodeId]).rotate(-angle);
				int cNode = createNode(cTest.x, cTest.y, cTest.radius, vNextDir.x, vNextDir.y, nodeId);
				//cNode.cost = 2f*this.c.radius*(diffAngle/90f);

				if(!isCollidingCircle(cNode)) {
					nnn[nnn_size++] = cNode;

					//shapeRenderer.setColor(Color.GREEN);
					//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));
					
					foundLeft = true;
					
					vNextDir.set(ndx[cNode], ndy[cNode]).nor().setAngle(dirTarget.angle());
					
					ndx[cNode] = vNextDir.x;
					ndy[cNode] = vNextDir.y;
					
				} else {
					
					nextNodeIndex--;
					
					//shapeRenderer.setColor(Color.RED);
					//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));
				}
			}
			
			
			if(angle == 0 && (foundLeft || foundRight)) {
				angle += 90;
				foundLeft = foundRight = false;
			} else {
				angle += 15;
			}
		}
	}
}
