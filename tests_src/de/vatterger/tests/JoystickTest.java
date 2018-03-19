package de.vatterger.tests;

import java.util.Arrays;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesListener;
import org.hid4java.event.HidServicesEvent;

public class JoystickTest {

	public static void main(String[] args) {

		Joystick joystick = new Joystick();

		HidServices hidServices = HidManager.getHidServices();
		hidServices.addHidServicesListener(joystick);

		// Provide a list of attached devices
		for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
			System.out.println(hidDevice);
			if(hidDevice.getUsage() == 0x04) {
				System.out.println("Found joystick: " + hidDevice.getProduct() + " from " + hidDevice.getManufacturer());
				
				hidDevice.open();
				
				
				System.out.println("Getting Feature Report!");
				
				for (int i = 0; i < 1; i++) {
					byte[] data = new byte[2048];
					hidDevice.read(data);
					System.out.println(Arrays.toString(data));
				}
				
				hidDevice.close();
			}
		}
		
		hidServices.shutdown();
	}
	
	public static class Joystick implements HidServicesListener{

		@Override
		public void hidDeviceAttached(HidServicesEvent ev) {
			System.out.println(ev.toString());
		}

		@Override
		public void hidDeviceDetached(HidServicesEvent ev) {
			System.out.println(ev.toString());
		}

		@Override
		public void hidFailure(HidServicesEvent ev) {
			System.out.println(ev.toString());
		}
	}
}
