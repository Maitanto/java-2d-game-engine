package engine.gal.actions;

import engine.entity.Entity;
import engine.gal.arguments.Direction;

public class Turn extends GALAction {

	private final double angle_deg;

	// 5 CONSTRUCTORS

	/**
	 * @param angle_deg in [-360,360]
	 * @param intensity in [0,1]
	 */
	public Turn(double angle_deg, double intensity) {
		super(intensity);

		if (angle_deg < -360 || angle_deg > 360) {
			throw new IllegalArgumentException("angle_deg must be in [-360, 360]");
		}

		this.angle_deg = angle_deg;
	}

	public Turn(Direction direction) {
		this(direction.toAngle(), 1.0);
	}

	public Turn(int angle_deg) {
		this(angle_deg, 1.0);
	}

	public Turn(double intensity) {
		this(0, intensity);
	}

	public Turn(Direction direction, double intensity) {
		this(direction.toAngle(), intensity);
	}

	// EXEC

	@Override
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null || e.bot().stunt() == null) {
			return false;
		}

		return e.bot().stunt().startTurning(angle_deg, intensity);
	}

	public double angle() {
		return angle_deg;
	}
}