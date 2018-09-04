package de.vatterger.engine.model;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

/** ModelCache tries to combine multiple render calls into a single render call by merging them where possible. Can be used for
 * multiple type of models (e.g. varying vertex attributes or materials), the ModelCache will combine where possible. Can be used
 * dynamically (e.g. every frame) or statically (e.g. to combine part of scenery). Be aware that any combined vertices are
 * directly transformed, therefore the resulting {@link Renderable#worldTransform} might not be suitable for sorting anymore (such
 * as the default sorter of ModelBatch does).
 * @author Xoppa */
public class ResponsiveModelCache implements Disposable, RenderableProvider {
	/** Allows to reuse one or more meshes while avoiding creating new objects. Depending on the implementation it might add memory
	 * optimizations as well. Call the {@link #obtain(VertexAttributes, int, int)} method to obtain a mesh which can at minimum the
	 * specified amount of vertices and indices. Call the {@link #flush()} method to flush the pool ant release all previously
	 * obtained meshes. */
	public interface MeshPool extends Disposable {
		/** Will try to reuse or, when not possible to reuse, optionally create a {@link Mesh} that meets the specified criteria.
		 * @param vertexAttributes the vertex attributes of the mesh to obtain
		 * @param vertexCount the minimum amount vertices the mesh should be able to store
		 * @param indexCount the minimum amount of indices the mesh should be able to store
		 * @return the obtained Mesh, or null when no mesh could be obtained. */
		Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount);

