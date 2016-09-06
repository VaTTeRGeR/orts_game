package de.vatterger.engine.handler.asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class AssetPathFinder {
	
	public static class AssetPath {
		final public String absolutePath;
		final public String relativePath;
		final public String name;
		private AssetPath(String ap, String rp, String n) {
			absolutePath = ap;
			relativePath = rp;
			name = n;
		}
	}
	
	public static AssetPath[] searchForAssets(String fileExtension) {
		return searchForAssets(fileExtension, "");
	}
	
	public static AssetPath[] searchForAssets(String fileExtension, String assetSubfolder) {
		
		LinkedList<AssetPath> result = new LinkedList<AssetPath>();
		
		assetSubfolder = assetSubfolder.replace("\\", "/");
		FileHandle fileHandle = Gdx.files.internal("assets/"+assetSubfolder);
		if(fileHandle.exists() && fileHandle.isDirectory()) {
			try {
				Files.walk(fileHandle.file().toPath()).filter(Files::isRegularFile).filter(isOfFileFormat(fileExtension)).forEach(new Consumer<Path>() {
					@Override
					public void accept(Path path) {
						result.add(getAssetPath(path));
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result.toArray(new AssetPath[result.size()]);
	}
	
	private static Predicate<Path> isOfFileFormat(String fileExtension) {
		final String fe;
		if(fileExtension.startsWith(".")){
			fe = fileExtension;
		} else {
			fe = "."+fileExtension;
		}
	    return p -> p.toString().endsWith(fe);
	}
	
	private static AssetPath getAssetPath(Path p) {
		String absPath = p.toAbsolutePath().toString();
		
		//Positions of a, b and c
		// "[path to asset folder]a[path within assets]b+1[name]c[extension]"

		absPath = absPath.replace('\\', '/');
		
		int a = absPath.lastIndexOf("assets");
		int b = absPath.lastIndexOf("/") + 1;
		int c = absPath.lastIndexOf(".");
		
		String name = absPath.substring(b, c);
		String relPath = absPath.substring(a);
		
		System.out.println(name + " - " + absPath + " - " + relPath);
		
		return new AssetPath(absPath, relPath, name);
	}
}
