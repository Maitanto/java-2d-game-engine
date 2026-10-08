package engine.shape;

import java.util.HashSet;
import java.util.Set;

public class Bounding {

	// FIELDS

	private Set<iShape> boundings;

	// CONSTRUCTOR

	public Bounding() {
		this.boundings = new HashSet<iShape>();
	}

	// BUILDER

	public void add(iShape shape) {
		if (shape == null)
			throw new IllegalArgumentException("shape cannot be null");
		this.boundings.add(shape);
	}

	// INTERSECTION

	boolean intersects(iShape shape) {
		for (iShape shape1 : this.boundings) {
			if (shape1.intersects(shape))
				return true;
		}
		return false;
	}

	public boolean intersects(Bounding bounding) {
		for (iShape shape1 : this.boundings) {
			for (iShape shape2 : bounding.boundings) {
				if (shape1.intersects(shape2))
					return true;
			}
		}
		return false;
	}

	static class SimpleBox implements iShape.Box {

		private double minX;
		private double maxX;
		private double minY;
		private double maxY;

		public SimpleBox(double minX, double maxX, double minY, double maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;

		}

		SimpleBox(iShape.Box box) {
			this.maxX = box.maxX();
			this.maxY = box.maxY();
			this.minX = box.minX();
			this.minY = box.minY();
		}

		@Override
		public double minX() {
			return this.minX;
		}

		@Override
		public double maxX() {
			return this.maxX;
		}

		@Override
		public double minY() {
			return this.minY;
		}

		@Override
		public double maxY() {
			return this.maxY;
		}

	}

	public Iterable<iShape.Box> boundingBoxes() {
		Set<iShape.Box> boxes = new HashSet<iShape.Box>();

		for (iShape shape : boundings) {
			boxes.add(new SimpleBox(shape.boundingBox()));
		}

		return boxes;
	}

	public Iterable<iShape> shapes() {
		return boundings;
	}

}