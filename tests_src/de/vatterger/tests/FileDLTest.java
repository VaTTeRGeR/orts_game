package de.vatterger.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;


public class FileDLTest {

	public static void main(String[] args) {
		try {
			URL website = new URL("http://speedtest.tokyo.linode.com/100MB-tokyo.bin");
			
			System.out.println("Size:"+getFileSize(website)/1000/1000+"mb");
			
			try (InputStream in = website.openStream()) {
				System.out.println("Begin downloading.");
				Files.copy(in, new File("tests/file.bin").toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Finished.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static long getFileSize(URL url) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			return conn.getContentLengthLong();
		} catch (IOException e) {
			return -1;
			// Or wrap into a (custom, if desired) RuntimeException so
			// exceptions are propagated.
			// throw new RuntimeException(e);
			// Alternatively you can just propagate IOException, but, urgh.
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}
