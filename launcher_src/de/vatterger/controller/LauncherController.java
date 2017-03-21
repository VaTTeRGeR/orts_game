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
			if(new File("release/client.jar").exists()) {
				Runtime.getRuntime().exec("javaw -jar release/client.jar");
				System.out.println("Client.jar found in release folder");
			} else if (new File("client.jar").exists()) {
				Runtime.getRuntime().exec("javaw -jar client.jar");
				System.out.println("client.jar found in root folder");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
