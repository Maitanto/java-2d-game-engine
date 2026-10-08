package engine.gal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;
import engine.gal.aut.Mode;
import engine.gal.aut.State;
import engine.gal.condition.Conjunction;
import engine.gal.condition.True;

class GALBaseTest {

	@Test
	void directionCanonicalReturnsSameObjects() {
		assertSame(Direction.N, Direction.canonical("N"));
		assertSame(Direction.N, Direction.canonical("North"));

		assertSame(Direction.F, Direction.canonical("F"));
		assertSame(Direction.F, Direction.canonical("Forward"));
		assertSame(Direction.F, Direction.canonical("Front"));
	}

	@Test
	void directionPredicatesAreCorrect() {
		assertTrue(Direction.N.isAbsolute());
		assertTrue(Direction.S.isAbsolute());
		assertTrue(Direction.E.isAbsolute());
		assertTrue(Direction.W.isAbsolute());

		assertTrue(Direction.F.isRelative());
		assertTrue(Direction.B.isRelative());

		assertTrue(Direction.H.isHere());
	}

	@Test
	void directionAbsoluteAnglesAreCorrect() {
		assertEquals(0, Direction.E.toAngle());
		assertEquals(90, Direction.S.toAngle());
		assertEquals(180, Direction.W.toAngle());
		assertEquals(270, Direction.N.toAngle());
	}

	@Test
	void relativeDirectionHasNoAbsoluteAngle() {
		assertThrows(IllegalStateException.class, () -> Direction.F.toAngle());
	}

	@Test
	void categoryCanonicalReturnsSameObjects() {
		assertSame(Category.Void, Category.canonical("Void"));
		assertSame(Category.Adversary, Category.canonical("Adversary"));
		assertSame(Category.Obstacle, Category.canonical("Obstacle"));
		assertSame(Category.Team, Category.canonical("Team"));
	}

	@Test
	void categoryInteractionCanBeSet() {
		Category.setInteraction(Category.Adversary, Category.Team, true);

		assertTrue(Category.Adversary.interactsWith(Category.Team));
	}

	@Test
	void modeCanonicalReusesKnownMode() {
		assertSame(Mode.Walking, Mode.canonical("Walking"));
		assertSame(Mode.Blocking, Mode.canonical("Blocking"));
	}

	@Test
	void modeCanonicalReusesCustomMode() {
		Mode chase1 = Mode.canonical("Chase");
		Mode chase2 = Mode.canonical("Chase");

		assertSame(chase1, chase2);
	}

	@Test
	void statesWithSameModeAndIdAreEqual() {
		State s1 = new State("Walking", 0);
		State s2 = new State("Walking", 0);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	void statesWithDifferentIdAreDifferent() {
		State s1 = new State("Walking", 0);
		State s2 = new State("Walking", 1);

		assertNotEquals(s1, s2);
	}

	@Test
	void trueConditionAlwaysReturnsTrue() {
		True condition = new True();

		assertTrue(condition.eval(null));
	}

	@Test
	void emptyConjunctionIsTrue() {
		Conjunction conjunction = new Conjunction();

		assertTrue(conjunction.eval(null));
	}

	@Test
	void conjunctionReturnsTrueWhenAllConditionsAreTrue() {
		Conjunction conjunction = new Conjunction();

		conjunction.add(e -> true);
		conjunction.add(e -> true);

		assertTrue(conjunction.eval(null));
	}

	@Test
	void conjunctionReturnsFalseWhenOneConditionIsFalse() {
		Conjunction conjunction = new Conjunction();

		conjunction.add(e -> true);
		conjunction.add(e -> false);
		conjunction.add(e -> true);

		assertFalse(conjunction.eval(null));
	}
}