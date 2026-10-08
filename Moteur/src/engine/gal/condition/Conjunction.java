package engine.gal.condition;

import java.util.ArrayList;
import java.util.List;

import engine.entity.Entity;

public class Conjunction extends GALCondition {

	// FIELDS

	private final List<iGALCondition> conditions;

	// CONSTRUCTOR

	public Conjunction() {
		super();
		this.conditions = new ArrayList<>();
	}

	// BUILDER

	public void add(iGALCondition condition) {
		if (condition == null) {
			throw new IllegalArgumentException("condition cannot be null");
		}

		conditions.add(condition);
	}

	// EVAL

	@Override
	public boolean eval(Entity e) {
		for (iGALCondition condition : conditions) {
			if (!condition.eval(e)) {
				return false;
			}
		}

		return true;
	}
}