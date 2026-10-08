package engine.gal.condition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConditionTest {

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
	void conjunctionIsTrueWhenAllConditionsAreTrue() {
		Conjunction conjunction = new Conjunction();

		conjunction.add(e -> true);
		conjunction.add(e -> true);

		assertTrue(conjunction.eval(null));
	}

	@Test
	void conjunctionIsFalseWhenOneConditionIsFalse() {
		Conjunction conjunction = new Conjunction();

		conjunction.add(e -> true);
		conjunction.add(e -> false);
		conjunction.add(e -> true);

		assertFalse(conjunction.eval(null));
	}

	@Test
	void conjunctionRejectsNullCondition() {
		Conjunction conjunction = new Conjunction();

		assertThrows(IllegalArgumentException.class, () -> conjunction.add(null));
	}
}