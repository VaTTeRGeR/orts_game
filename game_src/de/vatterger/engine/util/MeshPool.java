package de.vatterger.engine.util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongArray;

public class MeshPool implements Disposable {

	private final long trimTimeoutMillis;
	
	private final Array<Mesh> meshes = new Array<Mesh>(true, 256, Mesh.class);
	private final LongArray arrivalTimes = new LongArray(true, 256);
	
	// 5 minutes default timeout
	public MeshPool () {
		this(5*60*1000);
	}
	
	public MeshPool (long trimTimeoutMillis) {
		this.trimTimeoutMillis = trimTimeoutMillis;
	}
	
	public void trim() {
		
		if(trimTimeoutMillis <= 0) {
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		
		for (int i = 0; i < arrivalTimes.size;) {
			
			if(currentTime > arrivalTimes.items[i] + trimTimeoutMillis) {
				
				arrivalTimes.removeIndex(i);
				meshes.removeIndex(i).dispose();
				
			} else {
				i++;
			}
		}
	}
	
	private Mesh findReusableMesh(int verticesSize, int indicesSize) {
		
		Mesh selected = null;
		
		for (int i = 0; i < meshes.size; i++) {
			
			Mesh mesh = meshes.items[i];
			
			if(mesh.getMaxVertices() >= verticesSize && mesh.getMaxIndices() >= indicesSize) {
				
				meshes.removeIndex(i);
				arrivalTimes.removeIndex(i);
				
				selected = mesh;
				
				break;
			}
		}
		
		return selected;
	}
	
	public Mesh getMesh(int verticesSize, int indicesSize, VertexAttributes attributes) {
		
		Mesh selected = findReusableMesh(verticesSize, indicesSize);
		
		if(selected == null) {
			selected = new Mesh(false, false, verticesSize, indicesSize, attributes);
		}
		
		return selected;
	}
	
	public void free(Mesh mesh) {
		meshes.add(mesh);
		arrivalTimes.add(System.currentTimeMillis());
	}
	
	@Override
	public void dispose() {
		
		for (Mesh mesh : meshes) {
			mesh.dispose();
		}
		
		meshes.clear();
		arrivalTimes.clear();
	}
}