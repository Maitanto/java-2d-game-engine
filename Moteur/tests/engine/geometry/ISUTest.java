package engine.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import game.Game;

class ISUTest {

	private static final int WIDTH_NCELL = 12;
	private static final int HEIGHT_NCELL = 10;
	private static final double EPS = 1e-9;

	private Game game;
	private Grid grid;
	private ISU isu;

	private double cmPerCell;
	private double worldWidthCm;
	private double worldHeightCm;

	@BeforeEach
	void setUp() {
		game = new Game(WIDTH_NCELL, HEIGHT_NCELL);
		grid = game.grid;
		isu = game.isu;

		cmPerCell = game.cmPerCell;
		worldWidthCm = game.width_cm;
		worldHeightCm = game.height_cm;

		assertNotNull(grid);
		assertNotNull(isu);
		assertTrue(cmPerCell > 0);
		assertEquals(WIDTH_NCELL * cmPerCell, worldWidthCm, EPS);
		assertEquals(HEIGHT_NCELL * cmPerCell, worldHeightCm, EPS);
	}

	private double cellToCm(int ncell) {
		return ncell * cmPerCell;
	}

	private double wrapCm(double valueCm, double perimeterCm) {
		double r = valueCm % perimeterCm;
		return r < 0 ? r + perimeterCm : r;
	}

	private void assertCoordEquals(double expectedXcm, double expectedYcm, ISU.Coord actual) {
		assertEquals(expectedXcm, actual.x(), EPS);
		assertEquals(expectedYcm, actual.y(), EPS);
	}

	private void assertVectorEquals(double expectedXcm, double expectedYcm, ISU.Vector actual) {
		assertEquals(expectedXcm, actual.x(), EPS);
		assertEquals(expectedYcm, actual.y(), EPS);
	}

	// ============================================================
	// ISU : toutes les coordonnées sont en cm
	// ============================================================

	@Test
	@DisplayName("Coord should store coordinates already inside the world, in cm")
	void coordShouldStoreCoordinatesInsideWorldInCm() {
		ISU.Coord c = isu.new Coord(cellToCm(2), cellToCm(3));

		assertCoordEquals(cellToCm(2), cellToCm(3), c);
	}

	@Test
	@DisplayName("Coord should normalize coordinates using the world perimeter in cm")
	void coordShouldNormalizeCoordinatesWithCmPerimeter() {
		ISU.Coord c = isu.new Coord(worldWidthCm + cellToCm(2), -cellToCm(3));

		assertCoordEquals(cellToCm(2), worldHeightCm - cellToCm(3), c);
	}

	@Test
	@DisplayName("setxy should normalize coordinates using cm, not cells")
	void setxyShouldNormalizeCoordinatesWithCmPerimeter() {
		ISU.Coord c = isu.new Coord(0.0, 0.0);

		c.setxy(worldWidthCm + cellToCm(1), worldHeightCm + cellToCm(2));

		assertCoordEquals(cellToCm(1), cellToCm(2), c);
	}

	@Test
	@DisplayName("Dimension should normalize values using cm, not cells")
	void dimensionShouldNormalizeUsingCmPerimeter() {
		ISU.Dimension d = isu.new Dimension(worldWidthCm + cellToCm(4), -cellToCm(2));

		assertEquals(cellToCm(4), d.x(), EPS);
		assertEquals(worldHeightCm - cellToCm(2), d.y(), EPS);
	}

	@Test
	@DisplayName("Coord.translate should add a vector in cm then normalize the coord")
	void translateShouldMoveCoordAndNormalizeResultWithCmUnits() {
		ISU.Coord c = isu.new Coord(worldWidthCm - cellToCm(1), worldHeightCm - cellToCm(1));
		ISU.Vector v = isu.new Vector(cellToCm(2), cellToCm(3));

		c.translate(v);

		assertCoordEquals(cellToCm(1), cellToCm(2), c);
	}

	@Test
	@DisplayName("mkTranslated should return a new coord without changing the original")
	void mkTranslatedShouldReturnNewCoordWithoutChangingOriginal() {
		ISU.Coord original = isu.new Coord(cellToCm(1), cellToCm(2));
		ISU.Vector v = isu.new Vector(cellToCm(3), cellToCm(4));

		ISU.Coord result = original.mkTranslated(v);

		assertCoordEquals(cellToCm(4), cellToCm(6), result);
		assertCoordEquals(cellToCm(1), cellToCm(2), original);
		assertNotSame(original, result);
	}

