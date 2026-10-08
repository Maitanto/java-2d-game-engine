package engine.gal.aut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transitions implements iTransitions {

	private final Map<State, List<Transition>> transitions;

	public Transitions() {
		this.transitions = new HashMap<>();
	}

	@Override
	public void add(Transition transition) {
		if (transition == null) {
			throw new IllegalArgumentException("transition cannot be null");
		}

		transitions.computeIfAbsent(transition.source(), state -> new ArrayList<>()).add(transition);
	}

	@Override
	public List<Transition> get(State state) {
		if (state == null) {
			return Collections.emptyList();
		}

		return transitions.getOrDefault(state, Collections.emptyList());
	}
}