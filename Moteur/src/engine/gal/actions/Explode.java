package engine.gal.actions;

import engine.entity.Entity;

public class Explode extends GALAction {

	public Explode() {
		super();
	}

	@Override
	public boolean exec(Entity e) {
		if (e == null) {
			return false;
		}

		e.kill();
		return true;
	}
}