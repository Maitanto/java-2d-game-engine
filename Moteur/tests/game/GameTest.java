package game;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GameTest {

	private static final double EPSILON = 1e-9;

	@Test
	void constructorWithCellsShouldSetCmCorrectly() {
		Game game = new Game(10, 10);

		assertEquals(10, game.width_ncell);
		assertEquals(10, game.height_ncell);

		assertEquals(37.0, game.width_cm, EPSILON);
		assertEquals(37.0, game.height_cm, EPSILON);
	}

	@Test
	void constructorWithCmShouldSetCellsCorrectly() {
		Game game = new Game(37.0, 37.0);

		assertEquals(10, game.width_ncell);
		assertEquals(10, game.height_ncell);

		assertEquals(37.0, game.width_cm, EPSILON);
		assertEquals(37.0, game.height_cm, EPSILON);
	}

	@Test
	void bothConstructorsShouldCreateEquivalentGames() {
		Game fromCells = new Game(10, 10);
		Game fromCm = new Game(37.0, 37.0);

		assertEquals(fromCells.width_ncell, fromCm.width_ncell);
		assertEquals(fromCells.height_ncell, fromCm.height_ncell);

		assertEquals(fromCells.width_cm, fromCm.width_cm, EPSILON);
		assertEquals(fromCells.height_cm, fromCm.height_cm, EPSILON);
	}

	@Test
	void constructorWithCmShouldRejectInvalidSize() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Game(38.0, 37.0);
		});
	}
}