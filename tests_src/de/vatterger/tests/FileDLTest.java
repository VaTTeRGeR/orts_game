package de.vatterger.tests;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class FileDLTest {

	public static void main(String[] args) {
		try {
			URL website = new URL("http://speedtest.tokyo.linode.com/100MB-tokyo.bin");
			try (InputStream in = website.openStream()) {
				Files.copy(in, new File("tests/file.bin").toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
