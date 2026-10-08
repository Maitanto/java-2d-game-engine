package engine.gal.condition;

import engine.entity.Entity;
import engine.geometry.Grid;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;

public class AtStep extends GALCondition {

	private final int nbStep;

	// CONSTRUCTOR

	public AtStep(Direction direction, Category category, int nbStep) {
		super(direction, category);

		if (direction == null) {
			throw new IllegalArgumentException("direction cannot be null");
		}

		if (category == null) {
			throw new IllegalArgumentException("category cannot be null");
		}

		if (nbStep < 0) {
			throw new IllegalArgumentException("nbStep cannot be negative");
		}

		this.nbStep = nbStep;
	}

	// EVAL

	/**
	 * @apiNote check if the condition AtStep(...) is satisfied by the given entity
	 * @param e = the entity that does the evaluation
	 * @implNote AtStep(...) conditions are intensively used and must be efficient:
	 *           efficiency is perhaps more important than accuracy.
	 * @implNote There is plenty room for optimization here in collaboration with
	 *           the Model and the Bot.
	 */
	@Override
	public boolean eval(Entity e) {
		if (e == null || e.position() == null) {
			return false;
		}

		Grid grid = e.grid();

		Grid.Vector vector = directionVector(grid, e);

		int targetX = e.position().x() + vector.x() * nbStep;
		int targetY = e.position().y() + vector.y() * nbStep;

		Grid.Position targetPosition = grid.new Position(targetX, targetY);
		Grid.Cell targetCell = grid.cellAt(targetPosition);

		if (category == Category.Void) {
			return containsNoOtherEntity(targetCell, e);
			// If the entity occupies multiple cells
		}

		for (Entity entity : targetCell.entities()) {
			if (entity != e && entity.category() == category) {
				return true;
			}
		}

		return false;
	}

	private Grid.Vector directionVector(Grid grid, Entity e) {
		if (direction == Direction.H) {
			return grid.new Vector(0, 0);
		}

		if (direction == Direction.N) {
			return grid.new Vector(0, -1);
		}

		if (direction == Direction.S) {
			return grid.new Vector(0, 1);
		}

		if (direction == Direction.E) {
			return grid.new Vector(1, 0);
		}

		if (direction == Direction.W) {
			return grid.new Vector(-1, 0);
		}

		if (direction == Direction.F) {
			return vectorFromAngle(grid, e.orientation());
		}

		if (direction == Direction.B) {
			return vectorFromAngle(grid, e.orientation() + 180);
		}

		throw new IllegalStateException("Unknown direction: " + direction);
	}

	private Grid.Vector vectorFromAngle(Grid grid, double angleDegree) {
		double angleRadian = Math.toRadians(angleDegree);

		int dx = (int) Math.round(Math.cos(angleRadian));
		int dy = (int) Math.round(Math.sin(angleRadian));

		return grid.new Vector(dx, dy);
	}

	private boolean containsNoOtherEntity(Grid.Cell cell, Entity evaluator) {
		for (Entity entity : cell.entities()) {
			if (entity != evaluator) {
				return false;
			}
		}

		return true;
	}
}