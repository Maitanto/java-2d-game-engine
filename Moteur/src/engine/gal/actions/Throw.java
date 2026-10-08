package engine.gal.actions;

import engine.entity.Entity;
import engine.gal.arguments.Direction;

public class Throw extends GALAction {

	private final Direction direction;

	public Throw(Direction direction, double intensity) {
		super(intensity);

		if (direction == null) {
			throw new IllegalArgumentException("direction cannot be null");
		}

		this.direction = direction;
	}

	public Throw(Direction direction) {
		this(direction, 1.0);
	}

	public Throw() {
		this(Direction.F, 1.0);
	}

	@Override
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null || e.bot().stunt() == null) {
			return false;
		}

		return e.bot().stunt().startThrowing(direction, intensity);
	}
}