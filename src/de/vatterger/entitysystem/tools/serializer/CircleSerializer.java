package de.vatterger.entitysystem.tools.serializer;

import com.badlogic.gdx.math.Circle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CircleSerializer extends Serializer<Circle>{
	@Override
	public Circle read(Kryo kryo, Input in, Class<Circle> circleClass) {
		return new Circle(0f, 0f, in.readFloat());
	}
	
	@Override
	public void write(Kryo kryo, Output out, Circle circle) {
		out.writeFloat(circle.radius);
	}
}
