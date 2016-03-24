package com.artemis.io;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.EntitySubscription;
import com.artemis.annotations.Transient;
import com.artemis.utils.IntBag;
import com.artemis.utils.reflect.ClassReflection;

import java.util.*;

/**
 * <p>
 * Represents a set of Entities ready to be serialized, or a set of Entities that was just
 * deserialized (and therefore ready to use in your game). This class can be extended if additional
 * data requires persisting. All instance fields in this class - or its children - are persisted.
 * </p>
 *
 * <p>The default de/serializer backend provided is
 * {@code JsonArtemisSerializer}. A kryo backend is planned for a later release.
 * A backend knows how to serialize entities and metadata, but little else.
 * If extending this class, custom per-type serializers can be defined - these
 * serializers are convenient to have, but normal POJO classes with some
 * custom logic works too.</p>
 *
 * <p>The typical custom serializer works on type, e.g. a <code>GameStateManager</code>
 * contains additional data not available to components directly. A serializer would
 * be registered to only interact with that class; during loading and saving, the
 * serializer interacts directly with the manager and reads/writes the data as needed.</p>
 *
 * <p><b>Nota Bene:</b> PackedComponent types are not yet supported.</p>
 *
 * @see JsonArtemisSerializer
 * @see EntityReference
 */
public class SaveFileFormat {

	// all non-transient fields are automatically serialized
	public Metadata metadata;
	public ComponentIdentifiers componentIdentifiers;
	public IntBag entities;
	public ArchetypeMapper archetypes;

	transient SerializationKeyTracker tracker = new SerializationKeyTracker();

	public SaveFileFormat(IntBag entities) {
		this.entities = (entities != null) ? entities : new IntBag();
		componentIdentifiers = new ComponentIdentifiers();
		metadata = new Metadata();
		metadata.version = Metadata.LATEST;
	}


	public SaveFileFormat(EntitySubscription es) {
		this(es.getEntities());
	}

	public SaveFileFormat() {
		this((IntBag)null);
	}

	public final Entity get(String key) {
		return tracker.get(key);
	}

	public final boolean has(String key) {
		return tracker.get(key) != null;
	}

	public final Set<String> keys() {
		return tracker.keys();
	}

	public static class Metadata {
		public static final int VERSION_1 = 1;
		public static final int LATEST = VERSION_1;

		public int version;
	}

	public static class ComponentIdentifiers {
		public Map<Class<? extends Component>, String> typeToName =
			new IdentityHashMap<Class<? extends Component>, String>();
		public Map<String, Class<? extends Component>> nameToType =
			new HashMap<String, Class<? extends Component>>();

		transient Set<Class<? extends Component>> transientComponents =
			new HashSet<Class<? extends Component>>();

		void build() {
			Iterator<Map.Entry<Class<? extends Component>, String>> it = typeToName.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Class<? extends Component>, String> entry = it.next();
				Class<? extends Component> c = entry.getKey();
				if (ClassReflection.getDeclaredAnnotation(c, Transient.class) == null) {
					nameToType.put(entry.getValue(), c);
				} else {
					transientComponents.add(c);
					it.remove();
				}
			}
		}

		boolean isTransient(Class<? extends Component> c) {
			return transientComponents.contains(c);
		}
	}
}
