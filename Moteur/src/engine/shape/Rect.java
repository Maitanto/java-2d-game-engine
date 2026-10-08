// = Rect =
package engine.shape;

import engine.geometry.ISU;
import engine.shape.Bounding.SimpleBox;

public class Rect extends Shape {

	// FIELDS

	private double halfWidth, halfHeight;
	private double angle_degree;

	// CONSTRUCTOR

	public Rect(ISU.Coord center, ISU.Dimension size, double angle_degree) {
		super(center);
		this.halfHeight = size.y() / 2;
		this.halfWidth = size.x() / 2;
		this.angle_degree = (((angle_degree % 360) + 360) % 360);
	}

	// TRANSLATION ?

	// ROTATION

	void rotate(double angle_degree) {
		this.angle_degree = ((((this.angle_degree + angle_degree) % 360) + 360) % 360);
	}

	// ACCESSORS (for debug rendering)

	public ISU.Coord center() {
		return center;
	}

	public double halfWidth() {
		return halfWidth;
	}

	public double halfHeight() {
		return halfHeight;
	}

	public double angle_degree() {
		return angle_degree;
	}

	// == INTERSECTION ==
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	// === Rect/Circle Intersection ===
	public boolean intersects(Circle circle) {
		RectCircleIntersection inter = new RectCircleIntersection(this, circle);
		return inter.intersects();
	}

	private static class Point {
		double x;
		double y;

		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		void rotate(double angleDegree) {
			double angle = Math.toRadians(angleDegree);
			double oldX = this.x;
			double oldY = this.y;

			this.x = oldX * Math.cos(angle) - oldY * Math.sin(angle);
			this.y = oldX * Math.sin(angle) + oldY * Math.cos(angle);
		}

		void translate(double dx, double dy) {
			this.x += dx;
			this.y += dy;
		}
	}

	// === Helping inner class ===

	/**
	 * @implNote Principe
	 *           <UL>
	 *           <LI>translate le centre du cercle vers le repère formé par les axes
	 *           du rectangle,</LI>
	 *           <LI>redresse le repère du rectangle en annulant la rotation du
	 *           rectangle,</LI>
	 *           <LI>détermine le point <i>P</i> du rectangle le plus proche du
	 *           centre <i>C</i> du cercle de façon efficace car le rectangle est
	 *           aligné sur les axes X,Y.</LI>
	 *           </UL>
	 * @implNote Il y a intersection si distance(P,C) <= rayon du cercle</LI>
	 */

	private class RectCircleIntersection {

		// FIELDS

		private Rect outer;
		private Circle circle;

		private Point localCircleCenter;

		// CONSTRUCTOR

		RectCircleIntersection(Rect outer, Circle circle) {
			this.circle = circle;
			this.outer = outer;
			remedy();
		}

		// REMEDY means `set right an undesirable situation`

		/**
		 * @apiNote
		 * @implNote Translate virtuellement Rect et Circle dans un repère centré sur le
		 *           centre du rectangle donc les axes sont ceux du rectangle.
		 * @implNote Les coordonnées du centre du rectangle deviennent alors (0,0)
		 * @implNote On translate le centre du cercle
		 * @implNote On déplace par rotation le centre du cercle de -Rect.angle.
		 */
		void remedy() {
			double x = isu.euclideanX(outer.center.x(), circle.center.x()) - outer.center.x();
			double y = isu.euclideanY(outer.center.y(), circle.center.y()) - outer.center.y();

			this.localCircleCenter = new Point(x, y);
			this.localCircleCenter.rotate(-outer.angle_degree);
		}
		// INTERSECTION in the easy case

		boolean intersects() {
			Point closestP = closestRectPointLocal();

			double dx = this.localCircleCenter.x - closestP.x;
			double dy = this.localCircleCenter.y - closestP.y;

			return dx * dx + dy * dy <= circle.radius * circle.radius;
		}

		private Point closestRectPointLocal() {
			double px = clamp(localCircleCenter.x, -outer.halfWidth, outer.halfWidth);
			double py = clamp(localCircleCenter.y, -outer.halfHeight, outer.halfHeight);
			return new Point(px, py);
		}

		/**
		 * @apiNote POINT LE PLUS PROCHE DU CENTRE DU CERCLE
		 * @implNote on projette les coins du rectange (c1,c2) et le centre (c) du
		 *           cercle sur l'axe des <i>x<i>, la coordonnées en x du point le plus
		 *           proche est parmi {c1.x, c2.x, c.x}
		 * @implNote on projette les coins du rectange (c1,c2) et le centre (c) du
		 *           cercle sur l'axe des <i>y<i>, la coordonnées en x du point le plus
		 *           proche est parmi {c1.y, c2.y, c.y}
		 *
		 */

//		ISU.Coord closestRectpoint() {
//			double px = clamp(localCircleCenter.x, -outer.halfWidth, outer.halfWidth);
//			double py = clamp(localCircleCenter.y, -outer.halfHeight, outer.halfHeight);
//
//			Point p = new Point(px, py);
//
//			// local du rectangle -> monde
//			p.rotate(outer.angle_degree);
//			p.translate(outer.center.x(), outer.center.y());
//
//			return isu.new Coord(p.x, p.y);
//		}

