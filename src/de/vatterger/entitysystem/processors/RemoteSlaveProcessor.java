package de.vatterger.entitysystem.processors;

import static de.vatterger.entitysystem.GameConstants.*;

import javax.security.auth.callback.LanguageCallback;

import com.artemis.Aspect;
import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.annotations.Wire;
import com.artemis.systems.EntityProcessingSystem;
import com.artemis.utils.Bag;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Rectangle;
import com.esotericsoftware.kryonet.Listener.LagListener;

import de.vatterger.entitysystem.GameConstants;
import de.vatterger.entitysystem.components.client.RemoteSlave;
import de.vatterger.entitysystem.components.shared.Inactive;
import de.vatterger.entitysystem.network.ClientNetworkService;
import de.vatterger.entitysystem.network.MessageRemote;
import de.vatterger.entitysystem.network.FilteredListener;
import de.vatterger.entitysystem.network.messages.ClientViewportUpdate;
import de.vatterger.entitysystem.network.messages.RemoteMasterUpdate;
import de.vatterger.entitysystem.util.GameUtil;

@Wire
public class RemoteSlaveProcessor extends EntityProcessingSystem {

	private ComponentMapper<RemoteSlave>	rsm;
	
	private Bag<RemoteMasterUpdate> updateRegister = new Bag<RemoteMasterUpdate>(1);
	private Bag<Entity> slaveRegister = new Bag<Entity>(1);
	
	FilteredListener<RemoteMasterUpdate> listener = new FilteredListener<RemoteMasterUpdate>(RemoteMasterUpdate.class);
	
	private Camera camera;
	private ImmediateModeRenderer20 lineRenderer;
	
	private Rectangle viewport = new Rectangle(0,0,0,0);
	
	@SuppressWarnings("unchecked")
	public RemoteSlaveProcessor(Camera camera, ImmediateModeRenderer20 lineRenderer) {
		super(Aspect.getAspectForAll(RemoteSlave.class));
		this.camera = camera;
		this.lineRenderer = lineRenderer;
	}

	@Override
	protected void initialize() {
		ClientNetworkService.instance().addListener(listener);
	}
	
	@Override
	protected void begin() {
		MessageRemote<RemoteMasterUpdate> msg;
		while ((msg = listener.getNext()) != null) {
			int id = msg.getObject().id;
			// System.out.println("Received: "+updateQueue.peek().toString()+"."+updateQueue.size()+" left in queue");
			if (slaveRegister.safeGet(id) == null) {
				slaveRegister.set(id, world.createEntity().edit().add(new RemoteSlave(id)).getEntity());
			}
			updateRegister.set(id, msg.getObject());
		}
		float sendAreaSize = GameConstants.NET_SYNC_AREA;
		ClientNetworkService.instance().send(new ClientViewportUpdate(viewport.set(camera.position.x-sendAreaSize/2,camera.position.y-sendAreaSize/2,sendAreaSize,sendAreaSize)),false);

		lineRenderer.begin(camera.combined, GL20.GL_LINES);
		
		Color red = Color.RED;
		GameUtil.line(camera.position.x-sendAreaSize/2, camera.position.y-sendAreaSize/2, 0f/**/,/**/camera.position.x+sendAreaSize/2, camera.position.y-sendAreaSize/2, 0f/**/,/**/red.r, red.g, red.b, red.a, lineRenderer);
		GameUtil.line(camera.position.x+sendAreaSize/2, camera.position.y-sendAreaSize/2, 0f/**/,/**/camera.position.x+sendAreaSize/2, camera.position.y+sendAreaSize/2, 0f/**/,/**/red.r, red.g, red.b, red.a, lineRenderer);
		GameUtil.line(camera.position.x+sendAreaSize/2, camera.position.y+sendAreaSize/2, 0f/**/,/**/camera.position.x-sendAreaSize/2, camera.position.y+sendAreaSize/2, 0f/**/,/**/red.r, red.g, red.b, red.a, lineRenderer);
		GameUtil.line(camera.position.x-sendAreaSize/2, camera.position.y+sendAreaSize/2, 0f/**/,/**/camera.position.x-sendAreaSize/2, camera.position.y-sendAreaSize/2, 0f/**/,/**/red.r, red.g, red.b, red.a, lineRenderer);

		lineRenderer.end();
	}

	@Override
	protected void process(Entity e) {
		RemoteSlave rs = rsm.get(e);
		RemoteMasterUpdate rmu = updateRegister.get(rs.masterId);
		if (rmu != null) {
			if (rmu.fullUpdate) {
				
				Bag<Component> components = new Bag<Component>(8);
				e.getComponents(components);

				if (rmu.components != null) {
					if (rmu.components.length == 0) {
						GameUtil.stripComponents(e);
						e.edit().add(new Inactive());
					} else {
						EntityEdit ed = e.edit();
						for (int i = 0; i < rmu.components.length; i++) {
							ed.add((Component) rmu.components[i]);
						}
					}
				}
				INTERPOLATION_PERIOD_MEASURED = GameUtil.clamp(GameConstants.INTERPOLATION_PERIOD, rs.lastUpdateDelay, GameConstants.INTERPOLATION_PERIOD_MEASURED*2f);
				rs.lastUpdateDelay = 0f;
			}

			updateRegister.set(rmu.id, null);
		} else {
			if(rs.lastUpdateDelay>ENTITY_UPDATE_TIMEOUT || slaveRegister.get(rs.masterId) == null) {
				e.deleteFromWorld();
				slaveRegister.set(rs.masterId, null);
			}
			rs.lastUpdateDelay+=world.getDelta();
		}
	}
}
