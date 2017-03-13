package de.vatterger.tests.imu;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public class IMUTest extends Game {
	
	private PerspectiveCamera		camera;
	private CameraInputController	cameraInputController;
	private ModelBatch				modelBatch;

	private Model 					axesModel;
	private ModelInstance			axesInstance;
	
	private Model 					vectorModelX;
	private ModelInstance			vectorInstanceX;
	
	@Override
	public void create() {
		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(10f, 10f, 10f);
		camera.lookAt(0, 0, 0);
		camera.near = 0.1f;
		camera.far = 1000f;
		camera.update();
		
		Gdx.input.setInputProcessor(cameraInputController = new CameraInputController(camera));
		
		createAxes();
		createVectorX();
		
		imu = new IMU();
		gyro = new Vector3(0f, 0f, 0f);
		accel = new Vector3(0f, 0f, 0f);
		
		openSerial();
	}

	private SerialPort serialPort = new SerialPort("COM5");
	
	
	private void openSerial() {
		try {
	        serialPort.openPort();
	        serialPort.setParams(115200, 8, 1, 0);
	        serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
	        serialPort.readBytes();
        }
        catch (SerialPortException e) {
        	e.printStackTrace();
        }
	}
	
	private void createAxes () {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();

		MeshPartBuilder builder = modelBuilder.part("axes", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, 1, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, 1, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, 1);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}

	private void createVectorX () {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("vectorx", GL20.GL_LINES, Usage.Position | Usage.ColorUnpacked, new Material());
		builder.setColor(Color.YELLOW);
		builder.line(0, 0, 0, 0, -1, 0);
		vectorModelX = modelBuilder.end();
		vectorInstanceX = new ModelInstance(vectorModelX);
	}
	
	long last_serial = System.nanoTime();
	
	@Override
	public void render() {
		byte[] bytes;
        try {
        	while(serialPort.readBytes(1,1)[0] != 64);
        	if(serialPort.readBytes(1)[0] == 63){
            	if(serialPort.readBytes(1)[0] == 62){
                	last_serial = System.nanoTime();
                	
                	/*bytes = serialPort.readBytes(2);
                	
                	System.out.println("roll:" +bytes[0]);
                	System.out.println("pitch:" +bytes[1]);
                	
                	Quaternion qA = imu.getAttitude();
                	
                	qA.idt().setEulerAngles(0, bytes[0], bytes[1]);*/
                	
                	bytes = serialPort.readBytes(24);
                	
                	serialPort.readBytes();
                	
        			int offset = 0;
        			
        			gyro.z =  (bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0; offset+=4;
        			gyro.y =  (bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0; offset+=4;
        			gyro.x =  -((bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0); offset+=4;

        			gyro.scl(1f/16.375f);

        			
        			Vector3 accel_old = accel.cpy();
        			
        			accel.z =  (bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0; offset+=4;
        			accel.y =  (bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0; offset+=4;
        			accel.x =  -((bytes[offset+3] & 0xFF) << 24 |(bytes[offset+2] & 0xFF) << 16 |(bytes[offset+1] & 0xFF) << 8 |(bytes[offset+0] & 0xFF) << 0); offset+=4;

        			accel.scl(9.81f/4096f);
        			
        			if(accel.len() < 8 || accel.len() > 12) {
        				accel.set(accel_old);
        			}

        			
        			//System.out.println(Arrays.toString(bytes));
        			//System.out.println(Gdx.graphics.getRawDeltaTime());
        			
        			//System.out.println("g "+gyro);
        			System.out.println("a "+accel);
            	}
        	}
        	
		} catch (SerialPortException e) {
			//e.printStackTrace();
		} catch (SerialPortTimeoutException e) {
			//e.printStackTrace();
		}

        if(System.nanoTime() - last_serial > 1000L*1000L*10L)
        	System.out.println("Serial timeout: " + (System.nanoTime() - last_serial)/1000000L);

		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		
		cameraInputController.update();
		
		updateIMU();
		
		
		
		modelBatch.begin(camera);

		if(accel.len() > 8 && accel.len() < 12) {
			float acc_pitch =  MathUtils.radDeg * (float) Math.asin(accel.y/accel.len());
			float acc_roll  = -MathUtils.radDeg * (float) Math.asin(accel.x/accel.len());
			vectorInstanceX.nodes.first().rotation.setEulerAngles(0, acc_pitch, acc_roll);
			vectorInstanceX.calculateTransforms();
		}
		modelBatch.render(vectorInstanceX);
		
		axesInstance.nodes.first().rotation.set(imu.getAttitude());
		axesInstance.calculateTransforms();
		modelBatch.render(axesInstance);

		axesInstance.nodes.first().rotation.idt();
		axesInstance.calculateTransforms();
		modelBatch.render(axesInstance);

		modelBatch.end();
		
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
	}
	
	private IMU 		imu;
	private Vector3		gyro;
	private Vector3		accel;
	
	private final float xSpeed = 45f;
	private final float ySpeed = 45f;
	private final float zSpeed = 45f;
	
	private void updateIMU() {
		
		float delta = Gdx.graphics.getDeltaTime();
		
		//gyro.setZero();
		
		if(Gdx.input.isKeyPressed(Keys.I)) {
			gyro.z = -zSpeed*delta;
		} else if(Gdx.input.isKeyPressed(Keys.K)) {
			gyro.z = zSpeed*delta;
		}

		if(Gdx.input.isKeyPressed(Keys.J)) {
			gyro.x = -xSpeed*delta;
		} else if(Gdx.input.isKeyPressed(Keys.L)) {
			gyro.x = xSpeed*delta;
		}
		
		if(Gdx.input.isKeyPressed(Keys.U)) {
			gyro.y = -ySpeed*delta;
		} else if(Gdx.input.isKeyPressed(Keys.O)) {
			gyro.y = ySpeed*delta;
		}

		if(Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			gyro.setZero();
			imu.getAttitude().idt();
			accel.set(0f, -1f, 0f);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.P)) {
			accel.set(MathUtils.random(-1f,1f), MathUtils.random(-1f,1f), MathUtils.random(-1f,1f)).nor();
		}
		
		//accel.set(Vector3.X);
		//imu.getAttitude().transform(accel);
		//System.out.println(accel.toString());
		//accel.setZero();
		
		//accel.set(Vector3.Y).scl(-1f);

		//Quaternion qAccel = new Quaternion();
		
		//Vector3 axis = new Vector3();
		//float angle = imu.getAttitude().getAxisAngle(axis);
		//qAccel.setFromAxis(axis.scl(-1f), angle);
		
		//qAccel.transform(accel);
		
		//imu.getAttitude().cpy().conjugate().transform(accel);
		
		imu.update(gyro, accel, delta);
		
		//System.out.println("Roll:"+imu.getAttitude().getRoll()+", "+"Pitch:"+imu.getAttitude().getPitch()+", "+"Yaw:"+imu.getAttitude().getYaw());
	}
	
	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		super.resize(width, height);
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.update();
	}
	
	@Override
	public void dispose() {
        try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration configWindow = new LwjglApplicationConfiguration();
		
		configWindow.title = "IMU";
		
		configWindow.fullscreen = false;
		configWindow.vSyncEnabled = false;
		configWindow.resizable = true;

		configWindow.width = 1024;
		configWindow.height = 600;
		
		configWindow.foregroundFPS = 100;
		configWindow.backgroundFPS = 30;
		
		configWindow.addIcon("icon32.png", FileType.Internal);

		new LwjglApplication(new IMUTest(), configWindow);
	}
}
