// = Circle =
package engine.shape;

import engine.geometry.ISU;
import engine.shape.Bounding.SimpleBox;

public class Circle extends Shape {

	double radius;

	// CONSTRUCTOR

	public Circle(ISU.Coord center, double radius) {
		super(center);
		this.radius = radius;

	}

	// ACCESSORS (for debug rendering)

	public ISU.Coord center() {
		return center;
	}

	public double radius() {
		return radius;
	}

	// INTERSECTION
	@Override
	public boolean intersects(Rect rect) {
		return rect.intersects(this);
	}

	@Override
	public boolean intersects(Circle circle) {
		if (this.center.distanceTo(circle.center) <= this.radius + circle.radius)
			return true;
		return false;
	}

	@Override
	public boolean intersects(iShape shape) {
		return shape.intersects(this);
	}

	@Override
	public SimpleBox boundingBox() {
		double minX = center.x() - this.radius;
		double maxX = center.x() + this.radius;
		double minY = center.y() - this.radius;
		double maxY = center.y() + this.radius;
		return new SimpleBox(minX, maxX, minY, maxY);
	}

}