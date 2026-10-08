package engine.gal.actions;

import engine.entity.Entity;

public class Nothing extends GALAction {

	public Nothing() {
		super();
	}

	@Override
	public boolean exec(Entity e) {
		return true;
	}
}