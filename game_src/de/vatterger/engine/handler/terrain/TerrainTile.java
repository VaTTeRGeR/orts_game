package de.vatterger.engine.handler.terrain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ByteArray;

public class TerrainTile {
	
	@SuppressWarnings("unused")
	private final TerrainHandle handle;
	
	private final int tileIndex;
	
	private final Path tilePath;
	
	private final float tileBorderX1, tileBorderY1;
	private final float tileBorderX2, tileBorderY2;
	
	private final int cellsX, cellsY, cellsTotal;
	private final float cellSizeX,cellSizeY;
	
	private byte data[];
	private int	textureOffset;
	private int	heightOffset;
	private int	flagOffset;
	
	private boolean modified = false;
	
	private final ByteArray	textures;
	private final byte[]		heights;
	private final byte[]		flags;
	private final Array<byte[]> layers;
	
	protected TerrainTile (int tileIndex, TerrainHandle handle) {
		
		this.handle = handle;
		
		this.tileIndex = tileIndex;
		
		this.tilePath = Paths.get(handle.mapFolderString, tileIndex + ".bin");
		
		this.tileBorderX1 = handle.xFromTileIndex(tileIndex);
		this.tileBorderY1 = handle.yFromTileIndex(tileIndex);
		
		this.tileBorderX2 = tileBorderX1 + handle.tileSizeX;
		this.tileBorderY2 = tileBorderY1 + handle.tileSizeY;
		
		this.cellsX = handle.numCellsX;
		this.cellsY = handle.numCellsY;
		this.cellsTotal = handle.numCellsX * handle.numCellsY;
		
		this.cellSizeX = handle.cellSizeX;
		this.cellSizeY = handle.cellSizeY;
		
		final boolean existsOnDisk = Files.exists(tilePath);
		
		try {
			
			if(existsOnDisk) {
				data = Files.readAllBytes(tilePath);
				
				clearModified();
				
			} else {
				//numTextures, textureId0,height[N],flag[N],layer0[N]
				data = new byte[1 + 2 + cellsX * cellsY * 4];
				
				// Number of Textures
				data[0] = 2;
				
				// Texture ids
				data[1] = 0;
				data[2] = 1;
				
				setModified();
			}
			
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot read TerrainTile: " + e.getMessage());
		}
		
		final int textureSize = data[0];
		
		textureOffset = 1;

		heightOffset = textureOffset + textureSize;
		
		flagOffset = heightOffset + cellsTotal;
		
		textures = new ByteArray(true, data, textureOffset, textureSize);
		
		heights = Arrays.copyOfRange(data, heightOffset, heightOffset + cellsTotal);
		
		flags = Arrays.copyOfRange(data, flagOffset, flagOffset + cellsTotal);
		
		layers = new Array<byte[]>(true, textureSize);
		
		for (int i = 0; i < textureSize; i++) {
			layers.add(Arrays.copyOfRange(data, layerOffset(i), layerOffset(i) + cellsTotal));
		}
		
		if(!existsOnDisk) {
			
			byte[] layer0 = layers.get(0);
			byte[] layer1 = layers.get(1);
			
			Arrays.fill(layer0, (byte)255);
			
			for (int i = 0; i < layer1.length; i++) {
				
				if(MathUtils.randomBoolean(0.25f)) {
					layer1[i] = (byte)(MathUtils.random(0, 255));
				}
			}
		}
	}

	private int layerOffset(int layer) {
		return flagOffset + cellsTotal + layer * cellsTotal;
	}
	
	public int getTileIndex () {
		return tileIndex;
	}

	public byte[] getHeight() {
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

	public byte[] getFlags() {
		return flags;
	}
	
	public byte[] getLayer(int layerIndex) {
		
		if(layerIndex < 0 || layerIndex >= layers.size) {
			return null;
		}
		
		return layers.get(layerIndex);
	}
	
	public int addTexture(int textureId) {

		setModified();
		
		textures.add((byte)textureId);
		layers.add(new byte[cellsTotal]);
		
		return (textures.size - 1);
	}
	
	public boolean setTexture(int layer, int textureId) {

		if(layer < 0 || layer >= textures.size) {
			return false;
		}
		
		setModified();
		
		textures.set(layer, (byte)textureId);
		
		return true;
	}
	
	public byte[] getTextures() {
		
		textures.shrink();
		
		return textures.items;
	}
	
	public void setModified() {
		modified = true;
	}
	
	private void clearModified() {
		modified = false;
	}
	
	public boolean isModified () {
		return modified;
	}

	protected void writeToDisk () {
		try {
			Files.write(tilePath, data);
		} catch (IOException e) {
			throw new IllegalStateException("Couldn't save TerrainTile: " + tilePath);
		}
	}
	
	public void rebuildData () {
		
		clearModified();
		
		final int textureSize = textures.size;
		
		final int requiredSize = 1 + textureSize + 2 * cellsTotal + cellsTotal * textureSize;
		
		if(data.length != requiredSize) {
			data = new byte[requiredSize];
		}
		
		textureOffset = 1;

		heightOffset = textureOffset + textureSize;
		
		flagOffset = heightOffset + cellsTotal;
		
		data[0] = (byte)textureSize;
		System.arraycopy(textures.items, 0, data, textureOffset, textureSize);
		System.arraycopy(heights, 0, data, heightOffset, cellsTotal);
		System.arraycopy(flags, 0, data, flagOffset, cellsTotal);
		for (int i = 0; i < layers.size; i++) {
			System.arraycopy(layers.get(i), 0, data, layerOffset(i), cellsTotal);
		}
	}
	
	@Override
	public int hashCode () {
		return tileIndex;
	}
	
	@Override
	public boolean equals (Object obj) {
		if(obj instanceof TerrainTile) {
			return ((TerrainTile)obj).tileIndex == tileIndex;
		} else {
			return false;
		}
	}
}
