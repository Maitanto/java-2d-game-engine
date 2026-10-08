package game.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.Game;
import game.gradius.entity.Enemy;
import game.gradius.entity.Obstacle;
import game.gradius.entity.Ship;

class EntityCollisionTest {

	@BeforeEach
	void initGame() {
		new Game(30, 30);
	}

	// =========================
	// Helpers de test
	// =========================

	private static class TestShip1 extends Ship {
		void putAt(int x, int y) {
			super.setPosition(super.grid().new Position(x, y));
			super.setBounding();
		}
	}

	private static class TestShip2 extends Ship {
		void putAt(int x, int y) {
			super.setPosition(super.grid().new Position(x, y));
			super.setBounding();
		}
	}

	private static class TestEnemy extends Enemy {
		void putAt(int x, int y) {
			super.setPosition(super.grid().new Position(x, y));
			super.setBounding();
		}

		void rotate(int angleDegree) {
			super.turn(angleDegree);
			super.setBounding();
		}
	}

	// =========================
	// PacMan / Gum
	// =========================

	@Test
	void ship1AndShip2SameCellIntersect() {
		TestShip1 ship1 = new TestShip1();
		TestShip2 ship2 = new TestShip2();

		ship1.putAt(10, 10);
		ship2.putAt(10, 10);

		assertTrue(ship1.intersects(ship2));
		assertTrue(ship2.intersects(ship1));
	}

	@Test
	void ship1AndShip2FarAwayDoNotIntersect() {
		TestShip1 ship1 = new TestShip1();
		TestShip2 ship2 = new TestShip2();

		ship1.putAt(10, 10);
		ship2.putAt(5, 5);

		assertFalse(ship1.intersects(ship2));
		assertFalse(ship2.intersects(ship1));
	}

	// =========================
	// PacMan / Obstacle
	// =========================

	@Test
	void ship1AndObstacleSameCellIntersect() {
		TestShip1 ship1 = new TestShip1();
		ship1.putAt(10, 10);

		Obstacle obstacle = new Obstacle(10, 10);

		assertTrue(ship1.intersects(obstacle));
		assertTrue(obstacle.intersects(ship1));
	}

	// =========================
	// Boss / Obstacle
	// =========================

	@Test
	void EnemyAndObstacleOnHorizontalArmIntersect() {
		TestEnemy en = new TestEnemy();
		en.putAt(20, 20);

		// Le Boss non tourné a une branche horizontale vers la droite.
		Obstacle obstacle = new Obstacle(21, 20);

		assertTrue(en.intersects(obstacle));
		assertTrue(obstacle.intersects(en));
	}

	@Test
	void EnemyTurnedNinetyDegreesAndObstacleOnNewArmIntersect() {
		TestEnemy en = new TestEnemy();
		en.putAt(20, 20);

		en.rotate(90);

		// Après rotation de 90°, la branche qui partait à droite part vers le bas.
		Obstacle obstacle = new Obstacle(20, 21);

		assertTrue(en.intersects(obstacle));
		assertTrue(obstacle.intersects(en));
	}
}