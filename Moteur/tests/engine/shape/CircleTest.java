package engine.shape;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.geometry.ISU;
import game.Game;

class CircleTest {

	private static final int WIDTH_NCELL = 100;
	private static final int HEIGHT_NCELL = 100;

	private ISU isu;
	private Game game;

	@BeforeEach
	void setUp() {
		this.game = new Game(WIDTH_NCELL, HEIGHT_NCELL);
		this.isu = game.isu;
	}

	private Circle circle(double x, double y, double radius) {
		return new Circle(isu.new Coord(x, y), radius);
	}

	private Rect rect(double x, double y, double width, double height, int angleDegree) {
		return new Rect(isu.new Coord(x, y), isu.new Dimension(width, height), angleDegree);
	}

	// =========================
	// Circle / Circle
	// =========================

	@Test
	void sameCircleIntersects() {
		Circle c1 = circle(50, 50, 5);
		Circle c2 = circle(50, 50, 5);

		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
	}

	@Test
	void overlappingCirclesIntersect() {
		Circle c1 = circle(50, 50, 5);
		Circle c2 = circle(58, 50, 5);

		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
	}

	@Test
	void circleInsideAnotherIntersects() {
		Circle big = circle(50, 50, 10);
		Circle small = circle(52, 50, 2);

		assertTrue(big.intersects(small));
		assertTrue(small.intersects(big));
	}

	@Test
	void separatedCirclesDoNotIntersect() {
		Circle c1 = circle(50, 50, 5);
		Circle c2 = circle(61, 50, 5);

		assertFalse(c1.intersects(c2));
		assertFalse(c2.intersects(c1));
	}

	@Test
	void circlesTouchingExactlyIntersect() {
		Circle c1 = circle(50, 50, 5);
		Circle c2 = circle(60, 50, 5);

		assertTrue(c1.intersects(c2));
		assertTrue(c2.intersects(c1));
	}

	// =========================
	// Symmetry through iShape
	// =========================

	@Test
	void intersectsThroughIShapeWithCircle() {
		Circle c1 = circle(50, 50, 5);
		Circle c2 = circle(58, 50, 5);

		iShape s1 = c1;
		iShape s2 = c2;

		assertTrue(s1.intersects(s2));
		assertTrue(s2.intersects(s1));
	}

	// =========================
	// Circle / Rect dispatch
	// =========================

	@Test
	void circleInsideRectIntersects() {
		Circle c = circle(50, 50, 1);
		Rect r = rect(50, 50, 10, 10, 0);

		assertTrue(c.intersects(r));
		assertTrue(r.intersects(c));
	}

	@Test
	void circleTouchingRectEdgeIntersects() {
		Circle c = circle(56, 50, 1);
		Rect r = rect(50, 50, 10, 10, 0);

		assertTrue(c.intersects(r));
		assertTrue(r.intersects(c));
	}

	@Test
	void circleSeparatedFromRectDoesNotIntersect() {
		Circle c = circle(57.1, 50, 1);
		Rect r = rect(50, 50, 10, 10, 0);

		assertFalse(c.intersects(r));
		assertFalse(r.intersects(c));
	}

	@Test
	void circleTouchingRectCornerIntersects() {
		Circle c = circle(56, 56, Math.sqrt(2));
		Rect r = rect(50, 50, 10, 10, 0);

		assertTrue(c.intersects(r));
		assertTrue(r.intersects(c));
	}

	@Test
	void circleNearRotatedRectIntersects() {
		Circle c = circle(50, 57, 2);
		Rect r = rect(50, 50, 10, 10, 45);

		assertTrue(c.intersects(r));
		assertTrue(r.intersects(c));
	}
}