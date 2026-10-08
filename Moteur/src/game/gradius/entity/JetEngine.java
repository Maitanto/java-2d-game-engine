package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Rect;

public class JetEngine extends Entity{

	public JetEngine() {
		super("JetEngine");

		double cell = CM_PER_CELL;

		setSize(ISU.new Dimension(2 * cell, 1 * cell));
		setStep(ISU.new Dimension(cell, cell));
		category(Category.Team);

		place(GRID.new Position(5, GRID.height() / 2));
	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Rect(center(), size(), orientation()));
	}

	public void placeRelativeTo(Entity anchor) {
		if (anchor == null || anchor.center() == null) {
			throw new IllegalArgumentException("anchor must be placed before placing cannon");
		}

		double cell = CM_PER_CELL;

		double dx = -1.9 * cell;
		double dy =  0 * cell ;

		place(ISU.new Coord(anchor.center().x() + dx, anchor.center().y() + dy));
	}
	
}
