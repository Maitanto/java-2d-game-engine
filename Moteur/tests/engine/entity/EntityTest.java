package engine.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.shape.Circle;
import engine.shape.Rect;
import engine.shape.iShape;
import game.Game;

class EntityTest {

	private static class TestEntity extends Entity {

		TestEntity(String name) {
			super(name);
		}

		@Override
		protected void setBounding() {
			clearBounding();
		}
	}

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

	private static final int WIDTH_NCELL = 12;
	private static final int HEIGHT_NCELL = 10;
	private static final double EPSILON = 1e-9;

	private Game game;
	private Grid grid;
	private ISU isu;
	private Entity entity;

	@BeforeEach
	void init() {
		new Game(WIDTH_NCELL, HEIGHT_NCELL);

		this.game = Game.game();
		this.grid = game.grid;
		this.isu = game.isu;

		this.entity = new TestEntity("test-entity");
		this.entity.setStep(isu.new Dimension(2.0, 2.0));
	}

	@Test
	void setPositionShouldUpdatePositionAndCenter() {
		Grid.Position position = grid.new Position(2, 3);

		entity.setPosition(position);

		assertEquals(position.x(), entity.position().x(), "setPosition should update position.x.");
		assertEquals(position.y(), entity.position().y(), "setPosition should update position.y.");

		ISU.Coord expectedCenter = position.toISUCoord();

		assertEquals(expectedCenter.x(), entity.center().x(), EPSILON,
				"setPosition should update center.x from position.toISUCoord().");

		assertEquals(expectedCenter.y(), entity.center().y(), EPSILON,
				"setPosition should update center.y from position.toISUCoord().");
	}

	@Test
	void setCoordShouldUpdateCenterAndPosition() {
		ISU.Coord center = grid.new Position(4, 5).toISUCoord();

		entity.setCoord(center);

		assertEquals(center.x(), entity.center().x(), EPSILON, "setCoord should update center.x.");
		assertEquals(center.y(), entity.center().y(), EPSILON, "setCoord should update center.y.");

		Grid.Position expectedPosition = center.toGridPosition();

		assertEquals(expectedPosition.x(), entity.position().x(),
				"setCoord should update position.x from center.toGridPosition().");

		assertEquals(expectedPosition.y(), entity.position().y(),
				"setCoord should update position.y from center.toGridPosition().");
	}

	@Test
	void turnShouldNormalizePositiveAngles() {
		assertEquals(0, entity.orientation(), EPSILON, "Initial orientation should be 0.");

		entity.turn(90);
		assertEquals(90, entity.orientation(), EPSILON, "turn(90) from 0 should give 90.");

		entity.turn(300);
		assertEquals(30, entity.orientation(), EPSILON, "90 + 300 should be normalized to 30.");
	}

	@Test
	void turnShouldNormalizeNegativeAngles() {
		assertEquals(0, entity.orientation(), EPSILON, "Initial orientation should be 0.");

		entity.turn(-60);
		assertEquals(300, entity.orientation(), EPSILON, "turn(-60) from 0 should give 300.");

		entity.turn(-330);
		assertEquals(330, entity.orientation(), EPSILON, "300 - 330 should be normalized to 330.");
	}

	@Test
	void turnShouldNormalizeAnglesGreaterThan360() {
		assertEquals(0, entity.orientation(), EPSILON, "Initial orientation should be 0.");

		entity.turn(720);
		assertEquals(0, entity.orientation(), EPSILON, "turn(720) should keep orientation at 0.");

		entity.turn(450);
		assertEquals(90, entity.orientation(), EPSILON, "turn(450) should be equivalent to turn(90).");
	}

	@Test
	void moveEastByCentimetersShouldUpdateCenterAndPosition() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveEast(1.5);

		assertEquals(x0 + 1.5, entity.center().x(), EPSILON, "moveEast should increase x.");
		assertEquals(y0, entity.center().y(), EPSILON, "moveEast should not change y.");

		Grid.Position expectedPosition = entity.center().toGridPosition();

