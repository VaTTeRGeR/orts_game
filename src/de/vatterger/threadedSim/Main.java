package de.vatterger.threadedSim;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

import de.vatterger.entitysystem.SlimeSlickServer;
import de.vatterger.entitysystem.tools.Bucket;
import de.vatterger.entitysystem.tools.GridPartitionMap;

public class Main {
	private static SERVER_STATUS runStatus = SERVER_STATUS.Idle;
	private static final int MB = 1024 * 1024;
	
	private static boolean debug = false;
	private static UpdateRunnable runnable;
	private static Thread statusThread;
	private static Runtime runtime = Runtime.getRuntime();

	private static final JTextArea console = new JTextArea();
	private static final JLabel statusLabel = new JLabel("Idle");
	private static final JLabel debugLabel = new JLabel("[--]");
	
	public static void main(String[] args) {
		/*Creating the simulation-thread*/
		runnable = new UpdateRunnable(new SlimeSlickServer());
		
		/*The target-framerate is 20 steps per second*/
		runnable.setTargetFPS(20);
				
		/*Constructing the 640x480 JFrame*/
		final JFrame frame = new JFrame("SlimeSlick-Server");
		frame.setSize(640, 480);
		
		/*Constructing the JMenuBar*/
		JMenuBar menuBar = new JMenuBar();
		
		/*Constructing the JMenu for starting/stopping the simulation*/
		JMenu simulationMenu = new JMenu("Simulation");

		/*Constructing the JMenu for Exiting the application*/
		JMenu fileMenu = new JMenu("File");
		
		/*Constructing the JMenu for Exiting the application*/
		JMenu debugMenu = new JMenu("Debug");
		
		/*Constructing the JMenuItem for closing the application*/
		JMenuItem exitButton = new JMenuItem("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				shutdown();
				frame.dispose();
				System.exit(0);
			}
		});
		
		/*Constructing the JMenuItem for deleting the savefiles*/
		JMenuItem deleteSaveButton = new JMenuItem("Delete save-files");
		deleteSaveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSaveGame();
			}
		});

		/*Constructing the Button for clearing the console*/
		JMenuItem clearConsoleButton = new JMenuItem("Clear console");
		clearConsoleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearConsole();
			}
		});
		
		/*Constructing the JMenuItem for starting the server*/
		JMenuItem startButton = new JMenuItem("Start");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		
		/*Constructing the JMenuItem for stopping the server*/
		JMenuItem stopButton = new JMenuItem("Stop");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		
		/*Constructing the JMenuItem for getting the current update-rate of the runnable*/
		JMenuItem debugButton = new JMenuItem("Toggle Debug");
		debugButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleDebug();
			}
		});
		
		/*Constructing the JMenuItem for getting the current update-rate of the runnable*/
		JMenuItem fpsButton = new JMenuItem("Update-rate");
		fpsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printFPS();
			}
		});
		
		/*Constructing the JMenuItem for getting the current memory usage*/
		JMenuItem memButton = new JMenuItem("Memory usage");
		memButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printMem();
			}
		});
		
		/*Constructing the JMenuItem for doing some test with spatialvector3map*/
		JMenuItem testButton = new JMenuItem("TEST: SpatialMap");
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GridPartitionMap<String> map = new GridPartitionMap<String>(16, 256);
				
				printConsole("Adding stuff");

				map.insert(new Vector3(0f, 0f,0f), "0-0-0");
				map.insert(new Vector3(4f, 5f,0f), "4-5-0");
				
				map.insert(new Vector3(20f, 0f,0f), "20-0-0");
				map.insert(new Vector3(16f, 17f,0f), "16-17-0");
				
				map.insert(new Vector3(19f, 19f,0f), "19-19-0");
				map.insert(new Vector3(21f, 21f,0f), "21-21-0");

				map.insert(new Vector3(210f, 210f,0f), "210-210-0");
				
				Bucket<String> b1 = map.getBucket(10f, 10f);
				for (int i = 0; i < b1.size(); i++) {
					printConsole("Bucket for [10,10] has "+b1.get(i));
				}
				
				Bucket<String> b2 = map.getBucketsMerged(new Rectangle(0, 0, 256, 256));
				for (int i = 0; i < b2.size(); i++) {
					printConsole("Bucket for [0,0,256,256] has "+b2.get(i));
				}

				printConsole("Clearing the map");
				map.clear();

				printConsole("Adding stuff");
				map.insert(new Vector3(210f, 210f,0f), "210-210-0 v2");

				Bucket<String> b3 = map.getBucketsMerged(new Rectangle(0, 0, 256, 256));
				for (int i = 0; i < b3.size(); i++) {
					printConsole("Bucket for [0,0,256,256] has "+b3.get(i));
				}
			}
		});

		fileMenu.add(exitButton);
		fileMenu.add(clearConsoleButton);
		simulationMenu.add(startButton);
		simulationMenu.add(stopButton);
		simulationMenu.add(deleteSaveButton);
		simulationMenu.add(testButton);
		debugMenu.add(debugButton);
		debugMenu.add(fpsButton);
		debugMenu.add(memButton);
		
		menuBar.add(fileMenu);
		menuBar.add(simulationMenu);
		menuBar.add(debugMenu);
		menuBar.add(statusLabel);
		menuBar.add(debugLabel);
		
		frame.setJMenuBar(menuBar);

		statusLabel.setForeground(Color.LIGHT_GRAY);
		statusLabel.setText("idle");
		
		debugLabel.setForeground(Color.LIGHT_GRAY);
		
		JScrollPane scrollPanel = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		frame.add(scrollPanel);
		
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
				frame.dispose();
				System.exit(0);
			}
		});
		
		printConsole("Welcome to SlimeSlick Server GUI");
		
		frame.setVisible(true);
	}
	
	private static void startFPSWatch() {
		statusThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(runStatus == SERVER_STATUS.Run) {
					debugLabel.setText("["+String.format("%.4f", runnable.getCurrentFPS())+"|"+(runtime.totalMemory()-runtime.freeMemory())/MB+"M]");
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		statusThread.start();
		debugLabel.setForeground(Color.BLACK);
	}
	
	private static void stopFPSWatch() {
		statusThread.interrupt();
		debugLabel.setForeground(Color.LIGHT_GRAY);
	}
	
	private static void start() {
		if (runStatus == SERVER_STATUS.Idle) {
			printConsole("Starting Server");
			runnable.setDebug(debug);
			runnable.startSimulation();

			runStatus = SERVER_STATUS.Run;
			statusLabel.setForeground(Color.BLUE);
			statusLabel.setText("Server running");
			startFPSWatch();
		} else {
			printConsole("Server already started.");
		}
	}
	
	private static void stop() {
		if(runStatus == SERVER_STATUS.Run) {
			printConsole("Stopping server");
			runnable.stopSimulation();

			runStatus = SERVER_STATUS.Idle;
			statusLabel.setForeground(Color.LIGHT_GRAY);
			statusLabel.setText("Server stopped");
			
			stopFPSWatch();
		} else {
			printConsole("No server running");
		}
	}
	
	private static void shutdown() {
		stop();
		printConsole("Shutting down...");
	}
	
	private static void printFPS() {
		final String sCL;
		if(runStatus == SERVER_STATUS.Run)
			sCL = "Current";
		else
			sCL = "Last";
			printConsole(sCL + " FPS: "+runnable.getCurrentFPS()+"\n");
	}
	
	private static void printMem() {
		printConsole("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / MB);
		printConsole("Free Memory:" + runtime.freeMemory() / MB);
		printConsole("Total Memory:" + runtime.totalMemory() / MB);
		printConsole("Max Memory:" + runtime.maxMemory() / MB+"\n");
	}
	
	private static void toggleDebug(){
		debug = !debug;
		runnable.setDebug(debug);
		
		if(runnable.getIsDebug())
			printConsole("Debug enabled.");
		else
			printConsole("Debug disabled.");
	}
	
	private static void deleteSaveGame() {
		FileHandle savegame = new FileHandle("data/kryo.gzip");
		if (savegame.exists()) {
			savegame.delete();
			printConsole("Deleted Savefile.");
		} else {
			printConsole("Nothing to delete.");
		}
	}
	
	private static void printConsole(String s) {
		console.append(s+"\n");
	}

	private static void clearConsole() {
		console.setText("");
	}
}
enum SERVER_STATUS {Run,Idle}