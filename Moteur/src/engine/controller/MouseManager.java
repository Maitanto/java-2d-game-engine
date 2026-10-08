package engine.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import oop.graphics.Canvas;

public class MouseManager implements Canvas.MouseListener {

	public static final int BNO_LEFT_BUTTON_MOUSE = 1;
	public static final int BNO_MIDDLE_BUTTON_MOUSE = 2;
	public static final int BNO_RIGHT_BUTTON_MOUSE = 3;

	private static final boolean LOGGING;
	private static final boolean FINE;
	private static final Logger logger;
	private final Map<Integer, Runnable> bindings;
	private final Map<Integer, Boolean> down;

	static {
		logger = LoggerManager.getLogger(KeyManager.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		FINE = logger.isLoggable(Level.FINE);
	}

	private final Set<Canvas.MouseListener> delegates;

	public MouseManager() {
		this.bindings = new HashMap<>();
		this.down = new HashMap<>();
		this.delegates = new HashSet<>();
	}

	/**
	 * Adds a delegate that will also receive every key event.
	 *
	 * @param delegate the listener to add
	 * @return true if successfully added, false if not
	 */
	public boolean addDelegate(Canvas.MouseListener delegate) {
		if (delegate == null)
			return false;
		if (LOGGING && FINE)
			logger.log(Level.FINE, () -> String.format("Added delegate '%s' to MouseManager '%s'.", delegate.toString(),
					this.toString()));
		return delegates.add(delegate);// on suppose le Set jamais nul vu qu'il est créé dans le constructeur et qu'il
										// est private final.
	}

	/**
	 * Delete a delegate from the list
	 *
	 * @param delegate the delegate to remove
	 * @return true if successfully removed, false if not
	 */
	public boolean delDelegate(Canvas.MouseListener delegate) {
		if (delegate == null)
			return false;
		if (LOGGING && FINE)
			logger.log(Level.FINE, () -> String.format("Removed delegate '%s' from MouseManager '%s'.",
					delegate.toString(), this.toString()));
		return delegates.remove(delegate);// on suppose le Set jamais nul vu qu'il est créé dans le constructeur et
											// qu'il est private final.
	}

	/**
	 * Remove all the delegates from the set
	 */
	public void clearDelegates() {
		this.delegates.clear();
		// on suppose le Set jamais nul vu qu'il est créé dans le constructeur et
		// qu'il est private final.
		if (LOGGING && FINE)
			logger.log(Level.FINE, "Cleared MouseManager delegates.");
	}

	/**
	 * Binds a mouseButton code to an action run once per press.
	 *
	 * @param bno    the mouse button number
	 * @param action the action to run when the key is pressed
	 */
	public void bind(int bno, Runnable action) {
		bindings.put(bno, action);
	}

	/**
	 * Unbinds an action
	 * 
	 * @param bno
	 * @param action
	 */
	public void unbind(int bno, Runnable action) {
		bindings.remove(bno);
	}

	@Override
	public void moved(Canvas canvas, int x, int y) {
		// pas de logging pour les mouvements de la souris car ça spam trop
		for (Canvas.MouseListener d : delegates)
			d.moved(canvas, x, y);
	}

	@Override
	public void pressed(Canvas canvas, int bno, int x, int y) {
		if (LOGGING && FINE)
			logger.log(Level.FINE, () -> String.format("Mouse (%d button) pressed at (%d,%d).", bno, x, y));

		for (Canvas.MouseListener d : delegates)
			d.pressed(canvas, bno, x, y);

		boolean wasDown = Boolean.TRUE.equals(down.get(bno)); // down.get peut renvoyer Boolean ou null /!\ ->
		// utiliser equals
		down.put(bno, true);
		if (!wasDown) {
			Runnable action = bindings.get(bno);
			if (action != null)
				action.run();
		}
	}

	@Override
	public void released(Canvas canvas, int bno, int x, int y) {
		if (LOGGING && FINE)
			logger.log(Level.FINE, () -> String.format("Mouse (%d button) released at (%d,%d).", bno, x, y));
		for (Canvas.MouseListener d : delegates)
			d.released(canvas, bno, x, y);
		down.put(bno, false);
	}

}
