package engine.gal.actions;

import engine.entity.Entity;

public class Protect extends GALAction {

	private final double duration_ms;

	public Protect(double duration_ms) {
		super();

		if (duration_ms < 0.0) {
			throw new IllegalArgumentException("duration_ms cannot be negative");
		}

		this.duration_ms = duration_ms;
	}

	@Override
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		e.bot().startTimer(duration_ms);
		return true;
	}
}