		assertEquals(expectedPosition.x(), entity.position().x());
		assertEquals(expectedPosition.y(), entity.position().y());
	}

	@Test
	void moveWestByCentimetersShouldUpdateCenterAndPosition() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveWest(1.5);

		assertEquals(x0 - 1.5, entity.center().x(), EPSILON, "moveWest should decrease x.");
		assertEquals(y0, entity.center().y(), EPSILON, "moveWest should not change y.");

		Grid.Position expectedPosition = entity.center().toGridPosition();

		assertEquals(expectedPosition.x(), entity.position().x());
		assertEquals(expectedPosition.y(), entity.position().y());
	}

	@Test
	void moveSouthByCentimetersShouldUpdateCenterAndPosition() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveSouth(1.5);

		assertEquals(x0, entity.center().x(), EPSILON, "moveSouth should not change x.");
		assertEquals(y0 + 1.5, entity.center().y(), EPSILON, "moveSouth should increase y.");

		Grid.Position expectedPosition = entity.center().toGridPosition();

		assertEquals(expectedPosition.x(), entity.position().x());
		assertEquals(expectedPosition.y(), entity.position().y());
	}

	@Test
	void moveNorthByCentimetersShouldUpdateCenterAndPosition() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveNorth(1.5);

		assertEquals(x0, entity.center().x(), EPSILON, "moveNorth should not change x.");
		assertEquals(y0 - 1.5, entity.center().y(), EPSILON, "moveNorth should decrease y.");

		Grid.Position expectedPosition = entity.center().toGridPosition();

		assertEquals(expectedPosition.x(), entity.position().x());
		assertEquals(expectedPosition.y(), entity.position().y());
	}

	@Test
	void moveByStepsShouldUseStepXForEastAndWest() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());
		entity.setStep(isu.new Dimension(2.0, 3.0));

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveEast(4);

		assertEquals(x0 + 8.0, entity.center().x(), EPSILON, "moveEast(4) should add 4 * step.x().");
		assertEquals(y0, entity.center().y(), EPSILON, "moveEast(4) should not change y.");

		entity.moveWest(3);

		assertEquals(x0 + 2.0, entity.center().x(), EPSILON, "moveWest(3) should remove 3 * step.x().");
		assertEquals(y0, entity.center().y(), EPSILON, "moveWest(3) should not change y.");
	}

	@Test
	void moveByStepsShouldUseStepYForNorthAndSouth() {
		entity.setCoord(grid.new Position(4, 5).toISUCoord());
		entity.setStep(isu.new Dimension(2.0, 3.0));

		double x0 = entity.center().x();
		double y0 = entity.center().y();

		entity.moveSouth(4);

		assertEquals(x0, entity.center().x(), EPSILON, "moveSouth(4) should not change x.");
		assertEquals(y0 + 12.0, entity.center().y(), EPSILON, "moveSouth(4) should add 4 * step.y().");

		entity.moveNorth(3);

		assertEquals(x0, entity.center().x(), EPSILON, "moveNorth(3) should not change x.");
		assertEquals(y0 + 3.0, entity.center().y(), EPSILON, "moveNorth(3) should remove 3 * step.y().");
	}

	@Test
	void translateGridVectorShouldUpdatePositionAndCenter() {
		entity.setPosition(grid.new Position(2, 3));

		entity.translate(grid.new Vector(4, -1));

		assertEquals(6, entity.position().x(), "Grid translation should update position.x.");
		assertEquals(2, entity.position().y(), "Grid translation should update position.y.");

		ISU.Coord expectedCenter = entity.position().toISUCoord();

		assertEquals(expectedCenter.x(), entity.center().x(), EPSILON,
				"After grid translation, center.x should be synchronized.");

		assertEquals(expectedCenter.y(), entity.center().y(), EPSILON,
				"After grid translation, center.y should be synchronized.");
	}

	@Test
	void entitiesShouldIntersectWhenOneShapePairIntersects() {
		Entity e1 = new TestEntity("e1");
		Entity e2 = new TestEntity("e2");

		e1.addBounding(new FakeShape(new FakeBox(0, 1, 0, 1), true));
		e2.addBounding(new FakeShape(new FakeBox(10, 11, 10, 11), false));

		assertTrue(e1.intersects(e2));
	}

	@Test
	void entitiesShouldNotIntersectWhenNoShapePairIntersects() {
		Entity e1 = new TestEntity("e1");
		Entity e2 = new TestEntity("e2");

		e1.addBounding(new FakeShape(new FakeBox(0, 1, 0, 1), false));
		e2.addBounding(new FakeShape(new FakeBox(10, 11, 10, 11), false));

		assertFalse(e1.intersects(e2));
	}

	@Test
	void setBoundingShouldResetEntityHitbox() {
		Entity e1 = new TestEntity("e1");
		Entity e2 = new TestEntity("e2");

		e1.addBounding(new FakeShape(new FakeBox(0, 1, 0, 1), true));
		e2.addBounding(new FakeShape(new FakeBox(10, 11, 10, 11), false));

		assertTrue(e1.intersects(e2));

		e1.setBounding();

		assertFalse(e1.intersects(e2));
	}

	@Test
	void deployWithoutBoundingShouldOccupyNoCell() {
		entity.deploy();

		assertEquals(0, entity.occupied().size(), "An entity with an empty hitbox should occupy no cell.");
	}

	@Test
	void deployShouldOccupyCellsCoveredByBoundingBox() {
		double cell = Game.game().cmPerCell;

		entity.addBounding(new FakeShape(new FakeBox(1.6 * cell, 4.4 * cell, 2.6 * cell, 4.4 * cell), false));

		entity.deploy();

		assertEquals(6, entity.occupied().size(), "Box should occupy x=2..4 and y=3..4, so 3 * 2 = 6 cells.");
	}

	@Test
	void retractShouldClearOccupiedCells() {
		double cell = Game.game().cmPerCell;

		entity.addBounding(new FakeShape(new FakeBox(1.6 * cell, 4.4 * cell, 2.6 * cell, 4.4 * cell), false));

		entity.deploy();

		assertEquals(6, entity.occupied().size());

		entity.retract();

		assertEquals(0, entity.occupied().size(), "retract should clear the occupied cells set.");
	}
}