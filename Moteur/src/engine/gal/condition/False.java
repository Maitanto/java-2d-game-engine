package engine.gal.condition;

import engine.entity.Entity;

public class False extends GALCondition {

	public False() {
		super();
	}

	@Override
	public boolean eval(Entity e) {
		return false;
	}
}