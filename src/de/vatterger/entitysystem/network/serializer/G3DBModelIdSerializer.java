package de.vatterger.entitysystem.network.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.vatterger.entitysystem.components.shared.G3DBModelId;

public class G3DBModelIdSerializer extends Serializer<G3DBModelId>{
	@Override
	public G3DBModelId read(Kryo kryo, Input in, Class<G3DBModelId> oclass) {
		return new G3DBModelId(in.readInt(true));
	}
	
	@Override
	public void write(Kryo kryo, Output out, G3DBModelId gmi) {
		out.writeInt(gmi.id, true);
	}
}
