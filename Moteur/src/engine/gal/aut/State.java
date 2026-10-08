package engine.gal.aut;

import java.util.Objects;

public class State {

	private Mode mode;
	private int id;

	public State(String modeName, int id) {
		this.mode = Mode.canonical(modeName);
		this.id = id;
	}

	public State(Mode mode, int id) {
		if (mode == null) {
			throw new IllegalArgumentException("mode cannot be null");
		}

		this.mode = mode;
		this.id = id;
	}

	public Mode mode() {
		return mode;
	}

	public int id() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof State)) {
			return false;
		}

		State state = (State) o;
		return id == state.id && mode.equals(state.mode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mode, id);
	}

	@Override
	public String toString() {
		return mode + "(" + id + ")";
	}
}