		/** Releases all previously obtained {@link Mesh}es using the the {@link #obtain(VertexAttributes, int, int)} method. */
		void flush ();
	}

	/** A basic {@link MeshPool} implementation that avoids creating new meshes at the cost of memory usage. It does this by making
	 * the mesh always the maximum (32k) size. Use this when for dynamic caching where you need to obtain meshes very frequently
	 * (typically every frame).
	 * @author Xoppa */
	public static class SimpleMeshPool implements MeshPool {
		// FIXME Make a better (preferable JNI) MeshPool implementation
		private Array<Mesh> freeMeshes = new Array<Mesh>();
		private Array<Mesh> usedMeshes = new Array<Mesh>();

		@Override
		public void flush () {
			freeMeshes.addAll(usedMeshes);
			usedMeshes.clear();
		}

		@Override
		public Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount) {
			for (int i = 0, n = freeMeshes.size; i < n; ++i) {
				final Mesh mesh = freeMeshes.get(i);
				if (mesh.getVertexAttributes().equals(vertexAttributes) && mesh.getMaxVertices() >= vertexCount
					&& mesh.getMaxIndices() >= indexCount) {
					freeMeshes.removeIndex(i);
					usedMeshes.add(mesh);
					return mesh;
				}
			}
			vertexCount = 1 + (int)Short.MAX_VALUE;
			indexCount = Math.max(1 + (int)Short.MAX_VALUE, 1 << (32 - Integer.numberOfLeadingZeros(indexCount - 1)));
			Mesh result = new Mesh(false, vertexCount, indexCount, vertexAttributes);
			usedMeshes.add(result);
			return result;
		}

		@Override
		public void dispose () {
			for (Mesh m : usedMeshes)
				m.dispose();
			usedMeshes.clear();
			for (Mesh m : freeMeshes)
				m.dispose();
			freeMeshes.clear();
		}
	}

	/** A tight {@link MeshPool} implementation, which is typically used for static meshes (create once, use many).
	 * @author Xoppa */
	public static class TightMeshPool implements MeshPool {
		private Array<Mesh> freeMeshes = new Array<Mesh>();
		private Array<Mesh> usedMeshes = new Array<Mesh>();

		@Override
		public void flush () {
			freeMeshes.addAll(usedMeshes);
			usedMeshes.clear();
		}

		@Override
		public Mesh obtain (VertexAttributes vertexAttributes, int vertexCount, int indexCount) {
			for (int i = 0, n = freeMeshes.size; i < n; ++i) {
				final Mesh mesh = freeMeshes.get(i);
				if (mesh.getVertexAttributes().equals(vertexAttributes) && mesh.getMaxVertices() == vertexCount
					&& mesh.getMaxIndices() == indexCount) {
					freeMeshes.removeIndex(i);
					usedMeshes.add(mesh);
					return mesh;
				}
			}
			Mesh result = new Mesh(true, vertexCount, indexCount, vertexAttributes);
			usedMeshes.add(result);
			return result;
		}

		@Override
		public void dispose () {
			for (Mesh m : usedMeshes)
				m.dispose();
			usedMeshes.clear();
			for (Mesh m : freeMeshes)
				m.dispose();
			freeMeshes.clear();
		}
	}

	/** A {@link RenderableSorter} that sorts by vertex attributes, material attributes and primitive types (in that order), so that
	 * meshes can be easily merged.
	 * @author Xoppa */
	public static class Sorter implements RenderableSorter, Comparator<Renderable> {
		@Override
		public void sort (Camera camera, Array<Renderable> renderables) {
			renderables.sort(this);
		}

		@Override
		public int compare (Renderable arg0, Renderable arg1) {
			final VertexAttributes va0 = arg0.meshPart.mesh.getVertexAttributes();
			final VertexAttributes va1 = arg1.meshPart.mesh.getVertexAttributes();
			final int vc = va0.compareTo(va1);
			if (vc == 0) {
				final int mc = arg0.material.compareTo(arg1.material);
				if (mc == 0) {
					return arg0.meshPart.primitiveType - arg1.meshPart.primitiveType;
				}
				return mc;
			}
			return vc;
		}
	}

	private Array<Renderable> renderables = new Array<Renderable>();
	private FlushablePool<Renderable> renderablesPool = new FlushablePool<Renderable>() {
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}
	};
	private FlushablePool<MeshPart> meshPartPool = new FlushablePool<MeshPart>() {
		@Override
		protected MeshPart newObject () {
			return new MeshPart();
		}
	};

	private Array<Renderable> items = new Array<Renderable>();
	private Array<Renderable> tmp = new Array<Renderable>();

	private MeshBuilder meshBuilder;
	private boolean building;
	private RenderableSorter sorter;
	private MeshPool meshPool;
	private Camera camera;

	/** Create a ModelCache using the default {@link Sorter} and the {@link SimpleMeshPool} implementation. This might not be the
	 * most optimal implementation for you use-case, but should be good to start with. */
	public ResponsiveModelCache () {
		this(new Sorter(), new SimpleMeshPool());
	}

	/** Create a ModelCache using the specified {@link RenderableSorter} and {@link MeshPool} implementation. The
	 * {@link RenderableSorter} implementation will be called with the camera specified in {@link #begin(Camera)}. By default this
	 * will be null. The sorter is important for optimizing the cache. For the best result, make sure that renderables that can be
	 * merged are next to each other. */
	public ResponsiveModelCache (RenderableSorter sorter, MeshPool meshPool) {
		this.sorter = sorter;
		this.meshPool = meshPool;
		meshBuilder = new MeshBuilder();
	}

	/** Begin creating the cache, must be followed by a call to {@link #end()}, in between these calls one or more calls to one of
	 * the add(...) methods can be made. Calling this method will clear the cache and prepare it for creating a new cache. The
	 * cache is not valid until the call to {@link #end()} is made. Use one of the add methods (e.g. {@link #add(Renderable)} or
	 * {@link #add(RenderableProvider)}) to add renderables to the cache. */
	public void begin () {
		begin(null);
	}

	/** Begin creating the cache, must be followed by a call to {@link #end()}, in between these calls one or more calls to one of
	 * the add(...) methods can be made. Calling this method will clear the cache and prepare it for creating a new cache. The
	 * cache is not valid until the call to {@link #end()} is made. Use one of the add methods (e.g. {@link #add(Renderable)} or
	 * {@link #add(RenderableProvider)}) to add renderables to the cache.
	 * @param camera The {@link Camera} that will passed to the {@link RenderableSorter} */
	public void begin (Camera camera) {
		if (building) throw new GdxRuntimeException("Call end() after calling begin()");
		building = true;

		this.camera = camera;
		renderablesPool.flush();
		renderables.clear();
		items.clear();
		meshPartPool.flush();
		meshPool.flush();
	}

	private Renderable obtainRenderable (Material material, int primitiveType) {
		Renderable result = renderablesPool.obtain();
		result.bones = null;
		result.environment = null;
		result.material = material;
		result.meshPart.mesh = null;
		result.meshPart.offset = 0;
		result.meshPart.size = 0;
		result.meshPart.primitiveType = primitiveType;
		result.meshPart.center.set(0, 0, 0);
		result.meshPart.halfExtents.set(0, 0, 0);
		result.meshPart.radius = -1f;
		result.shader = null;
		result.userData = null;
		result.worldTransform.idt();
		return result;
	}
	
	private int i,n;
	private int offset;
	private int primitiveType;
	private VertexAttributes vertexAttributes;
	private Material material;
	private MeshPart part;
	
	public void build_begin () {
		if (!building) throw new GdxRuntimeException("Call begin() prior to calling end()");

		if (items.size == 0) return;
		sorter.sort(camera, items);

		final Renderable first = items.get(0);
		vertexAttributes = first.meshPart.mesh.getVertexAttributes();
		material = first.material;
		primitiveType = first.meshPart.primitiveType;
		offset = renderables.size;

		meshBuilder.begin(vertexAttributes);
		part = meshBuilder.part("", primitiveType, meshPartPool.obtain());
		renderables.add(obtainRenderable(material, primitiveType));
		i = 0;
	}

	private static float getElapsedSeconds(long t_begin) {
		long d = System.nanoTime() - t_begin;
		return (float)(((double)d)/1000000000d);
	}
	
	/**
	 * Continues building the cache for maxTime seconds and returns true once finished, you must call {@link build_begin} first before calling this method.
	 *
	 * @param maxTime The minimum time after which building is paused again, best effort is made to return quickly after the time has elapsed.
	 * @return True if the build has finished building the cache, false if the build is still in progress.
	*/
	public boolean build_update (float maxTime) {
		long t_begin = System.nanoTime();
		
		for (n = items.size; i < n && getElapsedSeconds(t_begin) < maxTime; ++i) {
			final Renderable renderable = items.get(i);
			final VertexAttributes va = renderable.meshPart.mesh.getVertexAttributes();
			final Material mat = renderable.material;
			final int pt = renderable.meshPart.primitiveType;

			final boolean sameMesh = va.equals(vertexAttributes)
				&& renderable.meshPart.size + meshBuilder.getNumVertices() < Short.MAX_VALUE; // comparing indices and vertices...
			final boolean samePart = sameMesh && pt == primitiveType && mat.same(material, true);

			if (!samePart) {
				if (!sameMesh) {
					final Mesh mesh = meshBuilder.end(meshPool.obtain(vertexAttributes, meshBuilder.getNumVertices(), meshBuilder.getNumIndices()));
					while (offset < renderables.size)
						renderables.get(offset++).meshPart.mesh = mesh;
					meshBuilder.begin(vertexAttributes = va);
				}

				final MeshPart newPart = meshBuilder.part("", pt, meshPartPool.obtain());
				final Renderable previous = renderables.get(renderables.size - 1);
				previous.meshPart.offset = part.offset;
				previous.meshPart.size = part.size;
				part = newPart;

				renderables.add(obtainRenderable(material = mat, primitiveType = pt));
			}
			meshBuilder.setVertexTransform(renderable.worldTransform);
			meshBuilder.addMesh(renderable.meshPart.mesh, renderable.meshPart.offset, renderable.meshPart.size);
		}
		
		if(i < n) {
			return false;
		} else {
			build_finish();
			return true;
		}
	}
	
	/** Finishes the cache building, htis is called automatically by */
	private void build_finish() {	
		final Mesh mesh = meshBuilder.end(meshPool.obtain(vertexAttributes, meshBuilder.getNumVertices(), meshBuilder.getNumIndices()));
		while (offset < renderables.size)
			renderables.get(offset++).meshPart.mesh = mesh;

		final Renderable previous = renderables.get(renderables.size - 1);
		previous.meshPart.offset = part.offset;
		previous.meshPart.size = part.size;
		building = false;
	}

	/** Adds the specified {@link Renderable} to the cache. Must be called in between a call to {@link #begin()} and {@link #end()}.
	 * All member objects might (depending on possibilities) be used by reference and should not change while the cache is used. If
	 * the {@link Renderable#bones} member is not null then skinning is assumed and the renderable will be added as-is, by
	 * reference. Otherwise the renderable will be merged with other renderables as much as possible, depending on the
	 * {@link Mesh#getVertexAttributes()}, {@link Renderable#material} and primitiveType (in that order). The
	 * {@link Renderable#environment}, {@link Renderable#shader} and {@link Renderable#userData} values (if any) are removed.
	 * @param renderable The {@link Renderable} to add, should not change while the cache is needed. */
	public void add (Renderable renderable) {
		if (!building) throw new GdxRuntimeException("Can only add items to the ModelCache in between .begin() and .end()");
		if (renderable.bones == null)
			items.add(renderable);
		else
			renderables.add(renderable);
	}

	/** Adds the specified {@link RenderableProvider} to the cache, see {@link #add(Renderable)}. */
	public void add (final RenderableProvider renderableProvider) {
		renderableProvider.getRenderables(tmp, renderablesPool);
		for (int i = 0, n = tmp.size; i < n; ++i)
			add(tmp.get(i));
		tmp.clear();
	}

	/** Adds the specified {@link RenderableProvider}s to the cache, see {@link #add(Renderable)}. */
	public <T extends RenderableProvider> void add (final Iterable<T> renderableProviders) {
		for (final RenderableProvider renderableProvider : renderableProviders)
			add(renderableProvider);
	}

	@Override
	public void getRenderables (Array<Renderable> renderables, Pool<Renderable> pool) {
		if (building) throw new GdxRuntimeException("Cannot render a ModelCache in between .begin() and .end()");
		for (Renderable r : this.renderables) {
			r.shader = null;
			r.environment = null;
		}
		renderables.addAll(this.renderables);
	}

	@Override
	public void dispose () {
		if (building) throw new GdxRuntimeException("Cannot dispose a ModelCache in between .begin() and .end()");
		meshPool.dispose();
	}
}