		/**
		 * @return &in; {p, l, r}
		 * @implNote la position dans l'interval [l,r] la plus proche de p est :
		 * @implNote p si p &in; [l,r]
		 * @implNote l si p < l
		 * @implNote r si r < p
		 * @param p = position
		 * @param l = borne inférieure de l'interval
		 * @param r = borne supérieure de l'interval
		 */
		double clamp(double p, double l, double r) {
			if (p < l)
				return l;
			if (r < p)
				return r;
			return p;
		}

	}

	// === Rect/Rect Intersection ===
	public boolean intersects(Rect rect) {
		RectRectIntersection inter = new RectRectIntersection(this, rect);
		return inter.intersects();
	}

	// === Helping inner class ===

	private class RectRectIntersection {

		private Rect rect1;
		private Rect rect2;

		RectRectIntersection(Rect rect1, Rect rect2) {
			this.rect1 = rect1;
			this.rect2 = rect2;
		}

		boolean intersects() {
			return overlapsOnAxesOf(rect1, rect2) && overlapsOnAxesOf(rect2, rect1);
		}

		/**
		 * 
		 * @param a1
		 * @param a2
		 * @param b1
		 * @param b2
		 * @return true si les deux intervalles ne se chevauchent pas
		 */

		private boolean notOverlaps(double s1, double e1, double s2, double e2) {
			return (e1 < s2 || e2 < s1);
		}

		private boolean overlapsOnAxesOf(Rect rectR, Rect rect) {
			Point[] corners = cornersL(rect);

			// centre de rect vu depuis rectR, en coordonnées euclidiennes
			double cx = isu.euclideanX(rectR.center.x(), rect.center.x()) - rectR.center.x();
			double cy = isu.euclideanY(rectR.center.y(), rect.center.y()) - rectR.center.y();

			for (Point p : corners) {
				// local rect -> orientation monde
				p.rotate(rect.angle_degree);

				// monde relatif à rectR
				p.translate(cx, cy);

				// repère de rectR
				p.rotate(-rectR.angle_degree);
			}

			if (notOverlaps(-rectR.halfHeight, rectR.halfHeight, minY(corners), maxY(corners)))
				return false;

			if (notOverlaps(-rectR.halfWidth, rectR.halfWidth, minX(corners), maxX(corners)))
				return false;

			return true;
		}

		/**
		 * 
		 * @param rect
		 * @return les coordonnées locales du rect
		 */

		private Point[] cornersL(Rect rect) {
			return new Point[] { new Point(-rect.halfWidth, -rect.halfHeight),
					new Point(-rect.halfWidth, rect.halfHeight), new Point(rect.halfWidth, rect.halfHeight),
					new Point(rect.halfWidth, -rect.halfHeight) };
		}

		private double max(double a, double b, double c, double d) {
			return Math.max(Math.max(a, b), Math.max(c, d));
		}

		private double min(double a, double b, double c, double d) {
			return Math.min(Math.min(a, b), Math.min(c, d));
		}

		private double minX(Point[] points) {
			return min(points[0].x, points[1].x, points[2].x, points[3].x);
		}

		private double maxX(Point[] points) {
			return max(points[0].x, points[1].x, points[2].x, points[3].x);
		}

		private double minY(Point[] points) {
			return min(points[0].y, points[1].y, points[2].y, points[3].y);
		}

		private double maxY(Point[] points) {
			return max(points[0].y, points[1].y, points[2].y, points[3].y);
		}

	}

	@Override
	public SimpleBox boundingBox() {

		ISU.Coord topLeft = isu.new Coord(center.x() - this.halfWidth, center.y() + this.halfHeight);
		ISU.Coord topRight = isu.new Coord(center.x() + this.halfWidth, center.y() + this.halfHeight);
		ISU.Coord downLeft = isu.new Coord(center.x() - this.halfWidth, center.y() - this.halfHeight);
		ISU.Coord downRight = isu.new Coord(center.x() + this.halfWidth, center.y() - this.halfHeight);
		topLeft.rotateAround(center, this.angle_degree);
		topRight.rotateAround(center, this.angle_degree);
		downLeft.rotateAround(center, this.angle_degree);
		downRight.rotateAround(center, this.angle_degree);

		double minX = Math.min(Math.min(topLeft.x(), topRight.x()), Math.min(downLeft.x(), downRight.x()));
		double maxX = Math.max(Math.max(topLeft.x(), topRight.x()), Math.max(downLeft.x(), downRight.x()));
		double minY = Math.min(Math.min(topLeft.y(), topRight.y()), Math.min(downLeft.y(), downRight.y()));
		double maxY = Math.max(Math.max(topLeft.y(), topRight.y()), Math.max(downLeft.y(), downRight.y()));

		return new SimpleBox(minX, maxX, minY, maxY);
	}
}