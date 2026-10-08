package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.geometry.ISU;
import engine.shape.Circle;
import engine.shape.Rect;

public class Projectile extends Entity {

	public enum Type {
		LASER, BLUE_ORB, BOSS_ORB
	}

	private static final double ORB_MIN_DIAMETER_IN_CELL = 0.7;
	private static final double ORB_MAX_DIAMETER_IN_CELL = 2.2;
	private static final double ORB_GROWTH_RATIO = 0.08;

	private final Type type;
	private final ISU.Coord spawnCenter;

	public Projectile(ISU.Coord center, ISU.Vector speed) {
		this(center, speed, Type.LASER);
	}

	public Projectile(ISU.Coord center, ISU.Vector speed, Type type) {
		super("Projectile");

		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}

		double cell = CM_PER_CELL;

		this.type = type;
		this.spawnCenter = center.mkCopy();

		if (type == Type.BLUE_ORB) {
			double diameter = ORB_MIN_DIAMETER_IN_CELL * cell;
			setSize(ISU.new Dimension(diameter, diameter));
		} else {
			setSize(ISU.new Dimension(cell, 0.6 * cell));
		}

		setStep(ISU.new Dimension(cell, cell));
		category(Category.Projectile);

		place(center);
		setLinearSpeed(speed);
	}

	public Type type() {
		return type;
	}

	public boolean isBlueOrb() {
		return type == Type.BLUE_ORB;
	}

	public void updatePowerAnimation() {
		if (!isBlueOrb() || center() == null) {
			return;
		}

		double cell = CM_PER_CELL;
		double minDiameter = ORB_MIN_DIAMETER_IN_CELL * cell;
		double maxDiameter = ORB_MAX_DIAMETER_IN_CELL * cell;

		double distance = spawnCenter.distanceTo(center());
		double diameter = Math.min(maxDiameter, minDiameter + distance * ORB_GROWTH_RATIO);

		setSize(ISU.new Dimension(diameter, diameter));
		setBounding();
		deploy();
	}

	@Override
	protected void setBounding() {
		clearBounding();

		if (isBlueOrb()) {
			addBounding(new Circle(center(), size().x() / 2.0));
		} else {
			addBounding(new Rect(center(), size(), orientation()));
		}
	}
}