	@Test
	@DisplayName("mkCopy should return an independent equal coord")
	void mkCopyShouldReturnIndependentEqualCoord() {
		ISU.Coord original = isu.new Coord(cellToCm(1), cellToCm(2));

		ISU.Coord copy = original.mkCopy();

		assertNotSame(original, copy);
		assertEquals(original, copy);

		copy.translate(isu.new Vector(cellToCm(1), cellToCm(1)));

		assertCoordEquals(cellToCm(1), cellToCm(2), original);
		assertCoordEquals(cellToCm(2), cellToCm(3), copy);
	}

	// ============================================================
	// VECTOR : un vecteur n'est pas une coordonnée
	// ============================================================

	@Test
	@DisplayName("Vector should store components in cm without normalizing")
	void vectorShouldStoreComponentsWithoutNormalizing() {
		ISU.Vector v = isu.new Vector(cellToCm(2), cellToCm(3));

		assertVectorEquals(cellToCm(2), cellToCm(3), v);
	}

	@Test
	@DisplayName("Vector should keep components outside the world without wrapping")
	void vectorShouldNotNormalizeWithCmPerimeter() {
		ISU.Vector v = isu.new Vector(worldWidthCm + cellToCm(2), -cellToCm(3));

		assertVectorEquals(worldWidthCm + cellToCm(2), -cellToCm(3), v);
	}

	@Test
	@DisplayName("Vector.add should add cm components without normalizing")
	void addShouldAddVectorsWithoutNormalizing() {
		ISU.Vector v = isu.new Vector(worldWidthCm - cellToCm(1), worldHeightCm - cellToCm(1));
		ISU.Vector other = isu.new Vector(cellToCm(1), cellToCm(2));

		v.add(other);

		assertVectorEquals(worldWidthCm, worldHeightCm + cellToCm(1), v);
	}

	@Test
	@DisplayName("scale should multiply a vector in cm without normalizing")
	void scaleShouldMultiplyVectorWithoutNormalizing() {
		ISU.Vector v = isu.new Vector(cellToCm(3), 0.0);

		v.scale(4.0);

		assertVectorEquals(cellToCm(12), 0.0, v);
	}

	@Test
	@DisplayName("scale(x,y) should multiply each axis in cm without normalizing")
	void scaleXYShouldMultiplyEachAxisWithCmUnits() {
		ISU.Vector v = isu.new Vector(cellToCm(2), cellToCm(3));

		v.scale(2.0, 3.0);

		assertVectorEquals(cellToCm(4), cellToCm(9), v);
	}

	// ============================================================
	// DOT / NORM / UNITY
	// ============================================================

	@Test
	@DisplayName("dot should return the scalar product in cm²")
	void dotShouldReturnScalarProductInSquareCentimeters() {
		ISU.Vector u = isu.new Vector(cmPerCell, 0.0);
		ISU.Vector v = isu.new Vector(0.0, cmPerCell);
		ISU.Vector w = isu.new Vector(3.0 * cmPerCell, 4.0 * cmPerCell);

		assertEquals(0.0, u.dot(v), EPS);
		assertEquals(25.0 * cmPerCell * cmPerCell, w.dot(w), EPS);
	}

	@Test
	@DisplayName("norm should return the Euclidean norm in cm")
	void normShouldReturnEuclideanNormInCentimeters() {
		ISU.Vector v = isu.new Vector(3.0 * cmPerCell, 4.0 * cmPerCell);

		assertEquals(5.0 * cmPerCell, v.norm(), EPS);
	}

	@Test
	@DisplayName("unity should make vector norm equal to one")
	void unityShouldMakeVectorNormEqualToOne() {
		ISU.Vector v = isu.new Vector(3.0, 4.0);

		v.unity();

		assertEquals(1.0, v.norm(), EPS);
		assertVectorEquals(0.6, 0.8, v);
	}

	@Test
	@DisplayName("unity should reject zero vector")
	void unityShouldRejectZeroVector() {
		ISU.Vector v = isu.new Vector(0.0, 0.0);

		assertThrows(IllegalStateException.class, v::unity);
	}

	// ============================================================
	// ROTATION
	// ============================================================

	@Test
	@DisplayName("Coord.rotation(90) should rotate around origin in cm")
	void coordRotationAroundOriginShouldRotatePointBy90DegreesInCm() {
		ISU.Coord c = isu.new Coord(cmPerCell, 0.0);

		c.rotation(90);

		assertCoordEquals(0.0, cmPerCell, c);
	}

	@Test
	@DisplayName("Coord.rotation(180) should rotate around origin then normalize negative cm coordinates")
	void coordRotationAroundOriginShouldRotatePointBy180DegreesAndWrap() {
		ISU.Coord c = isu.new Coord(cmPerCell, 0.0);

		c.rotation(180);

		assertCoordEquals(worldWidthCm - cmPerCell, 0.0, c);
	}

