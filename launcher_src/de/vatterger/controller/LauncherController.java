package de.vatterger.controller;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LauncherController {
	
	@FXML
	Button launchButton;
	
	@FXML
	public void launchGame(){
		launchButton.setDisable(true);
		System.out.println("Launching Game.");
		try {
			if(new File("target/release/orts-0.0.1.jar").exists()) {
				Runtime.getRuntime().exec("java -jar target/release/orts-0.0.1.jar");
				System.out.println("Client.jar found in release folder");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
