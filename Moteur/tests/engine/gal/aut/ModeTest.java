package engine.gal.aut;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ModeTest {

	@Test
	void canonicalReturnsExistingMode() {
		assertSame(Mode.Walking, Mode.canonical("Walking"));
		assertSame(Mode.Blocking, Mode.canonical("Blocking"));
	}

	@Test
	void canonicalCreatesAndReusesCustomMode() {
		Mode chase1 = Mode.canonical("Chase");
		Mode chase2 = Mode.canonical("Chase");

		assertSame(chase1, chase2);
	}
}