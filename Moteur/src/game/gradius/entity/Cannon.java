package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.shape.Rect;

public class Cannon extends Entity {

	private final CannonSlot slot;
	private Ship owner;

	public Cannon(CannonSlot slot) {
		super(slot.name() + "Cannon");

		this.slot = slot;

		double cell = CM_PER_CELL;

		setSize(ISU.new Dimension(0.9 * cell, 0.30 * cell));
		setStep(ISU.new Dimension(cell, cell));
		category(Category.Void);

		place(GRID.new Position(5, GRID.height() / 2));
	}

	public CannonSlot slot() {
		return slot;
	}

	public void attachTo(Ship owner) {
		if (owner == null) {
			throw new IllegalArgumentException("owner cannot be null");
		}

		this.owner = owner;
	}

	public Ship owner() {
		return owner;
	}

	public void placeRelativeTo(Entity anchor) {
		this.placeRelativeTo(anchor, 0.65 * CM_PER_CELL, slot.isTop() ? -2 * CM_PER_CELL : 2 * CM_PER_CELL);
	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Rect(center(), size(), orientation()));
	}
}