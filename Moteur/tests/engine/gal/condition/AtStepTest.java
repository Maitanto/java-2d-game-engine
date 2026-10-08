package engine.gal.condition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;
import game.Game;
import game.gradius.entity.Obstacle;
import game.gradius.entity.Ship;

class AtStepTest {

	// private Game game;
	private Ship ship;
	private int x, y;

	@BeforeEach
	void setup() {
		new Game(30, 30);

		ship = new Ship();
		ship.category(Category.Team);
		x = ship.position().x();
		y = ship.position().y();
	}

	@Test
	void atStepDetectsObstacleEast() {
		Obstacle obstacle = new Obstacle(x + 1, y);
		obstacle.category(Category.Obstacle);

		AtStep condition = new AtStep(Direction.E, Category.Obstacle, 1);

		assertTrue(condition.eval(ship));
	}

	@Test
	void atStepDoesNotDetectObstacleInWrongDirection() {
		Obstacle obstacle = new Obstacle(11, 10);
		obstacle.category(Category.Obstacle);

		AtStep condition = new AtStep(Direction.W, Category.Obstacle, 1);

		assertFalse(condition.eval(ship));
	}

	@Test
	void atStepForwardUsesEntityOrientation() {
		Obstacle obstacle = new Obstacle(x + 1, y);
		obstacle.category(Category.Obstacle);

		// Pacman orientation initiale = 0°, donc Forward = East
		AtStep condition = new AtStep(Direction.F, Category.Obstacle, 1);

		assertTrue(condition.eval(ship));
	}

	@Test
	void atStepVoidIsTrueWhenThereIsNoOtherEntity() {
		AtStep condition = new AtStep(Direction.W, Category.Void, 2);

		assertTrue(condition.eval(ship));
	}

	@Test
	void atStepVoidIsFalseWhenThereIsAnotherEntity() {
		Obstacle obstacle = new Obstacle(x + 1, y);
		obstacle.category(Category.Obstacle);

		AtStep condition = new AtStep(Direction.E, Category.Void, 1);

		assertFalse(condition.eval(ship));
	}
}