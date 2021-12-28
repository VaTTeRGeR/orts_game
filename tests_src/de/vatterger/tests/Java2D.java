
package de.vatterger.tests;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Java2D extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 1920;
	public static final int HEIGHT = WIDTH * 9 / 16;
	public static final String TITLE = "YOUR GAMES NAME";
	public static final int TICKSPERS = 120;
	public static final boolean ISFRAMECAPPED = false;

	public BufferedImage texture;
	public int rectWidth = 20;
	public int rectHeight = 20;

	public static JFrame frame;

	private Thread thread;
	private boolean running = false;

	public int frames;
	public int lastFrames;
	public int ticks;

	public Java2D () {
		Dimension size = new Dimension(WIDTH, HEIGHT);
		setPreferredSize(size);
		setMaximumSize(size);
		setMinimumSize(size);

		// texture = ImageIO.read(new File(""));
		texture = new BufferedImage(rectWidth, rectHeight, BufferedImage.TYPE_INT_RGB);
		texture.setAccelerationPriority(1f);

		for (int x = 0; x < rectWidth; x ++) {
			for (int y = 0; y < rectHeight; y ++) {
				texture.setRGB(x, y, (int)(Math.random() * Integer.MAX_VALUE));
			}
		}
	}

	public void render () {
		frames++;
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(2);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.setColor(new Color(79, 194, 232));
		g.fillRect(0, 0, getWidth(), getHeight());
		// Call your render funtions from here

		for (int x = 0; x < 1920; x += rectWidth) {
			for (int y = 0; y < 1080; y += rectHeight) {
				//g.setColor(new Color((int)(Math.random() * (double)Integer.MAX_VALUE)));
				//g.fillRect(x, y, rectWidth, rectHeight);
				g.drawImage(texture, x + (int)(Math.random()*10), y + (int)(Math.random()*10), null);
			}
		}

		g.dispose();
		bs.show();
	}

	public void tick () {
	}

	public synchronized void start () {
		if (running) return;
		running = true;
		thread = new Thread(this, "Thread");
		thread.start();
	}

	public synchronized void stop () {
		if (!running) return;
		running = false;
		try {
			System.exit(1);
			frame.dispose();
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void init () {

	}

	public void run () {
		init();
		// Tick counter variable
		long lastTime = System.nanoTime();
		// Nanoseconds per Tick
		double nsPerTick = 1000000000D / TICKSPERS;
		frames = 0;
		ticks = 0;
		long fpsTimer = System.currentTimeMillis();
		double delta = 0;
		boolean shouldRender;
		while (running) {
			shouldRender = !ISFRAMECAPPED;
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			// if it should tick it does this
			while (delta >= 1) {
				ticks++;
				tick();
				delta -= 1;
				shouldRender = true;
			}
			if (shouldRender) {
				render();
			}
			if (fpsTimer < System.currentTimeMillis() - 1000) {
				System.out.println(ticks + " ticks, " + frames + " frames");
				ticks = 0;
				lastFrames = frames;
				frames = 0;
				fpsTimer = System.currentTimeMillis();
			}
		}
	}

	public static void main (String[] args) {
		Java2D game = new Java2D();
		frame = new JFrame(TITLE);
		frame.add(game);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		game.start();
	}

}
