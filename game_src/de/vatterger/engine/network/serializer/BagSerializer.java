package de.vatterger.engine.network.serializer;


import com.artemis.utils.Bag;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static com.esotericsoftware.kryo.serializers.DefaultArraySerializers.*;

public class BagSerializer extends Serializer<Bag<?>>{

	ObjectArraySerializer oas = new ObjectArraySerializer(null, Object[].class);
	
	public BagSerializer() {
		oas.setElementsAreSameType(false);
		oas.setElementsCanBeNull(true);
	}
	
	@Override
	public Bag<?> read(Kryo kryo, Input in, Class<Bag<?>> bagClass) {
		Object[] content = kryo.readObject(in, Object[].class, oas);
		Bag<Object> bag = new Bag<Object>(content.length);
		for (int i = 0; i < content.length; i++) {
			bag.add(content[i]);
		}
		return bag;
	}
	
	@Override
	public void write(Kryo kryo, Output out, Bag<?> bag) {
		kryo.writeObject(out, bag.getData(), oas);
	}
}
