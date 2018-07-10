package de.vatterger.game.systems.graphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.engine.util.Math2D;
import de.vatterger.game.systems.gameplay.MaintainCollisionMapSystem;

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
		} else if(clickedMiddle) {
			Math2D.castRayCam(vBegin, camera);
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
	}
	
	private void createPath() {
		c0.set(vBegin.x, vBegin.y, 2f);
		c1.set(vEnd.x, vEnd.y, 2f);

		badList = new ArrayList<Node>(128);
		
		waitListPrio = new PriorityQueue<Node>(16, new Comparator<Node>() {
			private Vector2 v0 = new Vector2();
			@Override
			public int compare(Node n1, Node n2) {
				float dist1 = v0.set(vEnd.x, vEnd.y).sub(n1.c.x,n1.c.y).len2();
				float dist2 = v0.set(vEnd.x, vEnd.y).sub(n2.c.x,n2.c.y).len2();
				
				if(dist1 == dist2)
					return 0;
				return dist1 < dist2 ? -1 : 1;
			}
		});
		
		int counter = 256;
		
		Node startNode = new Node();
		
		Node currentNode = startNode;
		
		while (!c1.overlaps(currentNode.c) && counter > 0) {
			currentNode.generateNextNodes();
			
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
		
	}

	private void renderPath() {
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.circle(vBegin.x, vBegin.y, 2f, 16);
		
		if(path.size() > 0) {
			shapeRenderer.setColor(Color.YELLOW);
			Vector3 p0 = path.get(0);
			for (int i = 1; i < path.size(); i++) {
				Vector3 p1 = path.get(i);
				
				shapeRenderer.line(p0,p1);
				
				p0 = p1;
			}
		}
		
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.circle(vEnd.x, vEnd.y, 2f, 16);
	}
	
	@Override
	protected void end() {
		shapeRenderer.end();
	}

	@Override
	protected void dispose() {
		shapeRenderer.dispose();
	}
	
	private boolean isColliding(Node testNode) {
		Circle testNodeCircle = new Circle(testNode.c);
		
		for (int i = 0; i < MaintainCollisionMapSystem.getSize(); i++) {
			Circle colCircleB = MaintainCollisionMapSystem.getCircle(i);
			if(testNodeCircle.overlaps(colCircleB)) {
				return true;
			}
		}
		
		testNodeCircle.radius *= 0.5;
		
		for (Node node : badList) {
			if(testNodeCircle.overlaps(node.c)) {
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
		
		public Node() {
			this.c.set(vBegin.x, vBegin.y, 2f);
			dir.set(vEnd.x, vEnd.y).sub(vBegin.x, vBegin.y).nor();
		}
		
		public Node(Circle c, Node prev, Vector2 dir) {
			this.c.set(c);
			this.prev = prev;
			this.dir.set(dir);
		}
		
		public void generateNextNodes() {
			
			boolean stop = false;
			boolean foundLeft = false;
			boolean foundRight = false;
			
			int angle = 0;
			
			while(angle <= 120 && (!foundLeft || !foundRight) ) {
				if(!foundRight) {
					Vector2 vNextDir = new Vector2(dir);
					vNextDir.rotate(angle);
					vNextDir.nor().scl(1.75f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(angle));

					if(!isColliding(cNode)) {
						next.add(cNode);

						foundRight = true;
						
					} else {
						shapeRenderer.setColor(Color.RED);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					}
				}
				
				if(angle > 0 && !foundLeft) {
					Vector2 vNextDir = new Vector2(dir);
					vNextDir.rotate(-angle);
					vNextDir.nor().scl(1.75f * prev.c.radius);
					vNextDir.add(this.c.x,this.c.y);
					
					Circle cTest = new Circle(vNextDir,this.c.radius);
					Node cNode = new Node(cTest, this, vNextDir.set(dir).rotate(angle));

					if(!isColliding(cNode)) {
						next.add(new Node(cTest, this, vNextDir.set(dir).rotate(-angle)));

						foundLeft = true;
						
					} else {
						shapeRenderer.setColor(Color.RED);
						//shapeRenderer.circle(cTest.x, cTest.y, cTest.radius, 16);
					}
				}
				
				//early exit
				if(true && (angle <= 30 && (foundLeft || foundRight))) {
					for (Node nextNode : next) {
						nextNode.dir.set(vEnd.x, vEnd.y).sub(nextNode.c.x, nextNode.c.y).nor();
					}
					//break;
				};
				
				angle += 15;
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
