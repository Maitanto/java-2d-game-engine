package engine.move;

import oop.tasks.Task;
import oop.tasks.Runtime;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import oop.tasks.Runnable;

public class Ticker implements Runnable {
	private static final boolean LOGGING;
	private static final boolean INFO;
	private static final Logger logger;

	private static final boolean START_AT_CREATION;

	private static final int FRAME_DELAY_MS;
	private static final double NANO_TO_SECONDS;

	static {
		// Logger
		logger = LoggerManager.getLogger(Ticker.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		INFO = logger.isLoggable(Level.INFO);

		// Constants
		START_AT_CREATION = true;
		FRAME_DELAY_MS = 16;
		NANO_TO_SECONDS = 1_000_000_000.0;
	}

	private Task task;
	private Model model;

	private long lastTime;
	private boolean running;

	public Ticker(Model model) {
		if (LOGGING && INFO)
			logger.log(Level.INFO, "Created new Ticker");
		this.model = model;
		this.task = Runtime.newTask("GameLoopTask");
		if (START_AT_CREATION)
			this.start();
	}

	@Override
	public void run() throws Exception {
		if (!running) {
			return;
		}

		tick();

		task.post(this, FRAME_DELAY_MS);
	}

	private void tick() {
		long current = System.nanoTime();
		double deltaT = (current - lastTime) / NANO_TO_SECONDS;
		lastTime = current;

		try {
			model.tick(deltaT);
		}catch(Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
			Runtime.shutdown();
		}
	}

	public void start() {
		this.lastTime = System.nanoTime();
		this.running = true;
		this.task.post(this);
		if (LOGGING && INFO)
			logger.log(Level.INFO, "Started Ticker");
		if (this.listeners != null)
			for (TickerListener l : listeners) {
				l.starting();
			}
	}

	public void stop() {
		this.running = false;
		if (LOGGING && INFO)
			logger.log(Level.INFO, "Stopped Ticker");
		if (this.listeners != null)
			for (TickerListener l : listeners) {
				l.stopping();
			}
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * toggle if the ticker is running or not
	 * 
	 * @return the current state of this ticker : true if running, false if not
	 */
	public boolean toggleRunning() {
		this.running = !this.running;
		if (running) {
			this.start();
		} else {
			this.stop();
		}
		return this.running;
	}

	/**
	 * 
	 * À ne pas utiliser dans le jeu final. C'est du test
	 */
	public void test() {
		while (running) {
			tick();

			try {
				Thread.sleep(FRAME_DELAY_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				stop();
			}
		}
	}

	private Set<TickerListener> listeners;

	/**
	 * Pour détecter la pause
	 */
	public static interface TickerListener {
		void starting();

		void stopping();
	}

	public boolean addListener(TickerListener l) {
		if (this.listeners == null)
			this.listeners = new HashSet<>();
		return this.listeners.add(l);
	}

	public boolean delListener(TickerListener l) {
		if (this.listeners == null)
			return true;
		return this.listeners.remove(l);
	}

	public void clearListeners() {
		if (this.listeners != null)
			this.listeners.clear();
	}

}