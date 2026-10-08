package engine.gal.condition;

import engine.entity.Entity;

public class Timer extends GALCondition {

	public Timer() {
		super();
	}

	@Override
	public boolean eval(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		return e.bot().timerExpired();
	}
}