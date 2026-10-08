package engine.gal.aut;

import java.util.List;

public interface iTransitions {

	List<Transition> get(State state);

	void add(Transition transition);
}