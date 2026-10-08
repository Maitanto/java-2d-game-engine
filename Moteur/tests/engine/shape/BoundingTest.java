package engine.shape;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

class BoundingTest {

	private static class FakeBox implements iShape.Box {
		private final double minX;
		private final double maxX;
		private final double minY;
		private final double maxY;

		FakeBox(double minX, double maxX, double minY, double maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}

		@Override
		public double minX() {
			return minX;
		}

		@Override
		public double maxX() {
			return maxX;
		}

		@Override
		public double minY() {
			return minY;
		}

		@Override
		public double maxY() {
			return maxY;
		}
	}

	private static class FakeShape implements iShape {
		private final iShape.Box box;
		private final boolean intersects;

		FakeShape(iShape.Box box, boolean intersects) {
			this.box = box;
			this.intersects = intersects;
		}

		@Override
		public boolean intersects(iShape shape) {
			return intersects;
		}

		@Override
		public boolean intersects(Circle circle) {
			return intersects;
		}

		@Override
		public boolean intersects(Rect rect) {
			return intersects;
		}

		@Override
		public iShape.Box boundingBox() {
			return box;
		}
	}

	@Test
	void addNullShapeThrowsException() {
		Bounding bounding = new Bounding();

		assertThrows(IllegalArgumentException.class, () -> {
			bounding.add(null);
		});
	}

	@Test
	void boundingBoxesReturnsOneBoxPerShape() {
		Bounding bounding = new Bounding();

		bounding.add(new FakeShape(new FakeBox(0, 10, 0, 10), false));
		bounding.add(new FakeShape(new FakeBox(20, 30, 40, 50), false));

		Iterator<iShape.Box> it = bounding.boundingBoxes().iterator();

		int count = 0;

		while (it.hasNext()) {
			it.next();
			count++;
		}

		assertEquals(2, count);
	}

	@Test
	void intersectsShapeReturnsTrueIfOneInternalShapeIntersects() {
		Bounding bounding = new Bounding();

		bounding.add(new FakeShape(new FakeBox(0, 10, 0, 10), false));
		bounding.add(new FakeShape(new FakeBox(20, 30, 20, 30), true));

		iShape other = new FakeShape(new FakeBox(5, 6, 5, 6), false);

		assertTrue(bounding.intersects(other));
	}

	@Test
	void intersectsShapeReturnsFalseIfNoInternalShapeIntersects() {
		Bounding bounding = new Bounding();

		bounding.add(new FakeShape(new FakeBox(0, 10, 0, 10), false));
		bounding.add(new FakeShape(new FakeBox(20, 30, 20, 30), false));

		iShape other = new FakeShape(new FakeBox(5, 6, 5, 6), false);

		assertFalse(bounding.intersects(other));
	}

	@Test
	void intersectsBoundingReturnsTrueIfAtLeastOnePairIntersects() {
		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		b1.add(new FakeShape(new FakeBox(0, 10, 0, 10), false));
		b1.add(new FakeShape(new FakeBox(20, 30, 20, 30), true));

		b2.add(new FakeShape(new FakeBox(100, 110, 100, 110), false));

		assertTrue(b1.intersects(b2));
	}

	@Test
	void intersectsBoundingReturnsFalseIfNoPairIntersects() {
		Bounding b1 = new Bounding();
		Bounding b2 = new Bounding();

		b1.add(new FakeShape(new FakeBox(0, 10, 0, 10), false));
		b1.add(new FakeShape(new FakeBox(20, 30, 20, 30), false));

		b2.add(new FakeShape(new FakeBox(100, 110, 100, 110), false));

		assertFalse(b1.intersects(b2));
	}

	@Test
	void simpleBoxCopyKeepsAllCoordinates() {
		iShape.Box source = new FakeBox(1, 2, 3, 4);

		Bounding.SimpleBox copy = new Bounding.SimpleBox(source);

		assertEquals(1, copy.minX(), 1e-9);
		assertEquals(2, copy.maxX(), 1e-9);
		assertEquals(3, copy.minY(), 1e-9);
		assertEquals(4, copy.maxY(), 1e-9);
	}
}