package engine.gal.actions;

import engine.entity.Entity;
import engine.gal.arguments.Direction;

public class Move extends GALAction {

	private final Direction direction;
	private final int steps;
	private final double duration_ms;

	// 4 CONSTRUCTORS

	/**
	 * @apiNote asks for moving
	 *          <UL>
	 *          <LI>at celerity = intensity * maximal celerity</LI>
	 *          <LI>during duration (in ms) = n * simulationStep_ms</LI>
	 *          <LI>in the given direction</LI>
	 *          </UL>
	 * @param direction
	 * @param intensity : in [0,1]
	 * @param n         : number of action steps
	 */
	public Move(Direction direction, double intensity, int n) {
		super(intensity);

		if (direction == null) {
			throw new IllegalArgumentException("direction cannot be null");
		}

		if (n <= 0) {
			throw new IllegalArgumentException("n must be positive");
		}

		this.direction = direction;
		this.steps = n;

		// 1 step GAL = 1 tick logique de 100 ms.
		this.duration_ms = 100.0 * n;
	}

	public Move(Direction direction, double intensity) {
		this(direction, intensity, 1);
	}

	public Move(Direction direction, int n) {
		this(direction, 1.0, n);
	}

	public Move() {
		this(Direction.F, 1.0, 1);
	}

	// EXEC

	@Override
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null || e.bot().stunt() == null) {
			return false;
		}

		return e.bot().stunt().startMoving(direction, intensity, duration_ms);
	}

	public Direction direction() {
		return direction;
	}

	public int steps() {
		return steps;
	}

	public double duration() {
		return duration_ms;
	}
}