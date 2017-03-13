package de.vatterger.tests.imu;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class IMU {
	private Quaternion attitude = new Quaternion();

	//private float pitch = 0, roll = 0;

	public Quaternion getAttitude() {
		return attitude;
	}

	public void setAttitude(Quaternion attitude) {
		this.attitude.set(attitude);
	}
	
	public void update(Vector3 gyro, Vector3 accelerometer, float delta) {
		gyro 			= gyro.cpy();
		accelerometer	= accelerometer.cpy();
		
		Quaternion q0 = new Quaternion();

		gyro.scl(MathUtils.clamp(delta, 0.005f, 0.015f));
		
		q0.setFromAxis(Vector3.Z, gyro.x);
		attitude.mul(q0);
		
		q0.setFromAxis(Vector3.X, gyro.y);
		attitude.mul(q0);
		
		q0.setFromAxis(Vector3.Y, gyro.z);
		attitude.mul(q0);
		
		if(accelerometer.len() > 8 && accelerometer.len() < 12 && accelerometer.nor().dot(Vector3.Z) > 0.5f ) {
			
			System.out.println("correcting!");
			
			accelerometer.nor();
			float acc_pitch =  MathUtils.radDeg * (float) Math.asin(accelerometer.y);
			float acc_roll  = -MathUtils.radDeg * (float) Math.asin(accelerometer.x);
			
			q0.setEulerAngles(attitude.getYaw(), acc_roll, acc_pitch);
			
			attitude.slerp(q0, delta);
		}

		
		/*gyro.scl(MathUtils.clamp(delta, 0.005f, 0.015f));
		
		pitch += gyro.x;
		roll += gyro.y;
		
		pitch += roll  * MathUtils.sin(gyro.z * MathUtils.degRad);
		roll  += pitch * MathUtils.sin(gyro.z * MathUtils.degRad);
		
		accelerometer.nor();
		float acc_pitch =  MathUtils.radDeg * (float) Math.asin(accelerometer.y);
		float acc_roll  = -MathUtils.radDeg * (float) Math.asin(accelerometer.x);
		
		if(accelerometer.len() < 12 && accelerometer.len() > 8) {
			pitch = acc_pitch * 0.025f + pitch * 0.975f;
			roll  = acc_roll  * 0.025f + roll  * 0.975f;
		}
		
		attitude.setEulerAngles(0f, pitch, roll);*/
	}
}
