package engine.gal.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.entity.Entity;

public class SequenceAction implements iGALAction {

	private final List<iGALAction> actions;

	public SequenceAction(List<iGALAction> actions) {
		if (actions == null) {
			throw new IllegalArgumentException("actions cannot be null");
		}

		if (actions.isEmpty()) {
			throw new IllegalArgumentException("actions cannot be empty");
		}

		this.actions = new ArrayList<>();

		for (iGALAction action : actions) {
			if (action == null) {
				throw new IllegalArgumentException("action cannot be null");
			}

			this.actions.add(action);
		}
	}

	@Override
	public boolean exec(Entity e) {
		for (iGALAction action : actions) {
			if (!action.exec(e)) {
				return false;
			}
		}

		return true;
	}

	public List<iGALAction> actions() {
		return Collections.unmodifiableList(actions);
	}

	@Override
	public String toString() {
		return actions.toString();
	}
	
	
}