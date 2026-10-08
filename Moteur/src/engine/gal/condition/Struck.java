package engine.gal.condition;

import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.arguments.Category;

public class Struck extends GALCondition {

	public Struck() {
		super();
	}

	public Struck(Category category) {
		super(null, category);
	}

	@Override
	public boolean eval(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		Entity impactor = e.bot().impactor();

		if (impactor == null) {
			return false;
		}

		if (category != null && impactor.category() != category) {
			return false;
		}

		if (e.bot() instanceof GALBot) {
			((GALBot) e.bot()).selectedEntity(impactor);
		}

		return true;
	}
}