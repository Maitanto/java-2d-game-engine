package engine.gal.aut;

import java.util.HashMap;
import java.util.Map;

public class Mode {

	// CONSTANTS

	public static final Mode Blocking;
	public static final Mode Dying;
	public static final Mode Escaping;
	public static final Mode Fighting;
	public static final Mode Resting;
	public static final Mode Running;
	public static final Mode Searching;
	public static final Mode Sleeping;
	public static final Mode Waiting;
	public static final Mode Walking;

	// STATIC

	private static final Map<String, Mode> modes = new HashMap<>();

	// STATIC INITIALIZATION

	static {
		Blocking = register(new Mode("Blocking"));
		Dying = register(new Mode("Dying"));
		Escaping = register(new Mode("Escaping"));
		Fighting = register(new Mode("Fighting"));
		Resting = register(new Mode("Resting"));
		Running = register(new Mode("Running"));
		Searching = register(new Mode("Searching"));
		Sleeping = register(new Mode("Sleeping"));
		Waiting = register(new Mode("Waiting"));
		Walking = register(new Mode("Walking"));
	}

	private static Mode register(Mode mode) {
		modes.put(mode.name, mode);
		return mode;
	}

	// FACTORY

	public static Mode canonical(String name) {
		Mode mode = modes.get(name);

		if (mode == null) {
			mode = register(new Mode(name));
		}

		return mode;
	}

	// CONSTRUCTOR

	private final String name;

	private Mode(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("mode name cannot be null or blank");
		}

		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}