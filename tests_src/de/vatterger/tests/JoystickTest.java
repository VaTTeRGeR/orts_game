package de.vatterger.tests;

import java.util.Arrays;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;

public class JoystickTest {

	public static void main(String[] args) {

		HidServices hidServices = HidManager.getHidServices();

		// Provide a list of attached devices
		for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
			System.out.println(hidDevice);
			if(hidDevice.getUsage() == 0x04) {
				System.out.println("Found joystick: " + hidDevice.getProduct() + " from " + hidDevice.getManufacturer());
				
				hidDevice.open();
				
				byte[] dataFeatures = new byte[2048];
				hidDevice.getFeatureReport(dataFeatures, (byte)127);
				System.out.println(Arrays.toString(dataFeatures));
				
				System.out.println("Getting Feature Report!");
				
				for (int i = 0; i < 10000; i++) {
					byte[] data = new byte[16];
					
					hidDevice.read(data);
					
					int[] dataUnsigned = new int[16];
					
					for (int j = 0; j < data.length; j++) {
						dataUnsigned[j] = (data[j] & 0xff);
						
						int d = dataUnsigned[j];
						if(d<10) {
							System.out.print("00");
						}else if(d<100) {
							System.out.print("0");
						}
						System.out.print(d);
						System.out.print("|");
					}
					System.out.println();
					
					System.out.println("X: " + ((dataUnsigned[3]) + (dataUnsigned[4]<<8)));
					System.out.println("Y: " + ((dataUnsigned[5]) + (dataUnsigned[6]<<8)));

					
				}
				
				hidDevice.close();
			}
		}
		
		hidServices.shutdown();
	}
}
