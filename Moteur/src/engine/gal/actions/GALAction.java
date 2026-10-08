package engine.gal.actions;

import engine.entity.Entity;

public abstract class GALAction implements iGALAction {

	/**
	 * @param intensity &in; [0,1] ≃ %
	 */
	protected final double intensity;

	// CONSTANT

	public static final Nothing NOTHING = new Nothing();

	// CONSTRUCTORS

	protected GALAction(double intensity) {
		if (intensity < 0.0 || intensity > 1.0) {
			throw new IllegalArgumentException("intensity must be in [0, 1]");
		}

		this.intensity = intensity;
	}

	protected GALAction() {
		this(1.0);
	}

	public double intensity() {
		return intensity;
	}

	@Override
	public abstract boolean exec(Entity e);

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}