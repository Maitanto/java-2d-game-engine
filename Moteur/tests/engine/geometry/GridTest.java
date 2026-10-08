package engine.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.Game;

class GridTest {

	private static final int WIDTH_NCELL = 12;
	private static final int HEIGHT_NCELL = 10;
	private static final double EPS = 1e-9;

	private Game game;
	private Grid grid;
	private ISU isu;

	private double cmPerCell;

	@BeforeEach
	void setUp() {
		game = new Game(WIDTH_NCELL, HEIGHT_NCELL);
		grid = game.grid;
		isu = game.isu;
		cmPerCell = game.cmPerCell;

		assertNotNull(grid);
		assertNotNull(isu);
		assertTrue(cmPerCell > 0);
	}

	private Grid.Position pos(int xCell, int yCell) {
		return grid.new Position(xCell, yCell);
	}

	private Grid.Vector vec(int xCell, int yCell) {
		return grid.new Vector(xCell, yCell);
	}

	private Grid.Dimension dim(int xCell, int yCell) {
		return grid.new Dimension(xCell, yCell);
	}

	private void assertPositionEquals(int expectedXCell, int expectedYCell, Grid.Position actual) {
		assertAll(() -> assertEquals(expectedXCell, actual.x()), () -> assertEquals(expectedYCell, actual.y()));
	}

	private int wrapCell(int value, int perimeterInCells) {
		int r = value % perimeterInCells;
		return r < 0 ? r + perimeterInCells : r;
	}

	// ============================================================
	// GRID : toutes les coordonnées sont en cellules
	// ============================================================

	@Test
	@DisplayName("Grid should keep the dimensions given by Game, in cells")
	void gridShouldKeepGameDimensionsInCells() {
		assertEquals(WIDTH_NCELL, grid.width());
		assertEquals(HEIGHT_NCELL, grid.height());
	}

	@Test
	@DisplayName("Every valid grid position should have a non-null cell")
	void everyValidPositionShouldHaveCell() {
		for (int yCell = 0; yCell < HEIGHT_NCELL; yCell++) {
			for (int xCell = 0; xCell < WIDTH_NCELL; xCell++) {
				assertNotNull(grid.cellAt(pos(xCell, yCell)), "Missing cell at (" + xCell + "," + yCell + ")");
			}
		}
	}

	@Test
	@DisplayName("cellAt should normalize positions using the grid perimeter in cells")
	void cellAtShouldNormalizePositionWithCellUnits() {
		Grid.Cell origin = grid.cellAt(pos(0, 0));

		Grid.Cell wrappedRight = assertDoesNotThrow(() -> grid.cellAt(pos(WIDTH_NCELL, 0)));
		Grid.Cell wrappedLeft = assertDoesNotThrow(() -> grid.cellAt(pos(-1, 0)));
		Grid.Cell wrappedTop = assertDoesNotThrow(() -> grid.cellAt(pos(0, -1)));
		Grid.Cell wrappedBottom = assertDoesNotThrow(() -> grid.cellAt(pos(0, HEIGHT_NCELL)));

		assertEquals(origin.position, wrappedRight.position);
		assertPositionEquals(WIDTH_NCELL - 1, 0, wrappedLeft.position);
		assertPositionEquals(0, HEIGHT_NCELL - 1, wrappedTop.position);
		assertEquals(origin.position, wrappedBottom.position);
	}

	@Test
	@DisplayName("Dimension.normalize should wrap coordinates with cell units")
	void dimensionNormalizeShouldWrapWithCellUnits() {
		Grid.Dimension d = dim(WIDTH_NCELL + 3, HEIGHT_NCELL + 4);

		assertEquals(3, d.x());
		assertEquals(4, d.y());
	}

	@Test
	@DisplayName("Dimension.normalize should wrap negative coordinates with cell units")
	void dimensionNormalizeShouldWrapNegativeCoordinatesWithCellUnits() {
		Grid.Dimension d = dim(-1, -2);

		assertEquals(WIDTH_NCELL - 1, d.x());
		assertEquals(HEIGHT_NCELL - 2, d.y());
	}

	@Test
	@DisplayName("Dimension equality should compare canonical cell coordinates")
	void dimensionEqualsShouldCompareCanonicalCellCoordinates() {
		assertEquals(dim(4, 4), dim(WIDTH_NCELL + 4, HEIGHT_NCELL + 4));
		assertNotEquals(dim(4, 4), dim(4, 5));
		assertNotEquals(dim(4, 4), "not a dimension");
	}

