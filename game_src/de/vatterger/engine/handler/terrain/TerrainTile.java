
package de.vatterger.engine.handler.terrain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import de.vatterger.engine.util.OpenSimplex2S;

public class TerrainTile {

	private static final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, false);
	private static final Inflater inflater = new Inflater();
	private static final ThreadLocal<byte[]> compressionBuffers = ThreadLocal.withInitial( () -> new byte[128 * 1024]);

	@SuppressWarnings("unused") private final TerrainHandle handle;

	private final int tileIndex;

	private final Path tilePath;

	private final float tileBorderX1, tileBorderY1;
	private final float tileBorderX2, tileBorderY2;

	private final int cellsX, cellsY, cellsTotal;
	private final float cellSizeX, cellSizeY;

	private final int numLayerBytes;
	
	// enabledLayerBytes[numLayerBytes],height[N],flag[N],layer0[countSetBits(enabledLayerBytes)]
	// Each bit in enabledLayerBytes stands for one layer, 0=>empty, 1=>used
	private byte packedData[];
	private int heightOffset;
	private int flagOffset;
	private int layersOffset;

	private volatile boolean modified = false;

	private final byte[] heights;
	private final byte[] flags;
	private final byte[][] layers;

	protected TerrainTile (int tileIndex, TerrainHandle handle) {

		this.handle = handle;

		this.tileIndex = tileIndex;

		this.tilePath = Paths.get(handle.mapFolderString, tileIndex + ".mbin");

		this.tileBorderX1 = handle.xFromTileIndex(tileIndex);
		this.tileBorderY1 = handle.yFromTileIndex(tileIndex);

		this.tileBorderX2 = tileBorderX1 + handle.tileSizeX;
		this.tileBorderY2 = tileBorderY1 + handle.tileSizeY;

		this.cellsX = handle.numCellsX;
		this.cellsY = handle.numCellsY;
		this.cellsTotal = handle.numCellsX * handle.numCellsY;

		this.cellSizeX = handle.cellSizeX;
		this.cellSizeY = handle.cellSizeY;

		this.numLayerBytes = ( handle.numTextures + 7 ) / 8;
		
		// datablock offsets in packed representation
		this.heightOffset = numLayerBytes;
		this.flagOffset = heightOffset + cellsTotal;
		this.layersOffset = flagOffset + cellsTotal;
		
		final boolean existsOnDisk = Files.exists(tilePath);

		try {

			if (existsOnDisk) {
				byte[] compressedData = Files.readAllBytes(tilePath);
				packedData = inflate(compressedData);
				clearModified();
			} else {
				packedData = new byte[numLayerBytes + 2 * cellsTotal];
				setModified();
			}

		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read TerrainTile: " + e.getMessage());
		} catch (DataFormatException e) {
			throw new IllegalArgumentException("Cannot decompress TerrainTile: " + e.getMessage());
		}

		heights = Arrays.copyOfRange(packedData, heightOffset, heightOffset + cellsTotal);
		flags = Arrays.copyOfRange(packedData, flagOffset, flagOffset + cellsTotal);
		
		layers = new byte[handle.numTextures][];

		BitSet enabledLayersBitset = BitSet.valueOf(Arrays.copyOf(packedData, numLayerBytes));
		
		for (int i = 0; i < handle.numTextures; i++) {
			if(enabledLayersBitset.get(i)) {
				layers[i] = Arrays.copyOfRange(packedData, layerOffset(i), layerOffset(i) + cellsTotal);
			}
		}

		if (!existsOnDisk) {
			
			enableTextureLayer(0);
			Arrays.fill(layers[0], (byte)255);
			
			enableTextureLayer(1);
			OpenSimplex2S noise = new OpenSimplex2S(0);
			for (int i = 0; i < layers[1].length; i++) {

				float posX = tileBorderX1 + (tileBorderX2 - tileBorderX1) * ((float)(i % cellsX)) / (float)(cellsX - 1);
				float posY = tileBorderY2 - (tileBorderY2 - tileBorderY1) * ((float)(i / cellsX)) / (float)(cellsY - 1);

				double nA = ((noise.noise2(posX / 200d, posY / 200d) + 1d) * 0.5d * 255d);
				double nB = ((noise.noise2(posX / 70d, posY / 70d) + 1d) * 0.5d * 255d);
				double nC = ((noise.noise2(posX / 50d, posY / 50d) + 1d) * 0.5d * 255d);
				double nD = ((noise.noise2(posX / 20d, posY / 20d) + 1d) * 0.5d * 255d);

				layers[1][i] = (byte)((nA + nB + nC + nD) / 4d);
			}
		}
	}

	/** @param layer The layer-id 0...numLayers-1
	 * @return byte-offset of layer-data in packed representation. */
	private int layerOffset (int layerIndex) {
		
		int offset = layersOffset;
		
		for (int i = 0; i < layerIndex; i++) {
			if(isLayerEnabled(i)) {
				offset += cellsTotal;
			}
		}
		
		return offset;
	}
	
	private int countEnabledLayers() {
		
		int enabledLayers = 0;
		
		for (int layerIndex = 0; layerIndex < layers.length; layerIndex++) {
			if(isLayerEnabled(layerIndex)) {
				enabledLayers++;
			}
		}
		
		return enabledLayers;
	}
	
	public int getTileIndex () {
		return tileIndex;
	}

	public byte[] getHeight () {
		return heights;
	}

	public float getBorderX1 () {
		return tileBorderX1;
	}

	public float getBorderY1 () {
		return tileBorderY1;
	}

	public float getBorderX2 () {
		return tileBorderX2;
	}

	public float getBorderY2 () {
		return tileBorderY2;
	}

	public int getCellsX () {
		return cellsX;
	}

	public int getCellsY () {
		return cellsY;
	}

	public float getCellSizeX () {
		return cellSizeX;
	}

	public float getCellSizeY () {
		return cellSizeY;
	}

	public byte[] getFlags () {
		return flags;
	}

	public byte[] getLayer (int layerIndex) {

		// LayerIndex out of bounds
		if (layerIndex < 0 || layerIndex >= layers.length) {
			return null;
		}

		return layers[layerIndex];
	}

	public void enableTextureLayer (int layerIndex) {

		// LayerIndex out of bounds
		if(layerIndex < 0 || layerIndex >= layers.length) return;
		
		// Layer already present
		if(layers[layerIndex] != null) return;
		
		setModified();

		layers[layerIndex] = new byte[cellsTotal];
	}

	public boolean isLayerEnabled (int layerIndex) {

		// LayerIndex out of bounds
		if(layerIndex < 0 || layerIndex >= layers.length) return false;
		
		return layers[layerIndex] != null;
	}

	/** Call this after modifying the tile data, this will make sure changes are saved. */
	public void setModified () {
		modified = true;
	}

	private void clearModified () {
		modified = false;
	}

	public boolean isModified () {
		return modified;
	}

	protected void writeToDisk () {

		if (isModified()) {
			rebuildPackedData();
		}

		try {
			Files.write(tilePath, deflate(packedData));
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't save TerrainTile: " + tilePath);
		}
	}

	public void rebuildPackedData () {

		clearModified();

		final int requiredSize = numLayerBytes + ( 2 + countEnabledLayers() ) * cellsTotal;

		if (packedData.length != requiredSize) {
			packedData = new byte[requiredSize];
		}

		heightOffset = numLayerBytes;

		flagOffset = heightOffset + cellsTotal;

		BitSet enabledLayersBitset = new BitSet(numLayerBytes * 8);
		for (int i = 0; i < layers.length; i++) {
			enabledLayersBitset.set(i, isLayerEnabled(i));
		}
		
		System.arraycopy(enabledLayersBitset.toByteArray(), 0, packedData, 0, numLayerBytes);
		System.arraycopy(heights, 0, packedData, heightOffset, cellsTotal);
		System.arraycopy(flags, 0, packedData, flagOffset, cellsTotal);

		for (int i = 0; i < layers.length; i++) {
			if(isLayerEnabled(i)) {
				System.arraycopy(layers[i], 0, packedData, layerOffset(i), cellsTotal);
			}
		}
	}

	/** Compress input data using a statically initialized buffer, use with a signle thread per JVM instance only! */
	private static byte[] deflate (byte[] data) throws IOException {

		//Profiler p = new Profiler("Compressing " + data.length + " bytes", TimeUnit.MICROSECONDS);

		byte[] buffer = compressionBuffers.get();

		deflater.setInput(data);
		deflater.finish();
		int size = deflater.deflate(buffer);
		deflater.reset();

		//p.log();

		return Arrays.copyOf(buffer, size);
	}

	/** Decompress input data using a statically initialized buffer, use with a signle thread per JVM instance only! */
	private static byte[] inflate (byte[] data) throws IOException, DataFormatException {

		//Profiler p = new Profiler("Decompressing " + data.length + " bytes", TimeUnit.MICROSECONDS);

		byte[] buffer = compressionBuffers.get();

		inflater.setInput(data);
		int size = inflater.inflate(buffer);
		inflater.reset();

		//p.log();

		return Arrays.copyOf(buffer, size);
	}

	@Override
	public int hashCode () {
		return tileIndex;
	}

	@Override
	public boolean equals (Object obj) {
		if (obj instanceof TerrainTile) {
			return ((TerrainTile)obj).tileIndex == tileIndex;
		} else {
			return false;
		}
	}
}
