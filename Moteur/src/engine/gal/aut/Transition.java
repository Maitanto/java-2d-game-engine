package engine.gal.aut;

import engine.entity.Entity;
import engine.gal.actions.iGALAction;
import engine.gal.condition.iGALCondition;

public class Transition {

	// FIELDS

	private final State source;
	private final iGALCondition condition;
	private final iGALAction action;
	private final State target;

	// CONSTRUCTOR

	public Transition(State source, iGALCondition condition, iGALAction action, State target) {
		if (source == null) {
			throw new IllegalArgumentException("source cannot be null");
		}

		if (condition == null) {
			throw new IllegalArgumentException("condition cannot be null");
		}

		if (action == null) {
			throw new IllegalArgumentException("action cannot be null");
		}

		if (target == null) {
			throw new IllegalArgumentException("target cannot be null");
		}

		this.source = source;
		this.condition = condition;
		this.action = action;
		this.target = target;
	}

	// EXEC

	/**
	 * @apiNote Tries to execute the transition and update the {@code Bot} state.
	 * @return true if the transition was triggered and the action started.
	 */
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		State currentState = e.bot().state();

		if (!source.equals(currentState)) {
			return false;
		}

		if (!condition.eval(e)) {
			return false;
		}

		if (!action.exec(e)) {
			return false;
		}

		e.bot().lastAction(action);
		e.bot().state(target);
		return true;
	}

	public State source() {
		return source;
	}

	public State target() {
		return target;
	}

	public iGALCondition condition() {
		return condition;
	}

	public iGALAction action() {
		return action;
	}
}