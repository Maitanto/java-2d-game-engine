package engine.gal.actions;

import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.arguments.Category;

public class Get extends GALAction {

	private final Category category;

	public Get(Category category) {
		super();
		this.category = category;
	}

	@Override
	public boolean exec(Entity e) {
		if (e == null || !(e.bot() instanceof GALBot)) {
			return false;
		}

		Entity selected = ((GALBot) e.bot()).selected();

		if (selected == null) {
			return false;
		}

		if (category != null && selected.category() != category) {
			return false;
		}

		selected.kill();
		return true;
	}
}