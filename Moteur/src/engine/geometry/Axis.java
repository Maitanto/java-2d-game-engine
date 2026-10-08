package engine.geometry;

/**
 * @apiNote Axis of a Torus with origin at 0
 * @implNote Coordinate ranges in [ -perimeter/2 ; perimeter/2 [
 * @implNote Negative coordinate are allowed
 */

class Axis {

	// FIELDS

	private boolean onTorus;
	private double perimeter;
	private double halfPerimeter;

	// CONSTRUCTOR

	Axis(boolean onTorus, double perimeter) {
		this.onTorus = onTorus;
		if (perimeter <= 0)
			throw new IllegalArgumentException("perimeter must be positif");
		this.perimeter = perimeter;
		this.halfPerimeter = perimeter / 2;

	}

	// NORMALIZE INTEGER LENGTH

	/**
	 * @apiNote normalize _integer length_ according to the geometry
	 * @implNote returns positive values
	 * @return
	 *         <UL>
	 *         <LI>length % perimeter __&in; [0, perimeter-1]__ if onTorus</LI>
	 *         <LI>length if !onTorus</LI>
	 *         </UL>
	 */
	protected int normalize(int length) {
		if (onTorus) {
			return modp(length, (int) (perimeter));
		}
		return length;
	}

	/**
	 * @apiNote compute length modulo perimeter
	 * @return length % perimeter __&in; [0, perimeter-1]__
	 */
	private int modp(int length, int perimeter) {
		// modulo negatif reste negatif
		int r = length % perimeter;

		if (r < 0) {
			r += perimeter;
		}

		return r;
	}

	// NORMALIZE REAL LENGTH

	/**
	 * @apiNote normalize _real length_ according to the geometry
	 * @return
	 *         <UL>
	 *         <LI>length module perimeter <I>&in;[0 ; perimeter[</I> if
	 *         onTorus</LI>
	 *         <LI>length if !onTorus</LI>
	 *         </UL>
	 */
	double normalize(double length) {
		if (onTorus)
			return modp(length, perimeter);
		return length;
	}

	/**
	 * @apiNote compute length modulo perimeter
	 * @return length % perimeter __&in; [0, perimeter[__
	 */
	private double modp(double length, double perimeter) {
		double r = length % perimeter;

		if (r < 0) {
			r += perimeter;
		}
		return r;
	}

	// DISTANCE

	/**
	 * @apiNote The distance on a Torus is that of the shortest path, sometimes
	 *          going in the opposite direction and across the border is shorter.
	 * @implNote Look for the detail on internet.
	 */
	double distance(double position1, double position2) {
		if (this.onTorus) {
			double d = Math.abs(position1 - position2);
			return Math.min(d, perimeter - d);
		} else {
			return Math.abs(position1 - position2);
		}
	}

	/**
	 * Déplie x autour d'origin sur un axe torique, afin que
	 * x - origin représente le plus court déplacement.
	 *
	 * @param origin coordonnée de référence
	 * @param x      coordonnée à déplier
	 * @return une coordonnée équivalente à x, proche de origin
	 */
	double euclideanFrom(double origin, double x) {

		if (!onTorus) {
			return x;
		}

		double res = x;

		if (res - origin > halfPerimeter)
			res -= perimeter;
		else if (res - origin < -halfPerimeter)
			res += perimeter;

		return res;
	}
}
