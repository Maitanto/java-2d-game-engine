package engine.gal;

import engine.entity.Entity;
import engine.gal.actions.iGALAction;
import engine.gal.aut.Automaton;
import engine.gal.aut.State;

public class Bot {

	private final Entity entity;

	private Automaton automaton;
	private GALStunt stunt;

	private State state;
	private Entity impactor;

	private iGALAction lastAction;

	/**
	 * @apiNote 0 <= life <= 3
	 */
	private int life;

	private double timer_ms;

	// CONSTRUCTOR

	public Bot(Entity entity) {
		if (entity == null) {
			throw new IllegalArgumentException("entity cannot be null");
		}

		this.entity = entity;
		this.life = 3;
		this.timer_ms = 0.0;
	}

	// ENTITY

	public Entity entity() {
		return entity;
	}

	// AUTOMATON

	public void automaton(Automaton automaton) {
		this.automaton = automaton;
	}

	public Automaton automaton() {
		return automaton;
	}

	// STUNT

	public void stunt(GALStunt stunt) {
		this.stunt = stunt;
	}

	public GALStunt stunt() {
		return stunt;
	}

	// STATE

	/**
	 * @apiNote The state can be used
	 *          <UL>
	 *          <LI>to drive the automaton</LI>
	 *          <LI>to select the appropriate avatar</LI>
	 *          </UL>
	 * @return the state of mind of the Bot
	 */
	public State state() {
		return state;
	}

	public void state(State state) {
		this.state = state;
	}

	/**
	 * @return the last action performed by the bot, or null if none yet
	 */
	public iGALAction lastAction() {
		return lastAction;
	}

	/**
	 * Records the last action performed (called when a transition fires).
	 *
	 * @param action the action just executed
	 */
	public void lastAction(iGALAction action) {
		this.lastAction = action;
	}

	// HEALTH

	public int life() {
		return life;
	}

	public void healthPercent(int healthPercent) {
		if (healthPercent < 0 || healthPercent > 100) {
			throw new IllegalArgumentException("healthPercent must be in [0, 100]");
		}

		this.life = healthPercent;
	}
	
	public void sethealth(int health) {
		if (health < 0) {
			throw new IllegalArgumentException("health must be possitive !");
		}

		this.life = health;
	}

	public Entity impactor() {
		return impactor;
	}

	// TICK & COLLISION & COMPLETED

	/**
	 * @apiNote wakes up the Bot so that it can take action
	 * @param elapsed_ms
	 */
	public void tick(double elapsed_ms) {
		updateTimer(elapsed_ms);
		stepAutomaton();
	}

	/**
	 * @apiNote notifies the Bot that a collision has occurred with {@code impactor}
	 *          after {@code elapsed_ms} so that the Bot can take action
	 * @param impactor
	 * @param elapsed_ms
	 */
	public void collision(Entity impactor, double elapsed_ms) {
		updateTimer(elapsed_ms);
		this.impactor = impactor;
		stepAutomaton();
		this.impactor = null;
	}

	/**
	 * @apiNote notifies the Bot that the action of its Stunt is completed.
	 */
	public void completed() {
		stepAutomaton();
	}

	private boolean stepAutomaton() {
		if (automaton == null) {
			return false;
		}

		return automaton.step(entity);
	}

	// TIMER

	public void startTimer(double duration_ms) {
		if (duration_ms < 0.0) {
			throw new IllegalArgumentException("duration_ms cannot be negative");
		}

		this.timer_ms = duration_ms;
	}

	public boolean timerExpired() {
		return timer_ms <= 0.0;
	}

	private void updateTimer(double elapsed_ms) {
		if (elapsed_ms < 0.0) {
			throw new IllegalArgumentException("elapsed_ms cannot be negative");
		}

		if (timer_ms > 0.0) {
			timer_ms = Math.max(0.0, timer_ms - elapsed_ms);
		}
	}
}