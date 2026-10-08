package engine.graphics.avatars;

import engine.entity.Entity;
import game.Game;
import oop.graphics.Graphics;

public abstract class Avatar {

	protected static final Game GAME;
	protected static final double PIXEL_PER_CM;
	public final static int MIN_ZORDER; // compris
	public final static int MAX_ZORDER;// compris, donc de 0 à 9
	public static final int DEFAULT_ZORDER;
	public static final int ZORDER_PLAYER;
	public static final int ZORDER_BOSS;
	static {
		GAME = Game.game();
		PIXEL_PER_CM = GAME.pixelPerCm;
		MIN_ZORDER = 0;
		MAX_ZORDER = 9;
		DEFAULT_ZORDER = 1;
		ZORDER_PLAYER = 3;
		ZORDER_BOSS = MAX_ZORDER - 1;
	}

	protected Entity entity;
	private int z_order;
	/**
	 * unused
	 */
	protected boolean shouldCull;

	protected double multX, multY;

	public int z_order() {
		return z_order;
	}

	public void set_z_order(int z_order) {
		if (z_order > MAX_ZORDER)
			throw new IllegalArgumentException("The maximum Z_order is " + MAX_ZORDER);
		if (z_order < MIN_ZORDER)
			throw new IllegalArgumentException("The minimum Z_order is " + MIN_ZORDER);

		if (this.z_order == z_order)
			return;

		int old = this.z_order;
		this.z_order = z_order;

		if (GAME.view != null)
			GAME.view.onZOrderChanged(this, old);
	}

	protected Avatar(Entity entity, int multX, int multY, int z_order, boolean shouldCull) {
		this.entity = entity;
		this.z_order = z_order;
		this.multX = multX;
		this.multY = multY;
		this.shouldCull = shouldCull;
	}

	public Entity entity() {
		return entity;
	}

	public boolean dead() {
		return entity != null && entity.dead();
	}

	/**
	 * Indique si cet avatar peut être retiré de la vue. Par défaut, un avatar est
	 * prêt dès que son entité est morte (de base il y a aucune animation de mort à
	 * attendre). Les AnimationAvatar override ce comportement pour attendre la fin
	 * de leur animation de mort.
	 *
	 * @return true si la vue peut supprimer cet avatar
	 */
	public boolean isReadyToRemove() {
		return dead();
	}

	public void kill() {
		if (entity != null) {
			entity.kill();
		}
	}

	public void revive() {
		if (entity != null) {
			entity.revive();
		}
	}

	protected int cmToPixel(double value) {
		return (int) Math.round(value * PIXEL_PER_CM);
	}

	public abstract void paint(Graphics g);

	public abstract void updateAnimation(double delta_t);

}