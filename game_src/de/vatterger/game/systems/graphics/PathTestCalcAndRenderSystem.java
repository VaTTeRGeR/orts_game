
package de.vatterger.game.systems.graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;

import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Timer;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.components.gameobject.MovementParameters;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;

public class PathTestCalcAndRenderSystem extends BaseSystem {

	private static final float RADIUS = 1.5f;
	private static float costScl = 0.25f;

	private static int reduceIterations = 10;
	
	@Wire(name = "camera")
	private Camera camera;
	
	private ShapeRenderer shapeRenderer;
	
	private boolean clickedLeft;
	private boolean clickedMiddle;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	private Vector2 vBegin = new Vector2();
	private Vector2 vEnd = new Vector2();
	
	private IntArray nodePath = new IntArray(256);
	private ArrayList<Vector3>	path = new ArrayList<Vector3>(64);
	
	private Circle c0 = new Circle();
	private Circle c1 = new Circle();
	
	private PriorityQueue<Integer> waitListPrio;
	private IntArray badList;
	
	private int numShowNodesMax;
	
	private Timer timer = new Timer(0.10f);
	
	
	//int[]	ni = new int[2048];			// index
	
	private float[]	nx = new float[2048];		// y
	private float[]	ny = new float[2048];		// x
	private float[]	nr = new float[2048];		// radius
	
	private float[]	ndx = new float[2048];		// dir-x
	private float[]	ndy = new float[2048];		// dir-y

	private float[]	nc = new float[2048];		// cost

	private int[]	np = new int[2048];			// parent
	
	private int nextNodeIndex = 0;
	
	
	public PathTestCalcAndRenderSystem() {

		shapeRenderer = new ShapeRenderer(4096);
		
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void begin() {
		
		if(Gdx.input.isKeyJustPressed(Keys.PLUS)) {
			reduceIterations = Math.min(16, reduceIterations + 1);
			System.out.println("it: " + reduceIterations);
			
		} else if(Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			reduceIterations = Math.max(0, reduceIterations - 1);
			System.out.println("it: " + reduceIterations);
		}
		
		clickedLeft = Gdx.input.isButtonPressed(Buttons.LEFT);
		clickedMiddle= Gdx.input.isButtonPressed(Buttons.MIDDLE);
		
		if(clickedLeft) {
			Vector3 vProj = Math2D.castMouseRay(new Vector3(vEnd, 0f), camera);
			vEnd.set(vProj.x, vProj.y);
			
			numShowNodesMax = 1;
			timer.reset();
		} else if(clickedMiddle) {
			Vector3 vProj = Math2D.castMouseRay(new Vector3(vBegin, 0f), camera);
			vBegin.set(vProj.x, vProj.y);
			
			numShowNodesMax = 1;
			timer.reset();
		}
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		shapeRenderer.begin(ShapeType.Line);
		
		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.circle(vBegin.x, vBegin.y, RADIUS, 16);
		shapeRenderer.circle(vEnd.x, vEnd.y, RADIUS, 16);
	}
	
	@Override
	protected void processSystem() {
		createPath();
		renderPath();
		
		timer.update(world.getDelta());
		
		if(timer.isActive()) {
			timer.reset();
			numShowNodesMax++;
		}
	}
	
	Circle ccp0 = new Circle();
	
	private void createPath() {
		c0.set(vBegin.x, vBegin.y, RADIUS);
		c1.set(vEnd.x, vEnd.y, RADIUS);
		
		badList		= new IntArray(512);
		nodePath	= new IntArray(256);
		
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
				return dist1 + nc[i1]*costScl < dist2 + nc[i2]*costScl ? -1 : 1;
			}
		};
		
		waitListPrio = new PriorityQueue<Integer>(64, priorityComparator);
		
		int counter = 2048;//Math.min(2000, numShowNodesMax);
		
		int startNode = createFirstNode();
		
		ccp0.set(nx[startNode], ny[startNode], nr[startNode]);
		
		int currentNode = startNode;
		
		long t_begin = System.currentTimeMillis();
		
		while (!c1.overlaps(ccp0) /*&& counter > 0*/ && System.currentTimeMillis() - t_begin < 10) {

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
			
			counter--;
		}
		
		
		Vector2 v0 = new Vector2();
		
		float oldMinDist = Float.MAX_VALUE;
		
