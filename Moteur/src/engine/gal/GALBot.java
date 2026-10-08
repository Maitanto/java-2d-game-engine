package engine.gal;

import engine.entity.Entity;
import engine.gal.aut.Automaton;

public class GALBot extends Bot {

	private Entity selectedEntity;

	public GALBot(Entity entity) {
		super(entity);
		this.selectedEntity = entity;
	}

	// ENTITY SELECTED BY CONDITION

	public void selectedEntity(Entity entity) {
		this.selectedEntity = entity;
	}

	public Entity selected() {
		return selectedEntity;
	}

	// AUTOMATON

	/**
	 * @apiNote change the automaton of the Bot
	 * @implNote when changing automaton, the current state is reset.
	 */
	public void set(Automaton automaton) {
		automaton(automaton);
		state(null);
	}
}