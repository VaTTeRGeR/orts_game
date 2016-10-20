package de.vatterger.tests;

import java.awt.AWTException;
import java.awt.Robot;

public class RobotTest {
	public static void main(String[] args) {
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		for(int i = 1; i < 500;i++) {
			robot.mouseMove(5*i%1600, 50*i%1000);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
