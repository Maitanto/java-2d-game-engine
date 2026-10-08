package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Circle;


public class BossProjectile extends Entity {
	public BossProjectile() {
		super("BossProjectile");
		setSize(ISU.new Dimension(2*CM_PER_CELL, 2*CM_PER_CELL));
		setStep(ISU.new Dimension(CM_PER_CELL, CM_PER_CELL));
		category(Category.Adversary);
		place(GRID.new Position(0, GRID.height() / 2));
	}

	@Override
	protected void setBounding() {
		clearBounding();
		addBounding(new Circle(center(), size().x()/2));
	}
}