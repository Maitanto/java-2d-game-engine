package engine.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import oop.graphics.Canvas;
import oop.graphics.VirtualKeyCodes;
import oop.tasks.Runnable;

public class KeyManager implements Canvas.KeyListener, VirtualKeyCodes {

	private static final boolean LOGGING;
	private static final boolean INFO;
	private static final Logger logger;
	private final Set<Canvas.KeyListener> delegates;
	private final Map<Integer, Runnable> bindings;
	private static final Map<Integer, Boolean> down = new HashMap<>();
	static {
		logger = LoggerManager.getLogger(KeyManager.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		INFO = logger.isLoggable(Level.INFO);
	}

	public KeyManager() {
		delegates = new HashSet<>();
		bindings = new HashMap<>();
	}

	/**
	 * Adds a delegate that will also receive every key event.
	 *
	 * @param delegate the listener to add
	 * @return true if successfully added, false if not
	 */
	public boolean addDelegate(Canvas.KeyListener delegate) {
		return delegates.add(delegate);// on suppose le Set jamais nul vu qu'il est créé dans le constructeur et qu'il
										// est private final.
	}

	/**
	 * Delete a delegate from the list
	 *
	 * @param delegate the delegate to remove
	 * @return true if successfully removed, false if not
	 */
	public boolean delDelegate(Canvas.KeyListener delegate) {
		return delegates.remove(delegate);// on suppose le Set jamais nul vu qu'il est créé dans le constructeur et
											// qu'il est private final.
	}

	/**
	 * Remove all the delegates from the set
	 */
	public void clearDelegates() {
		this.delegates.clear();
	}

	/**
	 * Binds a key code to an action run once per press.
	 *
	 * @param keyCode     the virtual key code (see VirtualKeyCodes)
	 * @param toogleDebug the action to run when the key is pressed
	 */
	public void bind(int keyCode, Runnable toogleDebug) {
		bindings.put(keyCode, toogleDebug);
	}

	/**
	 * Unbinds an action
	 * 
	 * @param keyCode
	 * @param action
	 */
	public void unbind(int keyCode, Runnable action) {
		bindings.remove(keyCode);
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {
		if (LOGGING && INFO)
			logger.log(Level.INFO, () -> String.format("Key[%c:%d] pressed!", keyChar, keyCode));
		for (Canvas.KeyListener d : delegates)
			d.pressed(canvas, keyCode, keyChar);
		// permet de lancer l'action que avec le premier appui (évite que rester appuyé
		// sur une touche déclenche 10000 actions)
		boolean wasDown = Boolean.TRUE.equals(down.get(keyCode)); // down.get peut renvoyer Boolean ou null /!\ ->
																	// utiliser equals
		down.put(keyCode, true);
		if (!wasDown) {
			Runnable action = bindings.get(keyCode);
			if (action != null)
				try {
					action.run();
				} catch (Exception e) {
					logger.severe(e.getMessage());
				}
		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
		if (LOGGING && INFO)
			logger.log(Level.INFO, () -> String.format("Key[%c:%d] released!", keyChar, keyCode));
		for (Canvas.KeyListener d : delegates)
			d.released(canvas, keyCode, keyChar);
		down.put(keyCode, false);
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
		if (LOGGING && INFO)
			logger.log(Level.INFO, () -> String.format("Key[%c] typed!", keyChar));
		for (Canvas.KeyListener d : delegates)
			d.typed(canvas, keyChar);
	}

	public static boolean isPressed(int keyCode) {
		return Boolean.TRUE.equals(down.get(keyCode));
	}

}