	@Test
	@DisplayName("Vector.add should add cell components without normalizing")
	void vectorAddShouldAddWithoutNormalizing() {
		Grid.Vector v = vec(WIDTH_NCELL - 1, HEIGHT_NCELL - 1);

		v.add(vec(1, 2));

		assertEquals(WIDTH_NCELL, v.x());
		assertEquals(HEIGHT_NCELL + 1, v.y());
	}

	@Test
	@DisplayName("Vector equality should compare raw vector components")
	void vectorEqualsShouldCompareRawComponents() {
		assertEquals(vec(1, -2), vec(1, -2));
		assertNotEquals(vec(1, -2), vec(WIDTH_NCELL + 1, HEIGHT_NCELL - 2));
		assertNotEquals(vec(1, -2), "not a vector");
	}

	@Test
	@DisplayName("Position.translate should translate by a vector in cells and normalize the position")
	void positionTranslateShouldTranslateAndNormalizeWithCellUnits() {
		Grid.Position p = pos(WIDTH_NCELL - 1, HEIGHT_NCELL - 1);

		p.translate(vec(1, 2));

		assertPositionEquals(0, 1, p);
	}

	@Test
	@DisplayName("moveNorth should move in cells and wrap from top to bottom")
	void moveNorthShouldWrapFromTopToBottom() {
		Grid.Position p = pos(3, 0);

		p.moveNorth(1);

		assertPositionEquals(3, HEIGHT_NCELL - 1, p);
	}

	@Test
	@DisplayName("Position.copy should create an independent equal position")
	void positionCopyShouldBeIndependent() {
		Grid.Position p = pos(2, 3);
		Grid.Position copy = p.copy();

		assertEquals(p, copy);
		assertNotSame(p, copy);

		copy.translate(vec(1, 0));

		assertPositionEquals(2, 3, p);
		assertPositionEquals(3, 3, copy);
	}

	@Test
	@DisplayName("Position.distanceTo should compute a distance in cells")
	void distanceShouldUseCellUnitsAndShortestPathOnTorus() {
		assertEquals(1.0, pos(0, 0).distanceTo(pos(WIDTH_NCELL - 1, 0)), EPS);
		assertEquals(1.0, pos(0, 0).distanceTo(pos(0, HEIGHT_NCELL - 1)), EPS);
		assertEquals(Math.sqrt(2.0), pos(0, 0).distanceTo(pos(WIDTH_NCELL - 1, HEIGHT_NCELL - 1)), EPS);
	}

	@Test
	@DisplayName("Rotation around a grid position should work in cells")
	void rotationAroundShouldUseCellUnits() {
		Grid.Position center = pos(5, 5);
		Grid.Position p = pos(6, 5);

		double before = p.distanceTo(center);
		p.rotateAround(center, 90);
		double after = p.distanceTo(center);

		assertEquals(before, after, EPS);
		assertPositionEquals(5, 6, p);
	}

	@Test
	@DisplayName("Cell should add, contain and remove entities")
	void cellShouldAddContainAndRemoveEntity() {
		Grid.Cell cell = grid.cellAt(pos(0, 0));

		assertFalse(cell.contains(null));

		cell.add(null);
		assertTrue(cell.contains(null));

		cell.remove(null);
		assertFalse(cell.contains(null));
	}

	// ============================================================
	// CONVERSIONS GRID <-> ISU
	// Convention : le centre de la case 0 est 0.0
	// ============================================================

	@Test
	@DisplayName("Grid.Dimension -> ISU.Dimension should multiply cells by cmPerCell")
	void gridDimensionToISUDimensionShouldScaleByCmPerCell() {
		Grid.Dimension d = dim(2, 3);

		ISU.Dimension isuDim = d.toISUDimension();

		assertNotNull(isuDim);
		assertEquals(2 * cmPerCell, isuDim.x(), EPS);
		assertEquals(3 * cmPerCell, isuDim.y(), EPS);
	}

	@Test
	@DisplayName("Grid.Position.toISUCoordCentered should put cell centers on multiples of cmPerCell")
	void gridPositionToCenteredISUCoordShouldScaleByCmPerCell() {
		ISU.Coord c00 = pos(0, 0).toISUCoordCentered();
		ISU.Coord c23 = pos(2, 3).toISUCoordCentered();

		assertNotNull(c00);
		assertNotNull(c23);

		assertEquals(0.0, c00.x(), EPS);
		assertEquals(0.0, c00.y(), EPS);

		assertEquals(2 * cmPerCell, c23.x(), EPS);
		assertEquals(3 * cmPerCell, c23.y(), EPS);
	}

