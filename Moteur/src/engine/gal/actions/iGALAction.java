package engine.gal.actions;

import engine.entity.Entity;

public interface iGALAction {

	/**
	 * @apiNote asks the action to start execution
	 * @param e = the entity which performs the action
	 * @return true if the action can be started
	 */
	boolean exec(Entity e);

}