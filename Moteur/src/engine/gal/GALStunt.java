package engine.gal;

import java.util.List;

import engine.entity.Entity;
import engine.gal.arguments.Direction;
import engine.move.Model;
import engine.move.Stunt;
import game.Game;

public class GALStunt extends Stunt implements iAllGALActions {

	private double step_cm;
	private double max_cmPer_s;
	private double max_degPer_s;

	protected double action_ms;

	private boolean turning;
	private double finalAngle;

	private ThrowSpawner projectileSpawner;

	public GALStunt(Model model, Entity entity) {
		super(model, entity);

		this.step_cm = Game.game().cmPerCell;
		this.max_cmPer_s = 20.0;
		this.max_degPer_s = 180.0;

		this.action_ms = 0.0;
		this.turning = false;

		model.setStunt(entity, this);

		if (entity.bot() != null) {
			entity.bot().stunt(this);
		}
	}

	// STEP

	public void setStepLength(double cm) {
		if (cm <= 0.0) {
			throw new IllegalArgumentException("step length must be positive");
		}

		this.step_cm = cm;
	}

	public double stepLength() {
		return step_cm;
	}

	// SPEED

	public void setMaxLinearSpeed(double cmPer_s) {
		if (cmPer_s < 0.0) {
			throw new IllegalArgumentException("linear speed cannot be negative");
		}

		this.max_cmPer_s = cmPer_s;
	}

	public void setMaxAngularSpeed(double degPer_s) {
		if (degPer_s < 0.0) {
			throw new IllegalArgumentException("angular speed cannot be negative");
		}

		this.max_degPer_s = degPer_s;
	}

	// ACTION DURATION

	public double actionDuration() {
		return action_ms;
	}

	// TICK

	/**
	 * @apiNote The tick regularly provides the elapsed time in milliseconds.
	 */
	public void tick(double elapsed_s) {
		double elapsed_ms = elapsed_s * 1000.0;
		if (action_ms > 0.0) {
			action_ms -= elapsed_ms;

			if (action_ms <= 0.0) {
				finishAction();
			}

			return;
		}

		if (entity.bot() != null) {
			entity.bot().tick(elapsed_ms);
		}
	}

	protected void finishAction() {
		action_ms = 0.0;

		setLinearSpeed(Game.game().isu.new Vector(0.0, 0.0));
		setAngularSpeed(0);

		if (turning) {
			entity.forceOrientation(Math.round(finalAngle));
			turning = false;
		}

		if (entity.bot() != null) {
			entity.bot().completed();
		}
	}

	private void stopActionWithoutCompleted() {
		action_ms = 0.0;
		turning = false;

		setLinearSpeed(Game.game().isu.new Vector(0.0, 0.0));
		setAngularSpeed(0);
	}

	// MOVE

	@Override
	public boolean startMoving(Direction direction, double intensity, double duration_ms) {
		if (direction == null) {
			return false;
		}

		if (intensity < 0.0 || intensity > 1.0) {
			return false;
		}

		if (duration_ms <= 0.0) {
			return false;
		}

		if (action_ms > 0.0) {
			return false;
		}

		double angle = angleFromDirection(direction);
		double speed = max_cmPer_s * intensity;

		double rad = Math.toRadians(angle);
		double vx = Math.cos(rad) * speed;
		double vy = Math.sin(rad) * speed;

		setLinearSpeed(Game.game().isu.new Vector(vx, vy));

		action_ms = duration_ms;
		turning = false;

		return true;
	}

	private double angleFromDirection(Direction direction) {
		if (direction == Direction.F) {
			return entity.orientation();
		}

		if (direction == Direction.B) {
			return normalizeAngle(entity.orientation() + 180.0);
		}

		if (direction == Direction.L) {
			return normalizeAngle(entity.orientation() - 22.5);
		}

		if (direction == Direction.R) {
			return normalizeAngle(entity.orientation() + 22.5);
		}

		if (direction == Direction.H) {
			return entity.orientation();
		}

		return direction.toAngle();
	}

	// TURN

	@Override
	public boolean startTurning(double angle_deg, double intensity) {
		if (intensity < 0.0 || intensity > 1.0) {
			return false;
		}

		if (max_degPer_s <= 0.0 || intensity == 0.0) {
			return false;
		}

		if (action_ms > 0.0) {
			return false;
		}

		finalAngle = normalizeAngle(angle_deg);

		double delta = shortestDelta(entity.orientation(), finalAngle);

		if (Math.abs(delta) < 1e-9) {
			return true;
		}

		double angularSpeed = max_degPer_s * intensity;

		if (delta < 0.0) {
			angularSpeed = -angularSpeed;
		}

		setAngularSpeed((int) Math.round(angularSpeed));

		action_ms = Math.abs(delta) / Math.abs(angularSpeed) * 1000.0;
		turning = true;

		return true;
	}

	private double normalizeAngle(double angle) {
		double normalized = angle % 360.0;

		if (normalized < 0.0) {
			normalized += 360.0;
		}

		return normalized;
	}

	private double shortestDelta(double currentAngle, double targetAngle) {
		double delta = normalizeAngle(targetAngle) - normalizeAngle(currentAngle);

		if (delta > 180.0) {
			delta -= 360.0;
		}

		if (delta < -180.0) {
			delta += 360.0;
		}

		return delta;
	}

	// COLLISION

	@Override
	protected void collision(Entity other) {
		stopActionWithoutCompleted();

		if (entity.bot() != null) {
			entity.bot().collision(other, 0.0);
		}
	}

	@Override
	protected void collision(List<Entity> entities) {
		for (Entity other : entities) {
			collision(other);
		}
	}

	// FIRE

	public void setProjectileSpawner(ThrowSpawner projectileSpawner) {
		this.projectileSpawner = projectileSpawner;
	}

	public ThrowSpawner projectileSpawner() {
		return projectileSpawner;
	}

	@Override
	public boolean startThrowing(Direction direction, double intensity) {
		return false;
	}

	@Override
	public boolean startDashing(Direction direction, double intensity) {
		return false;
	}

}