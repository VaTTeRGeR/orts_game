package de.vatterger.game.systems.graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;

import de.vatterger.engine.util.Profiler;

public class GraphicalProfilerSystem extends BaseSystem {

	/**The maximum number of values remebered (includes interpolated values!)*/
	private static final int QUEUE_LENGTH_MAX = 2048;
	
	/**A value between 0 and 99 - Number of Interpolated values added between real samples to smooth transitions.*/
	private static final int INTERPOLATION_STEPS = 0;
	
	@Wire(name = "stage")
	private Stage stage;
	
	@Wire(name = "skin")
	private Skin skin;
	
	private ShapeRenderer shapeRenderer;
	
	private OrthographicCamera camera;
	private Batch batch;
	private BitmapFont font;
	
	private Vector3 v0	= new Vector3();
	private Vector3 v1	= new Vector3();

	
	private static Profiler							combinedProfiler		= null;
	private static Queue<Long>						combinedProfilerQueue	= null;

	
	private static HashMap<String, Profiler>	nameToProfilerMap	= new HashMap<>(32);
	
	private static ArrayList<Profiler>			profilerList		= new ArrayList<>(32);
	private static ArrayList<String>				profilerNameList	= new ArrayList<>(32);
	private static ArrayList<Color>				profilerColorList	= new ArrayList<>(32);
	private static ArrayList<Queue<Long>>		profilerQueueList	= new ArrayList<>(32);
	
	private static Queue<Long>						profilerMemoryList				= new Queue<Long>(32);
	private static ArrayList<Long>				profilerMemoryDifferencesList	= new ArrayList<Long>(32);

	private float[]									yOffsets			= new float[QUEUE_LENGTH_MAX];
	
	private	long										maxDeltaTime		= 0;
	
	private boolean									show				= false;

	
	private TextButton dragLL, dragUR;
	
