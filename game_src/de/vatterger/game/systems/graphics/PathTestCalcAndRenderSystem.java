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

	private Camera camera;
	private ShapeRenderer shapeRenderer;
	
	private boolean clickedLeft;
	private boolean clickedMiddle;
	
	//private SpriteBatch batch;
	//private BitmapFont font;
	
	private Vector3 vBegin = new Vector3();
	private Vector3 vEnd = new Vector3();
	
	private ArrayList<Vector3> path = new ArrayList<Vector3>(64);
	
	private Circle c0 = new Circle();
	private Circle c1 = new Circle();
	
	private PriorityQueue<Node>	waitListPrio;
	private ArrayList<Node>		badList;
	
	private int numShowNodesMax;
	
	private Timer timer = new Timer(0.01f);
	
	public PathTestCalcAndRenderSystem(Camera camera) {

		this.camera = camera;

		shapeRenderer = new ShapeRenderer(4096);
		
		//font = new BitmapFont();
		//batch = new SpriteBatch(64);
	}
	
	@Override
	protected void begin() {
		clickedLeft = Gdx.input.isButtonPressed(Buttons.LEFT);
		clickedMiddle= Gdx.input.isButtonPressed(Buttons.MIDDLE);
		
		if(clickedLeft) {
			Math2D.castRayCam(vEnd, camera);
			numShowNodesMax = 1;
			timer.reset();
		} else if(clickedMiddle) {
			Math2D.castRayCam(vBegin, camera);
			numShowNodesMax = 1;
			timer.reset();
		}
		
		shapeRenderer.setProjectionMatrix(camera.combined/*.cpy().scl(1f, Metrics.ymodp, 1f)*/);
		shapeRenderer.setTransformMatrix(new Matrix4(new Vector3(0f, 0f, camera.position.y - 1024f),new Quaternion(Vector3.X, -45f), new Vector3(1f, 1f, 1f)));
		shapeRenderer.updateMatrices();
		shapeRenderer.begin(ShapeType.Line);
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
		c0.set(vBegin.x, vBegin.y, 2f);
		c1.set(vEnd.x, vEnd.y, 2f);

		badList = new ArrayList<Node>(128);
		
		waitListPrio = new PriorityQueue<Node>(16, new Comparator<Node>() {
			private Vector2 v0 = new Vector2();
			@Override
			public int compare(Node n1, Node n2) {
				float dist1 = v0.set(vEnd.x, vEnd.y).sub(n1.c.x,n1.c.y).len();
				float dist2 = v0.set(vEnd.x, vEnd.y).sub(n2.c.x,n2.c.y).len();
				
				if(dist1 == dist2)
					return 0;
				return dist1 + n1.cost < dist2 + n2.cost ? -1 : 1;
			}
		});
		
		int counter = Math.max(1024, numShowNodesMax);
		
		Node startNode = new Node();
		
		Node currentNode = startNode;
		
		while (!c1.overlaps(currentNode.c) && counter > 0) {

			currentNode.generateNextNodes();
			
			shapeRenderer.setColor(Color.YELLOW);
			shapeRenderer.line(new Vector2(currentNode.prev.c.x,currentNode.prev.c.y), new Vector2(currentNode.c.x,currentNode.c.y));
			
			waitListPrio.addAll(currentNode.next);
			badList.addAll(currentNode.next);
			
			if(!waitListPrio.isEmpty()) {
				currentNode = waitListPrio.poll();
				//shapeRenderer.setColor(Color.GREEN);
				////shapeRenderer.circle(currentNode.c.x, currentNode.c.y, currentNode.c.radius, 16);
			} else {
				break;
			}
			
			counter--;
		}
		
		if(counter > 0) {
			Node nextCurrentNode = currentNode.prev;
			
			while(nextCurrentNode != currentNode) {
				
				while(!isCollidingLine(currentNode, nextCurrentNode.prev) && nextCurrentNode != nextCurrentNode.prev) {
					nextCurrentNode = nextCurrentNode.prev;
				}
				
				path.add(new Vector3(currentNode.c.x, currentNode.c.y, 0f));
				currentNode = nextCurrentNode;
				
				nextCurrentNode = currentNode.prev;
			}
		}
	}

	private void renderPath() {
		shapeRenderer.setColor(Color.BLUE);
		//shapeRenderer.circle(vBegin.x, vBegin.y, 2f, 16);
		
		
		if(path.size() > 0) {
			
			shapeRenderer.line(vBegin,path.get(path.size()-1));
			
			Vector3 p0 = path.get(0);
			for (int i = 1; i < path.size(); i++) {
				Vector3 p1 = path.get(i);
				
				shapeRenderer.line(p0,p1);
				
				p0 = p1;
			}
			
			if(Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT)) {
				int entity = UnitHandlerJSON.createTank("pz6h", vBegin, world);
				world.edit(entity).add(new MoveCurve(path.toArray(new Vector3[path.size()]), 8f, TimeSystem.getCurrentTime()));
			}
			
		}
		
		//shapeRenderer.circle(vEnd.x, vEnd.y, 2f, 16);
		
		path.clear();
	}
	
	@Override
	protected void end() {
		shapeRenderer.end();
	}

	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
	
	private boolean isCollidingCircle(Node testNode) {
		Circle testNodeCircle = new Circle(testNode.c);
		
		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircleB = MaintainCollisionMapSystem.getCircle(i);
			if(testNodeCircle.overlaps(colCircleB)) {
				return true;
			}
		}
		
		testNodeCircle.radius *= 0.99f;
		
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

		Vector2 offset90 = new Vector2(testNode2Vector).sub(testNode1Vector).nor().rotate(90f).scl(testNode1.c.radius);
		
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
		
		offset90.scl(-2f);

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
		
		private int				directPathCount = 0;
		
		public Node() {
			this.c.set(vBegin.x, vBegin.y, 2f);
			dir.set(vEnd.x, vEnd.y).sub(vBegin.x, vBegin.y).nor();
		}
		
		public Node(Circle c, Node prev, Vector2 dir) {
			this.c.set(c);
			this.prev = prev;
			this.dir.set(dir);
			this.cost = prev.cost + c.radius * 2;
			this.directPathCount = prev.directPathCount++;
		}
		
		public void generateNextNodes() {
			
			boolean stop = false;
			boolean foundLeft = false;
			boolean foundRight = false;
			
			Vector2 dirTarget = new Vector2(vEnd.x,vEnd.y).sub(c.x, c.y);
			
			int angle = 0;
			
			while(angle <= 180 - 30 && (!foundLeft || !foundRight) ) {
				if(!foundRight) {
					Vector2 vNextDir = new Vector2(dir);
					vNextDir.rotate(angle);
					vNextDir.nor().scl(2f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(angle));
					cNode.cost += 2f*this.c.radius*(angle/90f);

					if(!isCollidingCircle(cNode)) {
						next.add(cNode);

						shapeRenderer.setColor(Color.GREEN);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);

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
					vNextDir.nor().scl(2f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(-angle));
					cNode.cost += 8f*this.c.radius*(angle/90f);

					if(!isCollidingCircle(cNode)) {
						next.add(new Node(cTest, this, vNextDir.set(dir).rotate(-angle)));

						shapeRenderer.setColor(Color.GREEN);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
						
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
					angle += 30;
				}
			}
		}
		
		public boolean foundPathForward() {
			return next.size() > 0;
		}
		
		public boolean foundSplitPathForward() {
			return next.size() > 1;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((Node) obj).c.equals(c);
		}
	}
}
