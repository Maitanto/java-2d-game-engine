package engine.graphics.hud;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import oop.graphics.Graphics;

/**
 * Unlike Avatars who requires entities, hud allow to display element without
 * the need for one. Hud element will always be drawn on top of any entity (due
 * to the drawing order in View)
 */
public class Hud {

	private static final boolean LOGGING;
	private static final boolean INFO;
	private static Logger logger;
	static {
		logger = LoggerManager.getLogger(Hud.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		INFO = logger.isLoggable(Level.INFO);
	}

	private final List<IHudElement> elements;
	private final List<IHudElement> toRemoveNext;

	/**
	 * Creates a hud object
	 */
	public Hud() {
		if (LOGGING && INFO)
			logger.info("Created new HUD");
		this.elements = new LinkedList<>();
		this.toRemoveNext = new LinkedList<>();
	}

	/**
	 * Ajoute un élément au HUD
	 *
	 * @param e l'élément de HUD à ajouter
	 * @return true si l'ajout a réussi, false sinon
	 */
	public boolean add(IHudElement e) {
		if (e == null)
			return false;
		if (LOGGING && INFO)
			logger.info(String.format("Added %s to the HUD", e.toString()));
		return elements.add(e);
	}

	/**
	 * Supprime un élément du HUD.
	 *
	 * @param e l'élément à supprimer
	 * @return true si la suppression a réussi, false sinon
	 */
	public boolean remove(IHudElement e) {
		if (e == null)
			return false;
		if (LOGGING && INFO)
			logger.info(String.format("Removed %s from the HUD", e.toString()));
		return elements.remove(e);
	}

	public void clear() {
		if (LOGGING && INFO)
			logger.info("Cleared all elements from HUD");
		this.elements.clear();
	}

	public Iterator<IHudElement> iterator() {
		return this.elements.iterator();
	}

	/**
	 * Dessine tous les éléments visibles dans l'ordre (le plus ancien en premier,
	 * le plus récent au-dessus).
	 *
	 * @param graphics les éléments graphiques en pixels bruts du canevas
	 */
	public void draw(Graphics graphics, int canvasWidth, int canvasHeigh) {
		for (IHudElement e : elements) {
			if (e.mustBeDeleted()) {
				e.setVisibility(false);
				this.toRemoveNext.add(e);
			} else {
				e.update(canvasWidth, canvasHeigh);
				if (e.isVisible()) {
					e.draw(graphics);
				}
			}
		}
		for (IHudElement e : toRemoveNext) {
			this.elements.remove(e);
		}
		toRemoveNext.clear();
	}
}