	@Override
	protected void initialize() {

		this.camera = new OrthographicCamera();
		
		font = new BitmapFont();
		
		batch = new SpriteBatch(512);
		
		shapeRenderer = new ShapeRenderer(4096);
		
		
		dragLL = new TextButton("CLICK", skin);
		dragLL.setPosition(Gdx.graphics.getWidth() * 0.25f, Gdx.graphics.getHeight()*0.25f, Align.center);
		
		dragLL.addAction(new Action() {
			
			private boolean currentlyDragging = false;
			
			@Override
			public boolean act(float delta) {

				if(dragLL.isPressed() || currentlyDragging && Gdx.input.isButtonPressed(Buttons.LEFT)) {
					
					currentlyDragging = true;
					
					dragLL.setText("DRAG");
					dragLL.setColor(1f, 1f, 1f, 1f);

					dragLL.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), Align.center);
					
				} else {
					
					currentlyDragging = false;
					
					//Keep LL below and left from UR
					if(dragLL.getX(Align.topRight) > dragUR.getX(Align.bottomLeft)) dragLL.setX(dragUR.getX(Align.bottomLeft), Align.topRight);
					if(dragLL.getY(Align.topRight) > dragUR.getY(Align.bottomLeft)) dragLL.setY(dragUR.getY(Align.bottomLeft), Align.topRight);
					
					//Keep away from offscreen area
					if(dragLL.getX(Align.bottomLeft) < 0) dragLL.setX(0, Align.bottomLeft);
					if(dragLL.getY(Align.bottomLeft) < 0) dragLL.setY(0, Align.bottomLeft);

					//Keep away from offscreen area
					if(dragLL.getX(Align.topRight) > Gdx.graphics.getWidth()) dragLL.setX(Gdx.graphics.getWidth(), Align.topRight);
					if(dragLL.getY(Align.topRight) > Gdx.graphics.getHeight()) dragLL.setY(Gdx.graphics.getHeight(), Align.topRight);
					
					dragLL.setText("CLICK");
					dragLL.setColor(1f, 1f, 1f, 0.3f);
				}
				
				return false;
			}
		});
		
		dragUR = new TextButton("CLICK", skin);
		dragUR.setPosition(Gdx.graphics.getWidth() * 0.75f, Gdx.graphics.getHeight()*0.75f, Align.center);
		
		dragUR.addAction(new Action() {
			
			private boolean currentlyDragging = false;
			
			@Override
			public boolean act(float delta) {

				if(dragUR.isPressed() || currentlyDragging && Gdx.input.isButtonPressed(Buttons.LEFT)) {
					
					currentlyDragging = true;
					
					dragUR.setText("DRAG");
					dragUR.setColor(1f, 1f, 1f, 1f);
					
					dragUR.setPosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), Align.center);
					
				} else {
					
					currentlyDragging = false;
					
					//Keep UR above and right from LL
					if(dragUR.getX(Align.bottomLeft) < dragLL.getX(Align.topRight)) dragUR.setX(dragLL.getX(Align.topRight), Align.bottomLeft);
					if(dragUR.getY(Align.bottomLeft) < dragLL.getY(Align.topRight)) dragUR.setY(dragLL.getY(Align.topRight), Align.bottomLeft);
					
					//Keep away from offscreen area
					if(dragUR.getX(Align.bottomLeft) < 0) dragUR.setX(0, Align.bottomLeft);
					if(dragUR.getY(Align.bottomLeft) < 0) dragUR.setY(0, Align.bottomLeft);

					//Keep away from offscreen area
					if(dragUR.getX(Align.topRight) > Gdx.graphics.getWidth()) dragUR.setX(Gdx.graphics.getWidth(), Align.topRight);
					if(dragUR.getY(Align.topRight) > Gdx.graphics.getHeight()) dragUR.setY(Gdx.graphics.getHeight(), Align.topRight);
					
					dragUR.setText("CLICK");
					dragUR.setColor(1f, 1f, 1f, 0.3f);
				}
				
				return false;
			}
		});
		
		stage.addActor(dragLL);
		stage.addActor(dragUR);

		dragLL.setVisible(show);
		dragUR.setVisible(show);
	}
	
	@Override
	protected void processSystem() {
		
		if(combinedProfiler != null) {
			combinedProfiler.stop();
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.F2)) {
			
			show = !show;
			
			dragLL.setVisible(show);
			dragUR.setVisible(show);
		}

		collectData();
		
		if (show) {
			render();
		}
	}
	
	private void collectData() {
		
		if(combinedProfiler == null) return;
		
		if(combinedProfilerQueue == null) return;
		
		enqueueValue(combinedProfilerQueue, combinedProfiler);
		
		for (int i = 0; i < profilerList.size(); i++) {
			
			final Profiler profiler = profilerList.get(i);
			
			if(profiler == null) continue;
			
			Queue<Long> queue = profilerQueueList.get(i);
			
			if(queue == null) continue;
			
			enqueueValue(queue, profiler);
		}
		
		profilerMemoryList.addFirst(Long.valueOf(usedRamBytes()));
		while(profilerMemoryList.size >= QUEUE_LENGTH_MAX - 1) {
			profilerMemoryList.removeLast();
		}
		
		if(profilerMemoryList.size >= 2) {
			
			long vNew = Math.max(profilerMemoryList.get(1) - profilerMemoryList.get(0), 0L);
			
			for (int i = 0; i < Math.min(profilerMemoryDifferencesList.size(), 64); i++) {
				
				long vOld = profilerMemoryDifferencesList.get(i);
				
				if(vNew > 0 && vOld > 0 && vOld > vNew) {
					profilerMemoryDifferencesList.set(i, 0L);
					vNew += vOld;
				}
			}
			
			profilerMemoryDifferencesList.add(0,vNew);
		}

		while(profilerMemoryDifferencesList.size() >= QUEUE_LENGTH_MAX - 1) {
			profilerMemoryDifferencesList.remove(profilerMemoryDifferencesList.size()-1);
		}
	}
	
	/**
	 * 
	 * */
	@SuppressWarnings("unused")
	private void enqueueValue(Queue<Long> queue, Profiler profiler) {

		long timeMicros = profiler.getMeasuredTime(TimeUnit.MICROSECONDS);
		
		timeMicros = Math.max(0, timeMicros);

		while(queue.size >= QUEUE_LENGTH_MAX - INTERPOLATION_STEPS - 1) queue.removeLast();
		
		if(queue.size > 0 && INTERPOLATION_STEPS > 0 && INTERPOLATION_STEPS < 100) {

			final long prevValue = queue.first();
			
			final int interpStepsDelta = 100 / (INTERPOLATION_STEPS + 1);

			//System.out.print("a:" + prevValue + " b:" + timeMicros + " -> ");
			
			for (int alpha = interpStepsDelta; alpha <= 100 - interpStepsDelta; alpha += interpStepsDelta) {
				queue.addFirst(interpolate(prevValue, timeMicros, alpha));
				//System.out.print(" " + interpolate(prevValue, timeMicros, alpha) + "@" + alpha);
			}
			//System.out.println();
		}
		
		queue.addFirst(timeMicros);
	}
	
	/**
	 * Interpolate between a and b by choosing an alpha between 0 and 100
	 * @param a The value for alpha=0
	 * @param b The value for alpha=100
	 * @param alpha The interpolation value between 0 and 100
	 * */
	private long interpolate(long a, long b, long alpha) {
		return (a * 100L + (b - a) * alpha + 50L) / 100L;
	}

	private void render() {

		//camera.position.set(Gdx.graphics.getWidth() / 2, -1f, Gdx.graphics.getHeight() / 2);

		//camera.lookAt(Gdx.graphics.getWidth() / 2, 0f, Gdx.graphics.getHeight() / 2);

		//camera.update();

		shapeRenderer.setProjectionMatrix(camera.combined);

		shapeRenderer.begin(ShapeType.Line);

		shapeRenderer.setColor(Color.WHITE);

		v0.set(dragLL.getX(Align.topRight), dragLL.getY(Align.topRight), 0);
		v1.set(dragUR.getX(Align.bottomLeft), dragLL.getY(Align.topRight), 0);
		shapeRenderer.line(v0, v1);
		
		v0.set(dragLL.getX(Align.topRight), dragUR.getY(Align.bottomLeft), 0);
		v1.set(dragUR.getX(Align.bottomLeft), dragUR.getY(Align.bottomLeft), 0);
		shapeRenderer.line(v0, v1);
		
		float x0 = dragLL.getX(Align.topRight);
		float y0 = dragLL.getY(Align.topRight);
		float x1 = dragUR.getX(Align.bottomLeft);
		float y1 = dragUR.getY(Align.bottomLeft);
		
		float dx = x1 - x0;
		float dy = y1 - y0;
		
		for (int i = 0; i < profilerMemoryDifferencesList.size() - 1 && i < dx; i++) {
			// positive value means memory has been freed up by GC
			if(profilerMemoryDifferencesList.get(i) > 0) {
				shapeRenderer.setColor(Color.YELLOW);
				shapeRenderer.line(x1 - i, y1 + 5f, x1 - i, y0);
			}
		}
		
		Arrays.fill(yOffsets, 0f);
		
		for (int i = 0; i < profilerList.size(); i++) {
			
			shapeRenderer.setColor(profilerColorList.get(i));
			
			Queue<Long> queue = profilerQueueList.get(i);
			
			float valuesDisplayed = 0f;
			
			for (Long value : queue) {
				
				final int index = (int)(valuesDisplayed + 0.5f);

				float valueOffset = ((value * dy) / 16666f);
				
				v0.set(x1 - valuesDisplayed, y0 + yOffsets[index], 0f);
				v1.set(x1 - valuesDisplayed, y0 + yOffsets[index] + valueOffset, 0f);
				shapeRenderer.line(v0, v1);
				
				yOffsets[index] += valueOffset;
				
				valuesDisplayed++;
				
				if(valuesDisplayed >= dx) break;
			}
		}

		shapeRenderer.setColor(Color.GRAY);
		
		float valuesDisplayed = 1f;
		
		for (Long value : combinedProfilerQueue) {
			
			final int index = (int)(valuesDisplayed + 0.5f);

			float valueOffset = ((value * dy) / 16666f);
			
			v0.set(x1 - valuesDisplayed, y0 + yOffsets[index], 0f);
			v1.set(x1 - valuesDisplayed, y0 + valueOffset, 0f);
			shapeRenderer.line(v0, v1);
			
			valuesDisplayed++;
			
			if(valuesDisplayed >= dx) break;
		}
		

		
		for (int i = 1; i <= 15; i++) {

			if(i%5 == 0) {
				shapeRenderer.setColor(Color.CORAL);
			} else {
				shapeRenderer.setColor(Color.WHITE);
			}
			
			v0.set(x0, y0 + (dy*i)/16f, 0);
			v1.set(x1, y0 + (dy*i)/16f, 0);
			
			shapeRenderer.line(v0, v1);
			
		}
		
		maxDeltaTime *= 0.998;
		
		long averageCombinedDeltaTime = 0;
		long elements = Math.min(combinedProfilerQueue.size, 60);

		for (int i = 0; i < elements; i++) {
			
			long v = combinedProfilerQueue.get(i);
			
			averageCombinedDeltaTime += v;

			if(v > maxDeltaTime && v <= 16666) {
				maxDeltaTime = v;
			}
		}
		averageCombinedDeltaTime /= elements;

		v0.set(x0, y0 + (averageCombinedDeltaTime * dy) / 16666f, 0f);
		v1.set(x1, y0 + (averageCombinedDeltaTime * dy) / 16666f, 0f);
		
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(v0, v1);
		
		v0.sub(0f, 1f, 0f);
		v1.sub(0f, 1f, 0f);
		
		shapeRenderer.line(v0, v1);
		
		v0.set(x0, y0 + (maxDeltaTime * dy) / 16666f, 0f);
		v1.set(x1, y0 + (maxDeltaTime * dy) / 16666f, 0f);

		shapeRenderer.setColor(Color.PINK);
		shapeRenderer.line(v0, v1);

		v0.sub(0f, 1f, 0f);
		v1.sub(0f, 1f, 0f);
		
		shapeRenderer.line(v0, v1);
		
		shapeRenderer.end();

		camera.setToOrtho(false);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		GlyphLayout layout = new GlyphLayout();
		layout.setText(font, "MEM: " + usedRamMegaBytes() + "/" + allocatedRamMegaBytes() + " MB", Color.RED, 150f, Align.left, true);
		
		font.draw(batch, layout, x0, y1 + font.getLineHeight());

		layout.setText(font, "FPS: " + Gdx.graphics.getFramesPerSecond(), Color.RED, 75f, Align.left, true);
		font.draw(batch, layout, x0 + 150f, y1 + font.getLineHeight());

		layout.setText(font, "Load: " + averageCombinedDeltaTime*100/16666 + " %", Color.RED, 100f, Align.left, true);
		font.draw(batch, layout, x0 + 150f + 75f, y1 + font.getLineHeight());

		layout.setText(font, ""+averageCombinedDeltaTime/1000f, Color.RED, 100f, Align.left, true);
		font.draw(batch, layout, x0 - layout.width, y0 + (averageCombinedDeltaTime * dy) / 16666f + layout.height / 2f);
		
		layout.setText(font, ""+maxDeltaTime/1000f, Color.PINK, 100f, Align.left, true);
		font.draw(batch, layout, x0 - layout.width, y0 + (maxDeltaTime * dy) / 16666f + layout.height / 2f);
		
		for (int i = 0; i < profilerNameList.size(); i++) {
			layout.setText(font, profilerNameList.get(i), profilerColorList.get(i), 250f, Align.bottomLeft, false);
			font.draw(batch, layout, x1 + 10f, y0 + (i + 1) * layout.height * 1.5f);
		}
		
		for (int i = 0; i < profilerMemoryDifferencesList.size() - 1 && i < dx; i++) {
			
			// positive value means memory has been freed up by GC
			long dm = profilerMemoryDifferencesList.get(i);
			
			if(dm > 0) {
				layout.setText(font, "" + dm/1024/1024 + "MB", Color.YELLOW, 100f, Align.left, true);
				font.draw(batch, layout, x1 - i - layout.width*0.5f, y1 + layout.height + 5);
			}
		}
		
		batch.end();
	}
	
	public static long usedRamBytes() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

	public static long usedRamMegaBytes() {
        long memBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return memBytes/1024/1024;
    }
	
	public static long allocatedRamMegaBytes() {
        long memBytes = Runtime.getRuntime().totalMemory();
        return memBytes/1024/1024;
    }
	
	/**
	 * Sets the Profiler that measures the total time spent in all systems combined.
	 * This is needed to calculate the time spent in not explicitly profiled systems.
	 * @param profiler The Profiler object.
	 */
	public static void setCombinedProfiler(Profiler profiler) {
		
		if(profiler == null) throw new IllegalArgumentException("Combined profiler cannot be set to null");
		
		combinedProfiler = profiler;
		
		combinedProfilerQueue = new Queue<Long>(QUEUE_LENGTH_MAX);
		
		for (Queue<Long> queue : profilerQueueList) {
			queue.clear();
		}
	}
	
	/**
	 * Adds a Profiler to the displayed Profiler Stack.
	 * @param name Unique name of the profiler.
	 * @param color Display color for the Profiler.
	 * @param profiler The Profiler object.
	 */
	public static void registerProfiler(String name, Color color, Profiler profiler) {
		
		if(name == null) throw new IllegalArgumentException("Cannot register Profiler with alias '" + name + "'.");

		if(color == null)  throw new IllegalArgumentException("Cannot register Profiler '" + name + "' with color '" + color + "'.");
		
		if(profiler == null) throw new IllegalArgumentException("Profiler with alias '" + name + "' is '" + profiler + "'.");
		
		
		if(nameToProfilerMap.containsKey(name)) throw new IllegalArgumentException("A profiler with alias '" + name + "' has already been registered.");
		
		
		nameToProfilerMap.put(name, profiler);
		
		
		profilerList.add(profiler);
		
		profilerNameList.add(name);
		
		profilerColorList.add(color);
		
		profilerQueueList.add(new Queue<Long>(QUEUE_LENGTH_MAX));
		
		for (Queue<Long> queue : profilerQueueList) {
			queue.clear();
		}
	}
	
	/**
	 * @param Unique name of the profiler.
	 */
	public static void unregisterProfiler(String name) {

		if(name == null) throw new IllegalArgumentException("Cannot unregister Profiler with alias '" + name + "'.");
		
		if(!nameToProfilerMap.containsKey(name)) throw new IllegalArgumentException("There is no Profiler registered with the alias '" + name + "'.");
		
		
		Profiler profiler = nameToProfilerMap.remove(name);
		
		if(profiler == null) throw new IllegalStateException("A Profiler was found under alias '" + name + "' but it's value in the nameToProfilerMap is '" + profiler + "'.");
		
		
		final int index = profilerList.indexOf(profiler);
		
		if(index < 0) throw new IllegalStateException("Profiler '" + name + "' found in nameToProfilerMap but not in profilerList.");

		
		profilerList.remove(index);
		
		profilerNameList.remove(index);

		profilerColorList.remove(index);
		
		profilerQueueList.remove(index);
	}
	
	@Override
	protected void dispose() {
		shapeRenderer.dispose();
		batch.dispose();
		font.dispose();
		
		dragLL.remove();
		dragUR.remove();
	}
}
