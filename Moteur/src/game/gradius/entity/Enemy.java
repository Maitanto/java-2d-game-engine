package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Rect;

public class Enemy extends Entity {
	public static int SCORE_VALUE=1;
	
	public Enemy() {
		super("Enemy");

		double cell = CM_PER_CELL;

		setSize(ISU.new Dimension(2.0 * cell, 2.0 * cell));
		setStep(ISU.new Dimension(cell, cell));
		category(Category.Adversary);

		place(GRID.new Position(50, GRID.height() / 2));
	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Rect(center(), size(), orientation()));
	}
}