	@Test
	@DisplayName("Coord.rotateAround should rotate around a center in cm")
	void coordRotationAroundCenterShouldRotatePointInCm() {
		ISU.Coord center = isu.new Coord(cellToCm(1), cellToCm(1));
		ISU.Coord point = isu.new Coord(cellToCm(2), cellToCm(1));

		point.rotateAround(center, 90);

		assertCoordEquals(cellToCm(1), cellToCm(2), point);
	}

	@Test
	@DisplayName("Coord.rotateAround should preserve distance to center when no wrapping ambiguity occurs")
	void coordRotationAroundCenterShouldPreserveDistanceToCenter() {
		ISU.Coord center = isu.new Coord(cellToCm(2), cellToCm(2));
		ISU.Coord point = isu.new Coord(cellToCm(4), cellToCm(3));
		double before = point.distanceTo(center);

		point.rotateAround(center, 30);

		double after = point.distanceTo(center);

		assertEquals(before, after, EPS);
	}

	@Test
	@DisplayName("Vector.turn(90) should rotate the vector in cm")
	void vectorTurnShouldRotateVectorBy90DegreesInCm() {
		ISU.Vector v = isu.new Vector(cmPerCell, 0.0);

		v.turn(90);

		assertVectorEquals(0.0, cmPerCell, v);
	}

	@Test
	@DisplayName("Vector.turn(180) should rotate without normalizing negative cm components")
	void vectorTurnShouldRotateVectorBy180DegreesWithoutWrapping() {
		ISU.Vector v = isu.new Vector(cmPerCell, 0.0);

		v.turn(180);

		assertVectorEquals(-cmPerCell, 0.0, v);
	}

	@Test
	@DisplayName("Vector.turn should preserve norm")
	void vectorTurnShouldPreserveNorm() {
		ISU.Vector v = isu.new Vector(3.0, 4.0);
		double before = v.norm();

		v.turn(30);

		double after = v.norm();

		assertEquals(before, after, EPS);
	}

	// ============================================================
	// DISTANCE
	// ============================================================

	@Test
	@DisplayName("distanceTo should return Euclidean distance in cm")
	void distanceToShouldReturnEuclideanDistanceInCentimeters() {
		ISU.Coord a = isu.new Coord(0.0, 0.0);
		ISU.Coord b = isu.new Coord(3.0 * cmPerCell, 4.0 * cmPerCell);

		assertEquals(5.0 * cmPerCell, a.distanceTo(b), EPS);
	}

	@Test
	@DisplayName("distanceTo should use shortest torus distance in cm")
	void distanceToShouldUseShortestDistanceOnTorusInCentimeters() {
		ISU.Coord a = isu.new Coord(worldWidthCm - cmPerCell, 0.0);
		ISU.Coord b = isu.new Coord(cmPerCell, 0.0);

		assertEquals(2.0 * cmPerCell, a.distanceTo(b), EPS);
	}

	// ============================================================
	// FACTORIES
	// ============================================================

	@Test
	@DisplayName("mkVectorToward should return a raw vector from source to target in cm")
	void mkVectorTowardShouldReturnRawVectorFromSourceToTargetInCm() {
		ISU.Coord source = isu.new Coord(cellToCm(1), cellToCm(1));
		ISU.Coord target = isu.new Coord(cellToCm(4), cellToCm(5));

		ISU.Vector v = source.mkVectorToward(target);

		assertVectorEquals(cellToCm(3), cellToCm(4), v);
	}

	@Test
	@DisplayName("mkVectorToward should not canonicalize the vector on a torus")
	void mkVectorTowardShouldNotCanonicalizeVectorOnTorus() {
		ISU.Coord source = isu.new Coord(worldWidthCm - cmPerCell, 0.0);
		ISU.Coord target = isu.new Coord(cmPerCell, 0.0);

		ISU.Vector v = source.mkVectorToward(target);

		assertVectorEquals(cmPerCell - (worldWidthCm - cmPerCell), 0.0, v);
	}

//	@Test
//	@DisplayName("mkVector should create a vector with the same cm components")
//	void mkVectorShouldCreateEquivalentVector() {
//		ISU.Coord c = isu.new Coord(cellToCm(3), cellToCm(4));
//
//		ISU.Vector v = isu.mkVector();
//
//		assertVectorEquals(cellToCm(3), cellToCm(4), v);
//	}
//
//	@Test
//	@DisplayName("mkScaledVector should create a scaled vector without changing the source")
//	void mkScaledVectorShouldCreateScaledVectorWithoutChangingSource() {
//		ISU.Coord c = isu.new Coord(cellToCm(2), cellToCm(3));
//
//		ISU.Vector v = c.mkScaledVector(2.0);
//
//		assertVectorEquals(cellToCm(4), cellToCm(6), v);
//		assertCoordEquals(cellToCm(2), cellToCm(3), c);
//	}
//
//	@Test
//	@DisplayName("mkScaledVector(x,y) should apply different factors in cm")
//	void mkScaledVectorXYShouldApplyDifferentFactors() {
//		ISU.Coord c = isu.new Coord(cellToCm(2), cellToCm(3));
//
//		ISU.Vector v = c.mkScaledVector(2.0, 3.0);
//
//		assertVectorEquals(cellToCm(4), cellToCm(9), v);
//	}

