package de.vatterger.tests;

import java.util.Arrays;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoTest {

	public static void main(String[] args) {
		Kryo kryo = new Kryo();
		FASerializer fas = new FASerializer();

		byte[] buffer = new byte[12];
		System.out.println("Buffer: "+Arrays.toString(buffer));

		Output out = new Output();
		out.setBuffer(buffer, 12);
		fas.write(kryo, out, new float[]{1f,2f,3f});
		
		System.out.println("Buffer: "+Arrays.toString(buffer));
		System.out.println("Deserialized: "+Arrays.toString(fas.read(kryo, new Input(buffer), float[].class)));
	}
	
	private static class FASerializer extends Serializer<float[]> {
		@Override
		public float[] read(Kryo kryo, Input input, Class<float[]> type) {
			return input.readFloats(3);
		}
		
		@Override
		public void write(Kryo kryo, Output output, float[] object) {
			output.writeFloats(object);
		}
	}
}
