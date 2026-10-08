package engine.graphics.avatars;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.shape.Circle;
import engine.shape.Rect;
import engine.shape.iShape;
import oop.graphics.Color;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;

public class ShapeAvatar extends Avatar {

	public static final Color DEFAULT_BOX_COLOR = Colors.red;

	public static final Color DEFAULT_SHAPE_COLOR = Colors.yellow;

	public static boolean drawShapes = false;

	protected ShapeAvatar(Entity entity, int multX, int multY, int z_order) {
		super(entity, multX, multY, z_order,true);
		this.colorHitbox = DEFAULT_BOX_COLOR;
		this.colorShape = DEFAULT_SHAPE_COLOR; // default Color
	}

	protected final Color colorShape;
	protected final Color colorHitbox;

	@Override
	public void paint(Graphics g) {
		paintHitbox(g);
	}

	@Override
	public void updateAnimation(double delta_t) {
		// no animation
	}

	public void paintHitbox(Graphics g) {

		if (drawShapes) {
			drawHitboxShapes(g);
		}

		ISU.Coord coord = entity.center();

		int xCenter = this.cmToPixel(coord.x());
		int yCenter = this.cmToPixel(coord.y());

		ISU.Dimension size = entity.size();

		int width = (int) (Math.max(1, this.cmToPixel(size.x())) * this.multX);
		int height = (int) (Math.max(1, this.cmToPixel(size.y())) * this.multY);

		int xTopLeft = xCenter - width / 2;
		int yTopLeft = yCenter - height / 2;

		g.setColor(colorHitbox);
		g.drawRect(xTopLeft, yTopLeft, width, height);

		double r = entity.size().x(); // radius de l'entité
		double a = Math.toRadians(entity.orientation()); // un trait dans la direction de l'orientation

		g.drawLine(xCenter, yCenter, (int) Math.round(xCenter + r * Math.cos(a)),
				(int) Math.round(yCenter + r * Math.sin(a)));

	}

	protected void drawHitboxShapes(Graphics g) {
		g.setColor(colorShape);

		for (iShape shape : entity.hitboxShapes()) {
			if (shape instanceof Rect) {
				drawRectShape(g, (Rect) shape);
			} else if (shape instanceof Circle) {
				drawCircleShape(g, (Circle) shape);
			}
		}
	}

	protected void drawRectShape(Graphics g, Rect rect) {
		ISU.Coord c = rect.center();
		double hw = rect.halfWidth();
		double hh = rect.halfHeight();
		double angle = Math.toRadians(rect.angle_degree());
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		double[] lx = { -hw, hw, hw, -hw };
		double[] ly = { -hh, -hh, hh, hh };

		int[] xs = new int[4];
		int[] ys = new int[4];
		for (int i = 0; i < 4; i++) {
			double wx = c.x() + lx[i] * cos - ly[i] * sin;
			double wy = c.y() + lx[i] * sin + ly[i] * cos;
			xs[i] = cmToPixel(wx);
			ys[i] = cmToPixel(wy);
		}
		for (int i = 0; i < 4; i++) {
			int j = (i + 1) % 4;
			g.drawLine(xs[i], ys[i], xs[j], ys[j]);
		}
	}

	protected void drawCircleShape(Graphics g, Circle circle) {
		ISU.Coord c = circle.center();
		int cx = cmToPixel(c.x());
		int cy = cmToPixel(c.y());
		int rPix = Math.max(1, cmToPixel(circle.radius()));

		g.drawOval(cx - rPix, cy - rPix, rPix * 2, rPix * 2);
	}
}