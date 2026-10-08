package engine.gal.arguments;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CategoryTest {

	@Test
	void canonicalReturnsExistingCategory() {
		assertSame(Category.Void, Category.canonical("Void"));
		assertSame(Category.Adversary, Category.canonical("Adversary"));
		assertSame(Category.Obstacle, Category.canonical("Obstacle"));
		assertSame(Category.Team, Category.canonical("Team"));
	}

	@Test
	void unknownCategoryThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> Category.canonical("Unknown"));
	}

	@Test
	void interactionCanBeSet() {
		Category.setInteraction(Category.Adversary, Category.Team, true);

		assertTrue(Category.Adversary.interactsWith(Category.Team));
	}

	@Test
	void interactionIsNotAutomaticallySymmetric() {
		Category.setInteraction(Category.Obstacle, Category.Team, true);

		assertTrue(Category.Obstacle.interactsWith(Category.Team));
		assertFalse(Category.Team.interactsWith(Category.Obstacle));
	}
}