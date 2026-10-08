package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.geometry.ISU;
import engine.shape.Rect;

public class Mandibule extends Entity {

	private int nbSquaresForCollision;

	private static final double OPEN_DEGREES_PER_UNIT = 3.0;

	public final boolean isTop;

	protected Boss owner;

	public Mandibule(Boss owner, boolean isTop) {
		super(((isTop) ? "Top" : "Bottom") + " mandibule");
		this.isTop = isTop;
		this.owner = owner;

		setSize(ISU.new Dimension(32 * CM_PER_CELL, 12 * CM_PER_CELL));
		setStep(ISU.new Dimension(CM_PER_CELL, CM_PER_CELL));
		category(Category.Adversary);

		place(GRID.new Position(5, GRID.height() / 2));

		nbSquaresForCollision = 16; // doit être un nombre positif >=1. Détermine la précision des collisions de la
									// mandibule.
	}

	public Boss owner() {
		return owner;
	}

	public void placeRelativeTo(Entity anchor) {
		double openDegrees = owner.openAmount * OPEN_DEGREES_PER_UNIT;
		setOrientation(isTop ? openDegrees : -openDegrees);

		this.placeRelativeTo(anchor, -16 * CM_PER_CELL,
				isTop ? -owner.openAmount * CM_PER_CELL : owner.openAmount * CM_PER_CELL);
	}

	@Override
	protected void setBounding() {
		clearBounding();

		double w = size().x();
		double h = size().y();
		double cx = center().x();
		double cy = center().y();

		// The sprite only fills a thin diagonal of the size box, not the whole
		// rectangle. So instead of one big rect, we can add several smaller rectangles
		// at the diagonal.
		// Attention! C'est plus une courbe en cloche qu'une diagonale parfaite.
		double side = h / (nbSquaresForCollision / 2);

		double localStartX = -w / 2.0 + side / 2.0;
		double localEndX = w / 2.0 - side / 2.0;
		double localStartY = isTop ? (h / 2.0 - side / 2.0) : (-h / 2.0 + side / 2.0);
		double localEndY = isTop ? (-h / 2.0 + side / 2.0) : (h / 2.0 - side / 2.0);

		double angle = Math.toRadians(orientation());
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);

		ISU.Dimension squareSize = ISU.new Dimension(side, side);

		double curveAmplitude = h / 4;

		for (int i = 0; i < nbSquaresForCollision; i++) {
			double t = (nbSquaresForCollision == 1) ? 0.0 : (double) i / (nbSquaresForCollision - 1);

			double lx = localStartX + t * (localEndX - localStartX);
			double ly = localStartY + t * (localEndY - localStartY);

			// Calcul de l'offset en cloche
			double curveOffset = Math.sin(t * Math.PI) * curveAmplitude;

			if (isTop) {
				ly -= curveOffset;
			} else {
				ly += curveOffset;
			}

			double wx = cx + lx * cos - ly * sin;
			double wy = cy + lx * sin + ly * cos;

			addBounding(new Rect(ISU.new Coord(wx, wy), squareSize, orientation()));
		}
	}

}