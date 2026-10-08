package engine.gal.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import engine.entity.Entity;

public class ProbabilisticAction implements iGALAction {

	private static final Random RANDOM = new Random();

	private final List<iGALAction> actions;
	private final List<Integer> weights;

	public ProbabilisticAction(List<iGALAction> actions, List<Integer> weights) {
		if (actions == null || weights == null)
			throw new IllegalArgumentException("actions and weights cannot be null");
		if (actions.size() != weights.size())
			throw new IllegalArgumentException("actions and weights must have the same size");
		if (actions.isEmpty())
			throw new IllegalArgumentException("actions cannot be empty");

		int total = weights.stream().mapToInt(Integer::intValue).sum();
		if (total != 100)
			throw new IllegalArgumentException("weights must sum to 100, got " + total);

		this.actions = new ArrayList<>(actions);
		this.weights = new ArrayList<>(weights);
	}

	@Override
	public boolean exec(Entity e) {
		int roll = RANDOM.nextInt(100);
		int cumulative = 0;
		for (int i = 0; i < actions.size(); i++) {
			cumulative += weights.get(i);
			if (roll < cumulative) {
				return actions.get(i).exec(e);
			}
		}

		return actions.get(actions.size() - 1).exec(e);
	}

	public List<iGALAction> actions() {
		return Collections.unmodifiableList(actions);
	}

	public List<Integer> weights() {
		return Collections.unmodifiableList(weights);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int length = Math.min(actions.size(), weights.size());
		for (int i = 0; i < length; i++) {
			sb.append('[');
			sb.append(actions.get(i));
			sb.append(':');
			sb.append(weights.get(i));
			sb.append(']');
		}
		return sb.toString();
	}
}