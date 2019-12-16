package de.vatterger.tests;

import de.vatterger.engine.handler.asset.AssetPathFinder;
import de.vatterger.engine.handler.asset.AssetPathFinder.AssetPath;

public class AssetPathFinderTest {

	public static void main(String[] args) {

		AssetPath[] paths = AssetPathFinder.searchForAssets("png","assets");
		
		for(AssetPath path : paths) {
			System.out.println(path);
		}
	}
}
