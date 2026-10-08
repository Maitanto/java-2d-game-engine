package engine.gal.condition;

import engine.entity.Entity;

public class True extends GALCondition {

	public True() {
		super();
	}

	@Override
	public boolean eval(Entity e) {
		return true;
	}

}