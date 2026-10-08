package engine.gal.arguments;

import java.util.HashMap;
import java.util.Map;

public class Direction {

	// CONSTANTS

	public static final Direction B; // Backward
	public static final Direction F; // Forward
	public static final Direction L; // Left
	public static final Direction R; // Right
	public static final Direction H; // Here
	public static final Direction N; // North
	public static final Direction S; // South
	public static final Direction E; // East
	public static final Direction W; // West
	public static final Direction NE; // North-East
	public static final Direction NW; // North-West
	public static final Direction SE; // South-East
	public static final Direction SW; // South-West
	public static final Direction NNE; // North-North-East
	public static final Direction ENE; // East-North-East
	public static final Direction ESE; // East-South-East
	public static final Direction SSE; // South-South-East
	public static final Direction SSW; // South-South-West
	public static final Direction WSW; // West-South-West
	public static final Direction WNW; // West-North-West
	public static final Direction NNW; // North-North-West

	private static Map<String, Direction> directions = new HashMap<>();

	// STATIC INITIALIZATION

	/**
	 * @apiNote the global variables must be initialized in that section,
	 * @implNote which is executed at the class loading
	 */
	static {
		H = new Direction("Here");
		N = new Direction("North");
		S = new Direction("South");
		E = new Direction("East");
		W = new Direction("West");
		F = new Direction("Forward");
		B = new Direction("Backward");
		L = new Direction("Left");
		R = new Direction("Right");
		NE = new Direction("NorthEast");
		NW = new Direction("NorthWest");
		SE = new Direction("SouthEast");
		SW = new Direction("SouthWest");
		NNE = new Direction("NorthNorthEast");
		ENE = new Direction("EastNorthEast");
		ESE = new Direction("EastSouthEast");
		SSE = new Direction("SouthSouthEast");
		SSW = new Direction("SouthSouthWest");
		WSW = new Direction("WestSouthWest");
		WNW = new Direction("WestNorthWest");
		NNW = new Direction("NorthNorthWest");

		register(H, "H", "Here");
		register(N, "N", "North");
		register(S, "S", "South");
		register(E, "E", "East");
		register(W, "W", "West");
		register(F, "F", "Forward", "Front");
		register(B, "B", "Backward", "Back");
		register(L, "L", "Left");
		register(R, "R", "Right");
		register(NE, "NE", "NorthEast", "Northeast");
		register(NW, "NW", "NorthWest", "Northwest");
		register(SE, "SE", "SouthEast", "Southeast");
		register(SW, "SW", "SouthWest", "Southwest");
		register(NNE, "NNE", "NorthNorthEast", "Northnortheast");
		register(ENE, "ENE", "EastNorthEast", "Eastnortheast");
		register(ESE, "ESE", "EastSouthEast", "Eastsoutheast");
		register(SSE, "SSE", "SouthSouthEast", "Southsoutheast");
		register(SSW, "SSW", "SouthSouthWest", "Southsouthwest");
		register(WSW, "WSW", "WestSouthWest", "Westsouthwest");
		register(WNW, "WNW", "WestNorthWest", "Westnorthwest");
		register(NNW, "NNW", "NorthNorthWest", "Northnorthwest");
	}

	private static void register(Direction direction, String... names) {
		for (String name : names) {
			directions.put(name, direction);
		}
	}

	// STATIC

	/**
	 * @apiNote {@code directions} associates a Direction to a name in order to
	 *          ensure uniqueness (thus sharing) of the instance associated to that
	 *          name.
	 * @apiNote Advantages
	 *          <UL>
	 *          <LI>Sharing reduces memory consumption</LI>
	 *          <LI>Comparison can be done using {@code ==} instead of
	 *          {@code String:equals} on names.</LI>
	 *          <LI>it is easy to use: {@code Direction.canonical(name)} provides
	 *          the unique representative of the Direction associated to that
	 *          name.</LI>
	 *          </UL>
	 */

	// FACTORY

	/**
	 * @apiNote implements sharing : it avoids creating new direction each time the
	 *          parser encounters a direction.
	 * @return the existing direction associated to a name if it already exists
	 */
	public static Direction canonical(String name) {
		Direction direction = directions.get(name);

		if (direction == null) {
			throw new IllegalArgumentException("Unknown direction: " + name);
		}

		return direction;
	}

	// CONSTRUCTOR

	private String name;

	private Direction(String name) {
		this.name = name;
	}

	// PREDICATE

	public boolean isAbsolute() {
		return this == N || this == S || this == E || this == W || this == NE || this == NW || this == SE || this == SW
				|| this == NNE || this == ENE || this == ESE || this == SSE || this == SSW || this == WSW || this == WNW
				|| this == NNW;
	}

	public boolean isRelative() {
		return this == F || this == B || this == L || this == R;
	}

	public boolean isHere() {
		return this == H;
	}

	// CONVERSION

	public double toAngle() {
		if (this == E) {
			return 0;
		}
		if (this == ESE) {
			return 22.5;
		}
		if (this == SE) {
			return 45;
		}
		if (this == SSE) {
			return 67.5;
		}
		if (this == S) {
			return 90;
		}
		if (this == SSW) {
			return 112.5;
		}
		if (this == SW) {
			return 135;
		}
		if (this == WSW) {
			return 157.5;
		}
		if (this == W) {
			return 180;
		}
		if (this == WNW) {
			return 202.5;
		}
		if (this == NW) {
			return 225;
		}
		if (this == NNW) {
			return 247.5;
		}
		if (this == N) {
			return 270;
		}
		if (this == NNE) {
			return 292.5;
		}
		if (this == NE) {
			return 315;
		}
		if (this == ENE) {
			return 337.5;
		}

		throw new IllegalStateException("Direction has no absolute angle: " + name);
	}

	@Override
	public String toString() {
		return name;
	}
}