	@Test
	@DisplayName("Two adjacent grid cell centers should be cmPerCell apart in ISU")
	void adjacentCellCentersShouldBeCmPerCellApartInISU() {
		ISU.Coord c00 = pos(0, 0).toISUCoordCentered();
		ISU.Coord c10 = pos(1, 0).toISUCoordCentered();
		ISU.Coord c01 = pos(0, 1).toISUCoordCentered();

		assertEquals(cmPerCell, c00.distanceTo(c10), EPS);
		assertEquals(cmPerCell, c00.distanceTo(c01), EPS);
	}

	@Test
	@DisplayName("Grid position -> centered ISU coord -> Grid position should round-trip")
	void gridToCenteredISUToGridShouldRoundTrip() {
		for (int yCell = 0; yCell < HEIGHT_NCELL; yCell++) {
			for (int xCell = 0; xCell < WIDTH_NCELL; xCell++) {
				Grid.Position p = pos(xCell, yCell);

				ISU.Coord center = p.toISUCoordCentered();

				assertNotNull(center);
				assertEquals(p, center.toGridPosition(), "Round-trip failed at (" + xCell + "," + yCell + ")");
			}
		}
	}

	@Test
	@DisplayName("ISU coordinates should convert to grid positions with cell centers at multiples of cmPerCell")
	void isuCoordinatesInCmShouldConvertToCenteredGridPositions() {
		assertEquals(pos(0, 0), isu.new Coord(0.0, 0.0).toGridPosition());
		assertEquals(pos(1, 0), isu.new Coord(cmPerCell, 0.0).toGridPosition());
		assertEquals(pos(0, 1), isu.new Coord(0.0, cmPerCell).toGridPosition());
		assertEquals(pos(2, 3), isu.new Coord(2 * cmPerCell, 3 * cmPerCell).toGridPosition());

		assertEquals(pos(0, 0), isu.new Coord(cmPerCell / 2.0 - EPS, 0.0).toGridPosition());
		assertEquals(pos(1, 0), isu.new Coord(cmPerCell / 2.0, 0.0).toGridPosition());

		assertEquals(pos(0, 0), isu.new Coord(0.0, cmPerCell / 2.0 - EPS).toGridPosition());
		assertEquals(pos(0, 1), isu.new Coord(0.0, cmPerCell / 2.0).toGridPosition());
	}

	@Test
	@DisplayName("Negative ISU coordinates in cm should wrap to torus grid positions")
	void negativeISUCoordinatesInCmShouldWrapToTorusGridPosition() {
		assertEquals(pos(WIDTH_NCELL - 1, 0), isu.new Coord(-cmPerCell, 0.0).toGridPosition());
		assertEquals(pos(0, HEIGHT_NCELL - 1), isu.new Coord(0.0, -cmPerCell).toGridPosition());
	}

	@Test
	@DisplayName("Position.toISUCoord should represent the top-left corner in cm")
	void gridPositionToISUCoordShouldRepresentTopLeftCornerInCm() {
		Grid.Position p = pos(3, 4);

		ISU.Coord topLeft = p.toISUCoord();
		ISU.Coord center = p.toISUCoordCentered();

		double expectedDistance = Math
				.sqrt((cmPerCell / 2.0) * (cmPerCell / 2.0) + (cmPerCell / 2.0) * (cmPerCell / 2.0));

		assertEquals(expectedDistance, topLeft.distanceTo(center), EPS);
	}

	@Test
	@DisplayName("Wrapped grid positions should match a reference modulo formula in cells")
	void wrappedPositionsShouldMatchReferenceModuloFormulaInCells() {
		int[][] samples = { { 0, 0 }, { WIDTH_NCELL, HEIGHT_NCELL }, { WIDTH_NCELL + 1, HEIGHT_NCELL + 2 }, { -1, -1 },
				{ -WIDTH_NCELL - 3, -HEIGHT_NCELL - 4 } };

		for (int[] sample : samples) {
			int xCell = sample[0];
			int yCell = sample[1];

			Grid.Position p = pos(xCell, yCell);

			assertPositionEquals(wrapCell(xCell, WIDTH_NCELL), wrapCell(yCell, HEIGHT_NCELL), p);
		}
	}
}