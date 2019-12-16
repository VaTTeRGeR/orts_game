package de.vatterger.engine.handler.asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

		@Override
		public String toString() {
			return new StringBuilder(256).append(name).append(" - rel: '").append(relativePath).append("' - abs: '").append(absolutePath).append("'").toString();
		}
	}
	
	public static AssetPath[] searchForAssets(String fileExtension) {
		return searchForAssets(fileExtension, "assets");
	}
	
	public static AssetPath[] searchForAssets(String fileExtension, String folder) {
		
		LinkedList<AssetPath> result = new LinkedList<AssetPath>();
		
		folder = folder.replace("\\", "/");
		
		final String workingDirectory = Path.of("").toAbsolutePath().toString().replace("\\", "/");

		final Path searchDirectoryPath = Path.of(folder);

		if(Files.exists(searchDirectoryPath) && Files.isDirectory(searchDirectoryPath)) {
			try {
				Files.walk(searchDirectoryPath).filter(Files::isRegularFile).filter(isOfFileFormat(fileExtension)).forEach(new Consumer<Path>() {
					@Override
					public void accept(Path path) {
						result.add(getAssetPath(path, workingDirectory));
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
	
	private static AssetPath getAssetPath(Path p, String workingDirectory) {
		
		String absPath = p.toAbsolutePath().toString();
		
		//Positions of a, b and c
		// "[path to asset folder]a[path within assets]b+1[name]c[extension]"

		absPath = absPath.replace('\\', '/');
		
		//int a = absPath.lastIndexOf(/*"assets"*/);
		int b = absPath.lastIndexOf("/") + 1;
		int c = absPath.lastIndexOf(".");
		
		String name = absPath.substring(b, c);
		String relPath = absPath.replace(workingDirectory, "");
		
		if(relPath.startsWith("/") && relPath.length() >= 2) {
			relPath = relPath.substring(1);
		}
		
		return new AssetPath(absPath, relPath, name);
	}
}
