package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Rect;

public class Obstacle extends Entity {

	public Obstacle(int x_ncell, int y_ncell) {
		super("Obstacle");


		setSize(ISU.new Dimension(CM_PER_CELL, CM_PER_CELL));
		category(Category.Obstacle);
		place(GRID.new Position(x_ncell, y_ncell));

	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Rect(center(), size(), orientation()));
	}
}
