package engine.gal.condition;

import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;
import game.Game;

import java.util.List;

public class Closest extends GALCondition {

	private final double radius_cm;

	public Closest(Category category, Direction direction, double radius) {
		super(direction, category);
		double cell = Game.game().cmPerCell;
		if(radius > 0) {
			this.radius_cm = radius * cell;
		} else {
			this.radius_cm = Double.MAX_VALUE;
		}
	}

	public Closest(Category category) {
		this(category, null, -1);
	}

	public Closest(Category category, double radius) {
		this(category, null, radius);
	}

	@Override
	public boolean eval(Entity self) {
		if (self == null || self.center() == null) {
			return false;
		}

		List<Entity> allEntities = Game.game().model.entities;

		Entity best = null;
		double bestDist = Double.MAX_VALUE;

		for (Entity candidate : allEntities) {
			if (candidate.category() != category) {
				continue;
			}
			if (candidate == self) {
				continue;
			}
			if (candidate.dead() || candidate.center() == null) {
				continue;
			}

			double dist = self.center().distanceTo(candidate.center());

			if (dist > radius_cm) {
				continue;
			}

			if (direction != null && !isInSector(self, candidate)) {
				continue;
			}

			if (dist < bestDist) {
				bestDist = dist;
				best = candidate;
			}
		}

		if (best == null) {
			return false;
		}

		if (self.bot() instanceof GALBot) {
			((GALBot) self.bot()).selectedEntity(best);
		}

		return true;
	}

	private boolean isInSector(Entity self, Entity target) {
		double dx = target.center().x() - self.center().x();
		double dy = target.center().y() - self.center().y();
		double abs_dx = Math.abs(dx);
		double abs_dy = Math.abs(dy);

		if (direction == Direction.N) {
			return dy < 0 && abs_dy >= abs_dx;
		}
		if (direction == Direction.S) {
			return dy > 0 && abs_dy >= abs_dx;
		}
		if (direction == Direction.E) {
			return dx > 0 && abs_dx >= abs_dy;
		}
		if (direction == Direction.W) {
			return dx < 0 && abs_dx >= abs_dy;
		}

		if (direction.isRelative()) {

			double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
			if (targetAngle < 0) {
				targetAngle += 360;
			}

			double selfAngle = self.orientation();

			double diff = targetAngle - selfAngle;
			while (diff <= -180)
				diff += 360;
			while (diff > 180)
				diff -= 360;

			if (direction == Direction.F) {
				return Math.abs(diff) <= 45;
			}
			if (direction == Direction.B) {
				return Math.abs(diff) >= 135;
			}
			if (direction == Direction.R) {
				return diff > 45 && diff < 135;
			}
			if (direction == Direction.L) {
				return diff < -45 && diff > -135;
			}
		}

		return true;
	}
}