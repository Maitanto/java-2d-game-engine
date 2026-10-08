package engine.gal.condition;

import engine.entity.Entity;

public class Life extends GALCondition {

	private final int threshold;

	public Life(int threshold) {
		super();

		if (threshold < 0 || threshold > 100) {
			throw new IllegalArgumentException("threshold must be in [0, 100]");
		}

		this.threshold = threshold;
	}

	@Override
	public boolean eval(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		return e.bot().life() <= threshold;
	}
}