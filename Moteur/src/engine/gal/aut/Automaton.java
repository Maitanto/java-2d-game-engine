package engine.gal.aut;

import engine.entity.Entity;

public class Automaton {

	// FIELDS

	private final State initial;
	private final iTransitions transitions;
	private final String name;

	// CONSTRUCTOR

	public Automaton(String name, State initial) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name cannot be null or blank");
		}

		if (initial == null) {
			throw new IllegalArgumentException("initial cannot be null");
		}

		this.name = name;
		this.initial = initial;
		this.transitions = new Transitions();
	}

	// BUILDER

	/**
	 * @apiNote add a transition to the automaton after the previous ones
	 */
	public void add(Transition transition) {
		transitions.add(transition);
	}

	/**
	 * @apiNote Try to select and execute one valid transition
	 * @param e = the Entity whose bot evaluates the condition and whose stunt
	 *          executes the action
	 * @return true if there exists a transition which can be triggered by the bot,
	 *         false if no transition can be taken.
	 */
	public boolean step(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}

		State currentState = e.bot().state();

		if (currentState == null) {
			e.bot().state(initial);
			currentState = initial;
		}

		for (Transition transition : transitions.get(currentState)) {
			if (transition.exec(e)) {
				return true;
			}
		}

		return false;
	}

	public State initial() {
		return initial;
	}

	public String name() {
		return name;
	}
}