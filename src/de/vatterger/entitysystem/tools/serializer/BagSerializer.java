package de.vatterger.entitysystem.tools.serializer;

import com.artemis.utils.Bag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

@SuppressWarnings("rawtypes")
public class BagSerializer extends Serializer<Bag>{
	@Override
	public Bag<Object> read(Kryo kryo, Input in, Class<Bag> bagClass) {
		Object[] obj = kryo.readObject(in, Object[].class);
		Bag<Object> bag = new Bag<Object>(obj.length);
		for (int i = 0; i < obj.length; i++) {
			bag.set(i, obj[i]);
		}
		return bag;
	}
	
	@Override
	public void write(Kryo kryo, Output out, Bag bag) {
		kryo.writeObject(out, bag.getData());
	}
}
