package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Rect;

public class Power extends Entity {

	// JUSTE POUR LE TEST DOIT ETRE FINI

	public Power() {
		super("Power");

		setSize(ISU.new Dimension(2 * CM_PER_CELL, 2 * CM_PER_CELL));
		setStep(ISU.new Dimension(CM_PER_CELL, CM_PER_CELL));
		category(Category.Power);

		place(GRID.new Position(15, GRID.height() / 2));
	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Rect(center(), size(), orientation()));
	}
}