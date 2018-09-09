
package de.vatterger.game.systems.graphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.handler.unit.UnitHandlerJSON;
import de.vatterger.engine.util.Math2D;
import de.vatterger.engine.util.Timer;
import de.vatterger.game.components.gameobject.MoveCurve;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;
import de.vatterger.game.systems.gameplay.TimeSystem;

public class PathTestCalcAndRenderSystem extends BaseSystem {

	private static final float RADIUS = 2f;
	private static float costScl = 0.1f;
	
	
	private Camera camera;
	private ShapeRenderer shapeRenderer;
	
	private boolean clickedLeft;
	private boolean clickedMiddle;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	private Vector2 vBegin = new Vector2();
	private Vector2 vEnd = new Vector2();
	
	private ArrayList<Node>		nodePath = new ArrayList<Node>(128);
	private ArrayList<Vector3>	path = new ArrayList<Vector3>(64);
	
	private Circle c0 = new Circle();
	private Circle c1 = new Circle();
	
	private PriorityQueue<Node>	waitListPrio;
	private ArrayList<Node>		badList;
	
	private int numShowNodesMax;
	
	private Timer timer = new Timer(0.10f);
	
	public PathTestCalcAndRenderSystem(Camera camera) {

		this.camera = camera;

		shapeRenderer = new ShapeRenderer(4096);
		
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void begin() {
		
		if(Gdx.input.isKeyJustPressed(Keys.PLUS)) {
			costScl += 0.05f;
			System.out.println("costScl: " + costScl);
		} else if(Gdx.input.isKeyJustPressed(Keys.MINUS)) {
			costScl -= 0.05f;
			System.out.println("costScl: " + costScl);
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
	
	private void createPath() {
		c0.set(vBegin.x, vBegin.y, RADIUS);
		c1.set(vEnd.x, vEnd.y, RADIUS);
		
		badList		= new ArrayList<Node>(128);
		nodePath	= new ArrayList<Node>(128);
		
		path		= new ArrayList<Vector3>(64);
		
		waitListPrio = new PriorityQueue<Node>(16, new Comparator<Node>() {
			private Vector2 v0 = new Vector2();
			@Override
			public int compare(Node n1, Node n2) {
				float dist1 = v0.set(vEnd.x, vEnd.y).sub(n1.c.x,n1.c.y).len();
				float dist2 = v0.set(vEnd.x, vEnd.y).sub(n2.c.x,n2.c.y).len();
				
				if(dist1 == dist2)
					return 0;
				return dist1 + n1.cost*costScl < dist2 + n2.cost*costScl ? -1 : 1;
			}
		});
		
		int counter = Math.max(1024, numShowNodesMax);
		
		Node startNode = new Node();
		
		Node currentNode = startNode;
		
		if(isCollidingCircle(new Node(new Circle(vBegin.x, vBegin.y, RADIUS), null, new Vector2())) ||
		   isCollidingCircle(new Node(new Circle(vEnd.x, vEnd.y, RADIUS), null, new Vector2()))) {
			counter = 0;
		}
		
		while (!c1.overlaps(currentNode.c) && counter > 0) {

			currentNode.generateNextNodes();
			
			shapeRenderer.setColor(Color.YELLOW);
			shapeRenderer.line(new Vector2(currentNode.prev.c.x,currentNode.prev.c.y), new Vector2(currentNode.c.x,currentNode.c.y));
			
			waitListPrio.addAll(currentNode.next);
			badList.addAll(currentNode.next);
			
			if(!waitListPrio.isEmpty()) {
				currentNode = waitListPrio.poll();
				shapeRenderer.setColor(Color.GREEN);
				shapeRenderer.circle(currentNode.c.x, currentNode.c.y, currentNode.c.radius, 16);
				
			} else {
				break;
			}
			
			counter--;
		}
		
		if(counter >= 2) {
			
			while (!currentNode.isRoot()) {
				nodePath.add(currentNode);
				currentNode = currentNode.prev;
			}
			
			nodePath.add(new Node());
			
			
			for (int i = 0; i < 10; i++) {
				
				if(nodePath.size() < 3) break;
				
				Node n0 = nodePath.get(0 + (i % 2));
				
				int j = 2 + (i % 2);
				while(j < nodePath.size()) {
					
					Node n1 = nodePath.get(j);
					
					//Cannot jump from n0 to n1!
					if(isCollidingLine(n0, n1)) {
						j += 2;
					//Can jump from n0 to n1!
					} else {
						nodePath.remove(j-1);
						j += 1;
					}
					
					n0 = n1;
				}
			}
			
			
			for (Node node : nodePath) {
				shapeRenderer.setColor(Color.BLUE);
				//shapeRenderer.circle(node.c.x, node.c.y, 0.25f, 8);;
				
				path.add(new Vector3(node.c.x, node.c.y, 0f));
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
			
			if(Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT)) {
				int entity = UnitHandlerJSON.createTank("pz6h", new Vector3(vBegin, 0f), world);
				world.edit(entity).add(new MoveCurve(path.toArray(new Vector3[path.size()]), 8f, TimeSystem.getCurrentTime()));
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
	
	private boolean isCollidingCircle(Node testNode) {
		Circle testNodeCircle = new Circle(testNode.c);
		
		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircleB = MaintainCollisionMapSystem.getCircle(i);
			if(testNodeCircle.overlaps(colCircleB)) {
				return true;
			}
		}
		
		testNodeCircle.radius *= 0.95f;
		
		for (Node node : badList) {
			if(node.c.overlaps(testNodeCircle)) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isCollidingLine(Node testNode1, Node testNode2) {
		Vector2 testNode1Vector = new Vector2(testNode1.c.x, testNode1.c.y);
		Vector2 testNode2Vector = new Vector2(testNode2.c.x, testNode2.c.y);

		Vector2 offset90 = new Vector2(testNode2Vector).sub(testNode1Vector).nor().rotate(90f).scl(testNode1.c.radius * 0.5f);
		
		Vector2 circleCenter = new Vector2();
		
		shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);
		
		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircle = MaintainCollisionMapSystem.getCircle(i);
			circleCenter.set(colCircle.x, colCircle.y);
			if(Intersector.intersectSegmentCircle(testNode1Vector, testNode2Vector, circleCenter, colCircle.radius * colCircle.radius)) {
				return true;
			}
		}
		
		testNode1Vector.add(offset90);
		testNode2Vector.add(offset90);

		shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);

		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircle = MaintainCollisionMapSystem.getCircle(i);
			circleCenter.set(colCircle.x, colCircle.y);
			if(Intersector.intersectSegmentCircle(testNode1Vector, testNode2Vector, circleCenter, colCircle.radius * colCircle.radius)) {
				return true;
			}
		}
		
		offset90.scl(-RADIUS);

		testNode1Vector.add(offset90);
		testNode2Vector.add(offset90);
		
		shapeRenderer.setColor(Color.WHITE);
		//shapeRenderer.line(testNode1Vector, testNode2Vector);


		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircle = MaintainCollisionMapSystem.getCircle(i);
			circleCenter.set(colCircle.x, colCircle.y);
			if(Intersector.intersectSegmentCircle(testNode1Vector, testNode2Vector, circleCenter, colCircle.radius * colCircle.radius)) {
				return true;
			}
		}

		return false;
	}
	
	@SuppressWarnings("unused")
	private class Node {
		private Circle			c		= new Circle();
		private Node			prev	= this;

		private Vector2			dir		= new Vector2();
		private ArrayList<Node>	next	= new ArrayList<Node>(1);
		
		private float			cost	= 0f;
		
		public Node() {
			this.c.set(vBegin.x, vBegin.y, RADIUS);
			dir.set(vEnd.x, vEnd.y).sub(vBegin.x, vBegin.y).nor();
		}
		
		public Node(Circle c, Node prev, Vector2 dir) {

			if(prev != null && c != null && dir != null) {
				
				this.cost = prev.cost + 2f * c.radius;
			}
			
			this.c.set(c);
			this.prev = prev;
			this.dir.set(dir);
		}
		
		public void generateNextNodes() {
			
			boolean stop = false;
			boolean foundLeft = false;
			boolean foundRight = false;
			
			Vector2 dirTarget = new Vector2(vEnd.x,vEnd.y).sub(c.x, c.y);
			
			int angle = 0;
			
			while(angle <= 180 - 45 && (!foundLeft || !foundRight) ) {
				if(!foundRight) {
					Vector2 vNextDir = new Vector2(dir);
					vNextDir.rotate(angle);
					
					//float diffAngle = Math.abs(vNextDir.angle() - dirTarget.angle());
					
					vNextDir.nor().scl(2f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(angle));
					//cNode.cost = 2f*this.c.radius*(diffAngle/90f);

					if(!isCollidingCircle(cNode)) {
						next.add(cNode);

						shapeRenderer.setColor(Color.GREEN);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
						//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));

						foundRight = true;

						if(angle == 0) {
							foundLeft = true;
							cNode.dir.setAngle(dirTarget.angle());
						}
						
					} else {
						shapeRenderer.setColor(Color.RED);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
						//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));
					}
				}
				
				if(angle > 0 && !(foundRight && angle == 0)) {
					Vector2 vNextDir = new Vector2(dir);
					vNextDir.rotate(-angle);

					//float diffAngle = Math.abs(vNextDir.angle() - dirTarget.angle());
					
					vNextDir.nor().scl(2f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(-angle));
					//cNode.cost = 2f*this.c.radius*(diffAngle/90f);

					if(!isCollidingCircle(cNode)) {
						next.add(new Node(cTest, this, vNextDir.set(dir).rotate(-angle)));

						shapeRenderer.setColor(Color.GREEN);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
						//shapeRenderer.line(new Vector2(cTest.x,cTest.y), new Vector2(c.x, c.y));
						
						foundLeft = true;

						cNode.dir.setAngle(dirTarget.angle());
						
					} else {
						shapeRenderer.setColor(Color.RED);
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
		
		public boolean foundPathForward() {
			return next.size() > 0;
		}
		
		public boolean foundSplitPathForward() {
			return next.size() > 1;
		}
		
		public boolean isRoot() {
			return this.prev == this;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Node) obj).c.equals(c);
		}
	}
}
