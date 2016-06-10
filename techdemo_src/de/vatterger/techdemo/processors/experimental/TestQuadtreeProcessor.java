package de.vatterger.techdemo.processors.experimental;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.components.client.InterpolatedPosition;
import de.vatterger.techdemo.quadtree.Quadtree;
import de.vatterger.techdemo.quadtree.SpatialEntry;

public class TestQuadtreeProcessor extends EntityProcessingSystem {

	private ComponentMapper<InterpolatedPosition> cpm;
	private Quadtree<Rectangle> tree = new Quadtree<Rectangle>(new Rectangle(0,0,GameConstants.XY_BOUNDS, GameConstants.XY_BOUNDS), 8, 1);
	private ImmediateModeRenderer20 imr20;
	private Camera cam;

	public TestQuadtreeProcessor(ImmediateModeRenderer20 imr20, Camera cam) {
		super(Aspect.all(InterpolatedPosition.class));
		this.imr20 = imr20;
		this.cam = cam;
	}

	@Override
	protected void initialize() {
		cpm = world.getMapper(InterpolatedPosition.class);
	}
	
	@Override
	protected void process(Entity e) {
		Rectangle r = new Rectangle(cpm.get(e).getInterpolatedValue().x, cpm.get(e).getInterpolatedValue().y, 3f, 3f);
		tree.insert(new SpatialEntry<Rectangle>(r, r));
	}
	
	@Override
	protected void end() {
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		imr20.begin(cam.combined, GL20.GL_LINES);
		tree.render(imr20);
		imr20.end();
		tree.clear();
	}
}