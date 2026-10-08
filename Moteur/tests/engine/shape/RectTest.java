package engine.shape;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import engine.geometry.ISU;
import game.Game;

public class RectTest {

	private static final int WIDTH_NCELL = 200;
	private static final int HEIGHT_NCELL = 200;

	private static ISU isu;
	private static Game game;

	@BeforeAll
	static void init() {
		game = new Game(WIDTH_NCELL, HEIGHT_NCELL);
		isu = game.isu;
	}

	private Rect rect(double x, double y, double width, double height, int angle) {
		return new Rect(isu.new Coord(x, y), isu.new Dimension(width, height), angle);
	}

	private Circle circle(double x, double y, double radius) {
		return new Circle(isu.new Coord(x, y), radius);
	}

	// =========================
	// Rect / Rect : cas simples
	// =========================

	@Test
	void alignedRectsIntersectWhenOverlapping() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(55, 50, 10, 10, 0);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void alignedRectsDoNotIntersectWhenSeparatedHorizontally() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(61, 50, 10, 10, 0);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void alignedRectsDoNotIntersectWhenSeparatedVertically() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(50, 61, 10, 10, 0);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void alignedRectsIntersectWhenTouchingByEdge() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(60, 50, 10, 10, 0);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void alignedRectsIntersectWhenTouchingByCorner() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(60, 60, 10, 10, 0);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void alignedRectsDoNotIntersectWithTinyGap() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(60.01, 50, 10, 10, 0);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void rectInsideAnotherRectIntersects() {
		Rect outer = rect(50, 50, 30, 30, 0);
		Rect inner = rect(50, 50, 5, 5, 0);

		assertTrue(outer.intersects(inner));
		assertTrue(inner.intersects(outer));
	}

	// =========================
	// Rect / Rect : rotations
	// =========================

	@Test
	void rotatedRectIntersectsAlignedRect() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(50, 50, 12, 2, 45);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void rotatedRectDoesNotIntersectWhenFarAway() {
		Rect a = rect(50, 50, 10, 10, 0);
		Rect b = rect(90, 90, 12, 2, 45);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void twoRotatedRectsIntersect() {
		Rect a = rect(50, 50, 12, 4, 30);
		Rect b = rect(53, 50, 12, 4, -30);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void twoRotatedRectsDoNotIntersectWhenSeparated() {
		Rect a = rect(50, 50, 12, 4, 30);
		Rect b = rect(75, 50, 12, 4, -30);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void longThinRotatedRectCrossesSquare() {
		Rect square = rect(50, 50, 10, 10, 0);
		Rect blade = rect(50, 50, 30, 2, 45);

		assertTrue(square.intersects(blade));
		assertTrue(blade.intersects(square));
	}

	@Test
	void longThinRotatedRectNearSquareButNotTouching() {
		Rect square = rect(50, 50, 10, 10, 0);
		Rect blade = rect(70, 70, 20, 2, 45);

		assertFalse(square.intersects(blade));
		assertFalse(blade.intersects(square));
	}

	// =========================
	// Rect / Rect : tore
	// =========================

	@Test
	void alignedRectsIntersectAcrossHorizontalTorusCut() {
		Rect a = rect(738, 50, 8, 8, 0);
		Rect b = rect(2, 50, 8, 8, 0);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void alignedRectsDoNotIntersectAcrossHorizontalTorusCutWhenTooFar() {
		Rect a = rect(730, 50, 4, 4, 0);
		Rect b = rect(10, 50, 4, 4, 0);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void alignedRectsIntersectAcrossVerticalTorusCut() {
		Rect a = rect(50, 738, 8, 8, 0);
		Rect b = rect(50, 2, 8, 8, 0);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void alignedRectsDoNotIntersectAcrossVerticalTorusCutWhenTooFar() {
		Rect a = rect(50, 730, 4, 4, 0);
		Rect b = rect(50, 10, 4, 4, 0);

		assertFalse(a.intersects(b));
		assertFalse(b.intersects(a));
	}

	@Test
	void rotatedRectsIntersectAcrossHorizontalTorusCut() {
		Rect a = rect(738, 50, 10, 4, 30);
		Rect b = rect(2, 50, 10, 4, -30);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	@Test
	void rotatedRectsIntersectAcrossVerticalTorusCut() {
		Rect a = rect(50, 738, 10, 4, 30);
		Rect b = rect(50, 2, 10, 4, -30);

		assertTrue(a.intersects(b));
		assertTrue(b.intersects(a));
	}

	// =========================
	// Rect / Circle
	// =========================

	@Test
	void circleInsideRectIntersects() {
		Rect r = rect(50, 50, 10, 10, 0);
		Circle c = circle(50, 50, 1);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}

	@Test
	void circleTouchingRectEdgeIntersects() {
		Rect r = rect(50, 50, 10, 10, 0);
		Circle c = circle(56, 50, 1);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}

	@Test
	void circleSeparatedFromRectDoesNotIntersect() {
		Rect r = rect(50, 50, 10, 10, 0);
		Circle c = circle(57.1, 50, 1);

		assertFalse(r.intersects(c));
		assertFalse(c.intersects(r));
	}

	@Test
	void circleTouchingRectCornerIntersects() {
		Rect r = rect(50, 50, 10, 10, 0);
		Circle c = circle(56, 56, Math.sqrt(2));

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}

	@Test
	void circleNearRectCornerButNotTouchingDoesNotIntersect() {
		Rect r = rect(50, 50, 10, 10, 0);
		Circle c = circle(56, 56, 1.3);

		assertFalse(r.intersects(c));
		assertFalse(c.intersects(r));
	}

	@Test
	void circleIntersectsRotatedRect() {
		Rect r = rect(50, 50, 20, 4, 45);
		Circle c = circle(50, 50, 2);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}

	@Test
	void circleDoesNotIntersectRotatedRectWhenFarAway() {
		Rect r = rect(50, 50, 20, 4, 45);
		Circle c = circle(70, 70, 2);

		assertFalse(r.intersects(c));
		assertFalse(c.intersects(r));
	}

	@Test
	void circleIntersectsRectAcrossHorizontalTorusCut() {
		Rect r = rect(738, 50, 8, 8, 0);
		Circle c = circle(2, 50, 2);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}

	@Test
	void circleIntersectsRectAcrossVerticalTorusCut() {
		Rect r = rect(50, 738, 8, 8, 0);
		Circle c = circle(50, 2, 2);

		assertTrue(r.intersects(c));
		assertTrue(c.intersects(r));
	}
}