		for (int i : badList.items) {
			float newDist = v0.set(vEnd.x, vEnd.y).sub(nx[i],ny[i]).len();
			if(newDist < oldMinDist) {
				currentNode = i;
				oldMinDist = newDist;
			}
		}
		
		
		if(counter >= 2) {
			
			while (currentNode != np[currentNode]) {
				nodePath.add(currentNode);
				currentNode = np[currentNode];
			}
			
			nodePath.add(0);
			
			for (int i = 0; i < nodePath.size - 1; i++) {
				
				int node1 = nodePath.get(i);
				int node2 = nodePath.get(i + 1);
				
				shapeRenderer.setColor(Color.YELLOW);
				shapeRenderer.circle(nx[node1], ny[node1], 0.25f, 8);;
				shapeRenderer.line(new Vector2(nx[node1], ny[node1]), new Vector2(nx[node2], ny[node2]));
			}
			
			for (int i = 0; i < reduceIterations; i++) {
				
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
			
			for (int node : nodePath.items) {
				shapeRenderer.setColor(Color.BLUE);
				shapeRenderer.circle(nx[node], ny[node], 0.25f, 8);;
				
				path.add(new Vector3(nx[node], ny[node], 0f));
			}
		}
	}

	private void renderPath() {
		shapeRenderer.setColor(Color.BLUE);
		//shapeRenderer.circle(vBegin.x, vBegin.y, 2f, 16);
		
		
		if(path.size() > 0) {
			
			//shapeRenderer.line(vBegin,path.get(path.size()-1));
			
			Vector3 p0 = path.get(0);
			for (int i = 1; i < path.size(); i++) {
				Vector3 p1 = path.get(i);
				
				shapeRenderer.line(p0,p1);
				
				p0 = p1;
			}
			
			if(Gdx.input.isKeyJustPressed(Keys.CONTROL_LEFT)) {
				int entity = UnitHandlerJSON.createTank("pz6h", new Vector3(vBegin, 0f), world);
				world.edit(entity).add(new MoveCurve(path.toArray(new Vector3[path.size()]), new MovementParameters()));
			}
			
		}
		
		//shapeRenderer.circle(vEnd.x, vEnd.y, 2f, 16);
	}
	
	@Override
	protected void end() {
		shapeRenderer.end();
	}

	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
	
	/*int icr;
	
	private Vector2 calcAverage(Node n) {
		
		Vector2 avg = new Vector2(n.c.x, n.c.y);
		
		icr = 20;
		
		calcAvgRecursive(n.prev, n, avg, 0.1f);
		
		return avg;
	}
	
	private void calcAvgRecursive(Node n, Node prev, Vector2 avg, float alpha) {
		if(icr > 0 && n != null) {
			
			icr--;
			
			avg.interpolate(new Vector2(n.c.x, n.c.y), alpha, Interpolation.linear);
			
			for (Node next : n.next) {
				if(next != prev) {
					calcAvgRecursive(next, n,avg, 0.01f);
				}
			}
			
			if(n.prev != prev) {
				calcAvgRecursive(n.prev, n, avg, 0.1f);
			}
			
		} else {
			return;
		}
	}*/
	
	
	private static final boolean overlaps(float x1,float y1, float r1, float x2,float y2, float r2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float distance = dx * dx + dy * dy;
		
		float radiusSum = r1+r2;
		return distance < radiusSum * radiusSum;
	}
	
	//private Circle testNodeCircle = new Circle();
	//private Circle testNodeCircle2 = new Circle();
	
	private final boolean isCollidingCircle(int testNode) {
		
		float nxt = nx[testNode];
		float nyt = ny[testNode];
		float nrt = nr[testNode];
		
		float[] data = MaintainCollisionMapSystem.getData(nxt-nrt, nyt-nrt, nxt+nrt, nyt+nrt);
		
		//System.out.println("circle col with " + ((int)data[0]) + " circles.");
		
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
		
		float mx = (nx[testNode1] + nx[testNode2]) * 0.5f;
		float my = (ny[testNode1] + ny[testNode2]) * 0.5f;
		float dx2 = Math.abs(nx[testNode1] - nx[testNode2]);
		float dy2 = Math.abs(ny[testNode1] - ny[testNode2]);
		
		float[] data = MaintainCollisionMapSystem.getData(mx - dx2, my - dy2, mx + dx2, my + dy2);
		int size = (int)data[0];
		
		//System.out.println("line-line col with " + size + " circles.");
		
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
		
	private Vector2 vcn0 = new Vector2();
		
	private int createFirstNode() {
		
		nextNodeIndex = 0;
		
		nx[nextNodeIndex] = vBegin.x;
		ny[nextNodeIndex] = vBegin.y;
		nr[nextNodeIndex] = RADIUS;
		
		vcn0.set(vEnd.x, vEnd.y).sub(vBegin.x, vBegin.y).nor();
		
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
