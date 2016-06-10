package de.vatterger.techdemo.processors.client;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.techdemo.application.GameConstants;
import de.vatterger.techdemo.handler.network.ClientNetworkHandler;
import de.vatterger.techdemo.network.packets.client.ViewportUpdate;
import de.vatterger.techdemo.util.GameUtil;

public class SendViewportUpdateProcessor extends BaseSystem {

	private Camera camera;
	private ImmediateModeRenderer20 lineRenderer;
	private Rectangle viewport = new Rectangle(0, 0, 0, 0);

	public SendViewportUpdateProcessor(Camera camera, ImmediateModeRenderer20 lineRenderer) {
		this.camera = camera;
		this.lineRenderer = lineRenderer;
	}

	@Override
	protected void processSystem() {
		float sendAreaSize = GameConstants.NET_SYNC_THRESHOLD;
		viewport.set(camera.position.x - sendAreaSize / 2, camera.position.y - sendAreaSize / 2, sendAreaSize, sendAreaSize);
		ClientNetworkHandler.instance().send(new ViewportUpdate(viewport), false);

		if (GameConstants.DEBUG_SYNC_AREA) {
			lineRenderer.begin(camera.combined, GL20.GL_LINES);
			
			Color red = Color.RED;
			GameUtil.aabb(viewport, 0.1f, red, lineRenderer);
			
			lineRenderer.end();
		}
	}
}
