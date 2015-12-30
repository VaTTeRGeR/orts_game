package de.vatterger.entitysystem.processors.client;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.handler.network.ClientNetworkHandler;
import de.vatterger.entitysystem.network.packets.ClientViewportUpdate;
import de.vatterger.entitysystem.util.GameUtil;

@Wire
public class SendViewportUpdateProcessor extends EntityProcessingSystem {

	private Camera camera;
	private ImmediateModeRenderer20 lineRenderer;
	private float sendAreaSize = GameConstants.NET_SYNC_AREA * 2f;
	private Rectangle viewport = new Rectangle(0, 0, 0, 0);

	public SendViewportUpdateProcessor(Camera camera, ImmediateModeRenderer20 lineRenderer) {
		super(Aspect.getEmpty());
		this.camera = camera;
		this.lineRenderer = lineRenderer;
	}

	@Override
	protected void begin() {
		viewport.set(camera.position.x - sendAreaSize / 2, camera.position.y - sendAreaSize / 2, sendAreaSize,
				sendAreaSize);
		ClientNetworkHandler.instance().send(new ClientViewportUpdate(viewport), false);

		if (GameConstants.DEBUG_SYNC_AREA) {
			lineRenderer.begin(camera.combined, GL20.GL_LINES);

			Color red = Color.RED;
			GameUtil.line(camera.position.x - sendAreaSize / 2, camera.position.y - sendAreaSize / 2, 0f/**/,
					/**/camera.position.x + sendAreaSize / 2, camera.position.y - sendAreaSize / 2, 0f/**/, /**/red.r,
					red.g, red.b, red.a, lineRenderer);
			GameUtil.line(camera.position.x + sendAreaSize / 2, camera.position.y - sendAreaSize / 2, 0f/**/,
					/**/camera.position.x + sendAreaSize / 2, camera.position.y + sendAreaSize / 2, 0f/**/, /**/red.r,
					red.g, red.b, red.a, lineRenderer);
			GameUtil.line(camera.position.x + sendAreaSize / 2, camera.position.y + sendAreaSize / 2, 0f/**/,
					/**/camera.position.x - sendAreaSize / 2, camera.position.y + sendAreaSize / 2, 0f/**/, /**/red.r,
					red.g, red.b, red.a, lineRenderer);
			GameUtil.line(camera.position.x - sendAreaSize / 2, camera.position.y + sendAreaSize / 2, 0f/**/,
					/**/camera.position.x - sendAreaSize / 2, camera.position.y - sendAreaSize / 2, 0f/**/, /**/red.r,
					red.g, red.b, red.a, lineRenderer);

			lineRenderer.end();
		}
	}

	@Override
	protected void process(Entity e) {
	}
}
