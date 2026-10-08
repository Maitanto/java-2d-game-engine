package engine.graphics;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import oop.graphics.Canvas;
import oop.tasks.Task;

/**
 * Anciennement Paint, c'est un Paint plus avancée Manager de FPS : pilote le
 * repaint. La boucle d'affichage est séparée de la boucle de simulation (le
 * Ticker) : ici on demande au Canvas de se redessiner à un rythme borné par
 * fpsLimit, et on tient un compteur de frames réellement dessinées (countFrame
 * appelé par la View à chaque paint).
 */
public class FpsManager {

	private static final int A_SECOND;
	private static final int MAX_FPS;
	private static final int MIN_RECOMMENDED_FPS;
	private static final int MAX_RECOMMENDED_FPS;
	private static final int MIN_FPS;

	public static final int DEFAULT_NB_LAST_FPS_SAVED;
	public static final int DEFAULT_NB_LAST_TIME_PAINT_SAVED;

	private static final boolean LOGGING;
	private static final boolean INFO;
	private static final Logger logger;

	private static final DecimalFormat dfIndex;
	private static final DecimalFormat dfFps;
	static {
		// Logger
		logger = LoggerManager.getLogger(FpsManager.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		INFO = logger.isLoggable(Level.INFO);
		dfIndex = new DecimalFormat("00");
		dfFps = new DecimalFormat("000");
		// Constants
		A_SECOND = 1000;
		MAX_FPS = 120;
		MIN_RECOMMENDED_FPS = 30;
		MAX_RECOMMENDED_FPS = 60;
		MIN_FPS = 1;
		DEFAULT_NB_LAST_FPS_SAVED = 10;
		DEFAULT_NB_LAST_TIME_PAINT_SAVED = 32;
	}

	public final int NB_LAST_FPS_SAVED;
	private final int[] ARRAY_LAST_FPS_SAVED;
	private int indexArrayFps;

	private int sumFps;
	private int minFps;
	private int avgFps;
	private int maxFps;

	private final int UPDATE_FPS_COUNTER;
	private int fpsLimit, tempFps, fps;
	/**
	 * not relate to paintTime. It's a different thing
	 */
	private int execTime;
	public boolean shouldLogFps;
	private final Task fpsTask;

	public FpsManager(Task task, int limit, int update_timer, int nb_fps_for_avg, int nb_timevalues_for_avg,
			boolean shouldLogFps) {
		if (this.shouldLogFps && LOGGING)
			logger.info("Creating new fps controller");
		if (limit > MAX_FPS)
			throw new IllegalArgumentException(String.format("Maximum FPS is %d. Recommended FPS is between %d and %d.",
					MAX_FPS, MIN_RECOMMENDED_FPS, MAX_RECOMMENDED_FPS));
		if (limit < MIN_FPS)
			throw new IllegalArgumentException(String.format("Minimum fps is %d.", MIN_FPS));
		this.NB_LAST_FPS_SAVED = nb_fps_for_avg;
		this.ARRAY_LAST_FPS_SAVED = new int[NB_LAST_FPS_SAVED];
		this.indexArrayFps = 0;
		this.minFps = 0;
		this.avgFps = 0;
		this.maxFps = 0;
		this.fpsLimit = limit;
		this.UPDATE_FPS_COUNTER = update_timer;
		this.execTime = 0;
		this.shouldLogFps = shouldLogFps;
		this.fpsTask = task;
	}

	public FpsManager(Task task, int limit, boolean shouldLogFps) {
		this(task, limit, A_SECOND, DEFAULT_NB_LAST_FPS_SAVED, DEFAULT_NB_LAST_TIME_PAINT_SAVED, shouldLogFps);
	}

	/**
	 * Démarre les deux boucles : compteur de fps + demande de repaint.
	 * 
	 * @param canvas given Canvas
	 */
	public void start(Canvas canvas) {
		if (LOGGING && INFO && this.shouldLogFps)
			logger.info("Starting fps controller");
		this.fpsTask.post(() -> this.checkFPS());
		this.fpsTask.post(() -> this.frame(canvas));
	}

	/**
	 * 
	 * @return fpsLimit
	 */
	public int getDesiredFps() {
		return this.fpsLimit;
	}

	/**
	 * 
	 * @return fps
	 */
	public int getFps() {
		return this.fps;
	}

	public int getMinFps() {
		return minFps;
	}

	public int getAvgFps() {
		return avgFps;
	}

	public int getMaxFps() {
		return maxFps;
	}

	public String getFormattedFps() {
		return dfFps.format(this.fps);
	}

	public String getFormattedMinFps() {
		return dfFps.format(this.minFps);
	}

	public String getFormattedAvgFps() {
		return dfFps.format(this.avgFps);
	}

	public String getFormattedMaxFps() {
		return dfFps.format(this.maxFps);
	}

	/**
	 * boucle 1 : une fois par seconde, fige le compteur et le remet à zéro.
	 */
	public void checkFPS() {
		this.fps = tempFps; // this.fps sont les fps à l'affichage
		this.ARRAY_LAST_FPS_SAVED[indexArrayFps++] = tempFps;
		this.indexArrayFps %= NB_LAST_FPS_SAVED;
		this.sumFps = 0;
		this.minFps = MAX_FPS;
		this.avgFps = 0;
		this.maxFps = 0;
		for (int i = 0; i < this.NB_LAST_FPS_SAVED; i++) {
			int localFps = this.ARRAY_LAST_FPS_SAVED[i];
			this.sumFps += localFps;
			this.avgFps = this.sumFps / NB_LAST_FPS_SAVED;
			if (localFps < minFps)
				minFps = localFps;
			if (localFps > maxFps)
				maxFps = localFps;
		}
		if (LOGGING && INFO && this.shouldLogFps) {
			StringBuilder sbFpsArray = new StringBuilder();
			for (int i = 0; i < NB_LAST_FPS_SAVED; i++) {
				sbFpsArray.append("[");
				sbFpsArray.append(dfIndex.format(i));
				sbFpsArray.append(":");
				sbFpsArray.append(dfFps.format(ARRAY_LAST_FPS_SAVED[i]));
				sbFpsArray.append("]");
			}
			logger.info(String.format(
					"Execution time %ds \nfps: %d { min: %d ; avg: %d ; max: %d ; sum: %d (of the last %ds) } \n%s",
					this.execTime, this.fps, this.minFps, this.avgFps, this.maxFps, this.sumFps, this.NB_LAST_FPS_SAVED,
					sbFpsArray.toString()));
		}
		this.tempFps = 0;
		this.execTime++;
		this.fpsTask.post(() -> checkFPS(), this.UPDATE_FPS_COUNTER);
	}

	/**
	 * boucle 2 : demande un repaint tant qu'on n'a pas atteint la limite.
	 * 
	 * @param canvas given Canvas
	 */
	public void frame(Canvas canvas) {
		if (this.tempFps < this.fpsLimit)
			this.fpsTask.post(() -> canvas.repaint());
		this.fpsTask.post(() -> frame(canvas), A_SECOND / this.fpsLimit);
	}

	/**
	 * appelé à chaque frame réellement dessinée, permet d'avoir un compteur de
	 * frame accurate
	 */
	public void countFrame() {
		this.tempFps++;
	}
}
