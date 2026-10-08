package engine.gal.aut;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StateTest {

	@Test
	void statesWithSameModeAndIdAreEqual() {
		State s1 = new State("Walking", 0);
		State s2 = new State("Walking", 0);

		assertEquals(s1, s2);
		assertEquals(s1.hashCode(), s2.hashCode());
	}

	@Test
	void statesWithDifferentIdsAreDifferent() {
		State s1 = new State("Walking", 0);
		State s2 = new State("Walking", 1);

		assertNotEquals(s1, s2);
	}

	@Test
	void statesWithDifferentModesAreDifferent() {
		State s1 = new State("Walking", 0);
		State s2 = new State("Running", 0);

		assertNotEquals(s1, s2);
	}
}