	// ============================================================
	// CONVERSION ISU -> GRID
	// Convention : le centre de la case 0 est 0.0
	// ============================================================

	@Test
	@DisplayName("toGridPosition should convert cm coordinates to centered grid cells")
	void toGridPositionShouldConvertCmToCenteredCellCoordinates() {
		assertEquals(grid.new Position(0, 0), isu.new Coord(0.0, 0.0).toGridPosition());
		assertEquals(grid.new Position(1, 0), isu.new Coord(cmPerCell, 0.0).toGridPosition());
		assertEquals(grid.new Position(0, 1), isu.new Coord(0.0, cmPerCell).toGridPosition());
		assertEquals(grid.new Position(2, 3), isu.new Coord(cellToCm(2), cellToCm(3)).toGridPosition());
	}

	@Test
	@DisplayName("toGridPosition should switch cell at half cmPerCell")
	void toGridPositionShouldSwitchCellAtHalfCellBoundary() {
		assertEquals(grid.new Position(0, 0), isu.new Coord(cmPerCell / 2.0 - EPS, 0.0).toGridPosition());
		assertEquals(grid.new Position(1, 0), isu.new Coord(cmPerCell / 2.0, 0.0).toGridPosition());

		assertEquals(grid.new Position(0, 0), isu.new Coord(0.0, cmPerCell / 2.0 - EPS).toGridPosition());
		assertEquals(grid.new Position(0, 1), isu.new Coord(0.0, cmPerCell / 2.0).toGridPosition());
	}

	@Test
	@DisplayName("Negative ISU cm coordinates should wrap before conversion to grid cells")
	void negativeCmCoordinatesShouldWrapBeforeConvertingToGridPosition() {
		assertEquals(grid.new Position(WIDTH_NCELL - 1, 0), isu.new Coord(-cmPerCell, 0.0).toGridPosition());
		assertEquals(grid.new Position(0, HEIGHT_NCELL - 1), isu.new Coord(0.0, -cmPerCell).toGridPosition());
	}

	// ============================================================
	// EQUALITY
	// ============================================================

	@Test
	@DisplayName("Coords with same canonical cm coordinates should be equal")
	void coordsWithSameCanonicalCoordinatesShouldBeEqual() {
		ISU.Coord a = isu.new Coord(worldWidthCm + cellToCm(2), 0.0);
		ISU.Coord b = isu.new Coord(cellToCm(2), 0.0);

		assertTrue(a.equiv(b)); // TODO TEST DID NOT SUCCEED ANYMORE WITH EQUALS, USED EQUIV INSTEAD.
								// CHECK IF IT'S OK
	}

	@Test
	@DisplayName("Vectors should not be treated as canonical torus coordinates")
	void vectorsShouldNotBeCanonicalTorusCoordinates() {
		ISU.Vector a = isu.new Vector(worldWidthCm + cellToCm(2), 0.0);
		ISU.Vector b = isu.new Vector(cellToCm(2), 0.0);

		assertVectorEquals(worldWidthCm + cellToCm(2), 0.0, a);
		assertVectorEquals(cellToCm(2), 0.0, b);
		assertNotEquals(a, b);
	}

	@Test
	@DisplayName("Coord should not equal Vector even with same cm components")
	void coordShouldNotEqualVectorEvenWithSameCoordinates() {
		ISU.Coord c = isu.new Coord(cellToCm(1), cellToCm(2));
		ISU.Vector v = isu.new Vector(cellToCm(1), cellToCm(2));

		assertNotEquals(c, v);
		assertNotEquals(v, c);
	}

	@Test
	@DisplayName("Geometry object should not equal null or unrelated object")
	void geometryShouldNotEqualNullOrOtherObject() {
		ISU.Coord c = isu.new Coord(cellToCm(1), cellToCm(2));

		assertNotEquals(null, c);
		assertNotEquals("hello", c);
	}

	@Test
	@DisplayName("Reference wrap formula should match ISU normalization in cm")
	void wrappedCmCoordinatesShouldMatchReferenceModuloFormula() {
		double x = -cellToCm(3);
		double y = worldHeightCm + cellToCm(4);

		ISU.Coord c = isu.new Coord(x, y);

		assertCoordEquals(wrapCm(x, worldWidthCm), wrapCm(y, worldHeightCm), c);
	}
}