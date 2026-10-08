package engine.move;

import engine.entity.Entity;
import engine.geometry.ISU;
import game.Game;

/**
 * Une "caméra" sur le monde.
 *
 * La fenêtre de vue est un rectangle sur la carte : elle est définie par une
 * origine (coin supérieur gauche, en cm) et des dimensions (largeur et hauteur,
 * en cm). La classe View l'utilise pour l'affichage.
 */
public class ViewPort {

	private static final Game GAME;
	private static final ISU ISU;
	private static final boolean TORUS_X_AXIS;
	private static final boolean TORUS_Y_AXIS;
	static {
		GAME = Game.game();
		ISU = GAME.isu;
		TORUS_X_AXIS = GAME.torusOnXaxis;
		TORUS_Y_AXIS = GAME.torusOnYaxis;
	}

	/**
	 * Comportement du viewmport, cad de la caméra
	 * 
	 * >FREE : l'origine est définie explicitement et ne se déplace pas d'elle-même
	 * 
	 * 
	 * FOLLOW : l'origine est recalculée à chaque tick à jour afin qu'une entité
	 * cible reste au centre de la fenêtre de vue
	 * 
	 * 
	 * RAIL : l'origine défile à une vitesse constante (en cm par seconde).
	 */
	public enum Mode {
		FREE, FOLLOW, RAIL
	}

	private double originX_cm;
	private double originY_cm;

	private double width_cm;
	private double height_cm;

	private Mode mode;

	/**
	 * The targeted entity in FOLLOW mode, null otherwise.
	 */
	private Entity followed;

	/**
	 * Scroll speed in cm per second used in RAIL mode.
	 */
	private double railSpeedX_cm;
	private double railSpeedY_cm;

	/**
	 * Builds a free view port covering the whole world.
	 *
	 * @throws IllegalStateException if no current Game instance exists
	 */
	public ViewPort() {
		this(0, 0, worldWidth(), worldHeight());
	}

	/**
	 * Builds a free view port at the given origin and size, in cm.
	 *
	 * @param originX_cm the left edge of the view port, in cm
	 * @param originY_cm the top edge of the view port, in cm
	 * @param width_cm   the view port width, in cm (must be positive)
	 * @param height_cm  the view port height, in cm (must be positive)
	 * @throws IllegalStateException    if no current Game instance exists
	 * @throws IllegalArgumentException if width or height is not positive
	 */
	public ViewPort(double originX_cm, double originY_cm, double width_cm, double height_cm) {
		this.mode = Mode.FREE;
		this.followed = null;
		this.railSpeedX_cm = 0;
		this.railSpeedY_cm = 0;

		setSize(width_cm, height_cm);
		setOrigin(originX_cm, originY_cm);
	}

	// =========================
	// Mode selection
	// =========================

	/**
	 * Switches to FREE mode and places the origin explicitly.
	 *
	 * @param originX_cm the left edge of the view port, in cm
	 * @param originY_cm the top edge of the view port, in cm
	 */
	public void moveTo(double originX_cm, double originY_cm) {
		this.mode = Mode.FREE;
		this.followed = null;
		setOrigin(originX_cm, originY_cm);
	}

	/**
	 * Switches to FOLLOW mode so the view port keeps the given entity centered.
	 *
	 * @param e the entity to follow (must not be null)
	 * @throws IllegalArgumentException if e is null
	 */
	public void follow(Entity e) {
		if (e == null) {
			throw new IllegalArgumentException("followed entity cannot be null");
		}

		this.mode = Mode.FOLLOW;
		this.followed = e;
		recenterOnFollowed();
	}

	/**
	 * Switches to RAIL mode so the view port scrolls at a constant speed.
	 *
	 * @param speedX_cm horizontal scroll speed, in cm per second
	 * @param speedY_cm vertical scroll speed, in cm per second
	 */
	public void rail(double speedX_cm, double speedY_cm) {
		this.mode = Mode.RAIL;
		this.followed = null;
		this.railSpeedX_cm = speedX_cm;
		this.railSpeedY_cm = speedY_cm;
	}

	/**
	 * @return the current camera mode
	 */
	public Mode mode() {
		return mode;
	}

	/**
	 * @return the followed entity, or null when not in FOLLOW mode
	 */
	public Entity followed() {
		return followed;
	}

	public double getRailSpeedX_cm() {
		return railSpeedX_cm;
	}

	public double getRailSpeedY_cm() {
		return railSpeedY_cm;
	}

	// =========================
	// Tick
	// =========================

	/**
	 * Advances the camera for one model step. Called by the model after the
	 * entities have moved.
	 *
	 * @param delta_t the elapsed time since the previous tick, in seconds
	 */
	public void tick(double delta_t) {
		switch (mode) {
		case RAIL:
			setOrigin(originX_cm + railSpeedX_cm * delta_t, originY_cm + railSpeedY_cm * delta_t);
			break;
		case FOLLOW:
			recenterOnFollowed();
			break;
		case FREE:
		default:
			break;
		}
	}

	private void recenterOnFollowed() {
		if (followed == null || followed.dead()) {
			return;
		}

		ISU.Coord c = followed.center();
		setOrigin(c.x() - width_cm / 2.0, c.y() - height_cm / 2.0);
	}

	// =========================
	// Geometry / setters
	// =========================

	/**
	 * Sets the view port size, clamped so it never exceeds the world on a toric
	 * axis (no tiling) and stays at most the world size otherwise.
	 *
	 * @param w_cm the new width, in cm (must be positive)
	 * @param h_cm the new height, in cm (must be positive)
	 * @throws IllegalArgumentException if width or height is not positive
	 */
	public void resize(double w_cm, double h_cm) {
		setSize(w_cm, h_cm);
		setOrigin(originX_cm, originY_cm);
	}

	private void setSize(double w_cm, double h_cm) {
		if (w_cm <= 0 || h_cm <= 0) {
			throw new IllegalArgumentException("view port size must be positive");
		}

		this.width_cm = Math.min(w_cm, worldWidth());
		this.height_cm = Math.min(h_cm, worldHeight());
	}

	/**
	 * Sets the origin, clamping each axis according to the world geometry.
	 *
	 * @param x_cm the left edge, in cm
	 * @param y_cm the top edge, in cm
	 */
	private void setOrigin(double x_cm, double y_cm) {
		this.originX_cm = clampOriginX(x_cm);
		this.originY_cm = clampOriginY(y_cm);
	}

	/**
	 * On a toric axis the origin wraps into the world range so it stays a valid,
	 * canonical coordinate. On a non-toric axis the view port is pushed back so it
	 * remains entirely inside the world.
	 */
	private double clampOriginX(double x_cm) {
		if (TORUS_X_AXIS) {
			return wrap(x_cm, worldWidth());
		}
		return clampInside(x_cm, worldWidth(), width_cm);
	}

	private double clampOriginY(double y_cm) {
		if (TORUS_Y_AXIS) {
			return wrap(y_cm, worldHeight());
		}
		return clampInside(y_cm, worldHeight(), height_cm);
	}

	private static double wrap(double value, double period) {
		double r = value % period;

		if (r < 0) {
			r += period;
		}

		return r;
	}

	private static double clampInside(double origin, double world, double size) {
		double max = Math.max(0, world - size);
		return Math.max(0, Math.min(origin, max));
	}

	// =========================
	// Visibility / culling
	// =========================

	/**
	 * Tells whether an entity center lies within the view port, taking the toric
	 * geometry into account. Intended for culling, for example to destroy
	 * projectiles that scroll off screen.
	 *
	 * @param e the entity to test (must not be null)
	 * @return true if the entity center is inside the view port rectangle
	 * @throws IllegalArgumentException if e is null
	 */
	public boolean isVisible(Entity e) {
		if (e == null) {
			throw new IllegalArgumentException("entity cannot be null");
		}
		return this.isVisible(e.center(), e.size().x(), e.size().y());
	}

	public boolean isVisible(ISU.Coord coord, double marginX, double marginY) {

		if (coord == null) {
			return false;
		}

		return this.isVisible(coord.x(), coord.y(), marginX, marginY);
	}

	public boolean isVisible(double x, double y, double marginX, double marginY) {
		return inRange(x, originX_cm, width_cm, TORUS_X_AXIS, worldWidth(), marginX)
				&& inRange(y, originY_cm, height_cm, TORUS_Y_AXIS, worldHeight(), marginY);
	}

	/**
	 * Hard-clamps an entity so its center stays inside the view port rectangle,
	 * acting as walls on the view port edges. Game-specific behaviour: a game that
	 * does not want this simply never calls it.
	 *
	 * The clamp is toric-correct: the entity offset from the origin is measured
	 * modulo the world period, then limited to [0, size]; the result is mapped back
	 * to an absolute world coordinate and the entity is moved there.
	 *
	 * @param e the entity to confine (must be placed)
	 */
	public void confine(Entity e) {
		if (e == null || e.center() == null) {
			return;
		}

		ISU.Coord c = e.center();

		double nx = clampAxis(c.x(), originX_cm, width_cm, TORUS_X_AXIS, worldWidth());
		double ny = clampAxis(c.y(), originY_cm, height_cm, TORUS_Y_AXIS, worldHeight());

		if (nx != c.x() || ny != c.y()) {
			// translate by the correction so the entity ends exactly on the clamp.
			e.translate(ISU.new Vector(nx - c.x(), ny - c.y()));
		}
	}

	/**
	 * Clamps one coordinate inside [origin, origin + size] on one axis. On a toric
	 * axis the forward distance from the origin (modulo the world period) is what
	 * gets limited, so the clamp is correct even when the view port is larger than
	 * half the world.
	 *
	 * @return the clamped absolute coordinate, in cm
	 */
	private static double clampAxis(double value, double origin, double size, boolean onTorus, double period) {
		if (!onTorus) {
			return Math.max(origin, Math.min(value, origin + size));
		}

		double delta = ((value - origin) % period + period) % period; // [0, period)

		if (delta <= size) {
			return value; // already inside
		}

		// Outside: snap to the nearer edge (0 or size) of the view port.
		double clampedDelta = (delta - size < period - delta) ? size : 0.0;
		return origin + clampedDelta;
	}

	/**
	 * Tells whether a coordinate falls within the view port on one axis.
	 *
	 * On a toric axis the test must use the forward distance from the origin modulo
	 * the world period, not a distance centered on the origin: the view port may be
	 * larger than half the world (e.g. full height), in which case a centered
	 * unfolding would wrongly fold the far half of the view port back to the other
	 * side and report it as outside.
	 *
	 * @param value   the coordinate to test, in cm
	 * @param origin  the view port origin on this axis, in cm
	 * @param size    the view port size on this axis, in cm
	 * @param onTorus whether this axis wraps
	 * @param period  the world size on this axis, in cm (the toric period)
	 * @param margin  the margin corresponding to the distance over which the entity
	 *                should still be considered in the viewport after its
	 *                center has left it.
	 * @return true if value lies within [origin, origin + size] on this axis
	 */
	private static boolean inRange(double value, double origin, double size, boolean onTorus, double period,
			double margin) {
		if (!onTorus) {
			return value >= origin - margin && value <= origin + size + margin;
		}

		// Forward distance from origin, wrapped into [0, period).
		double delta = ((value - origin) % period + period) % period;

		// On étend la zone de visibilité vers l'extérieur :
		// - Soit l'entité est devant l'origine (jusqu'à la fin du viewport + marge)
		// - Soit l'entité est juste derrière l'origine (la marge arrière s'enroule à
		// period - margin)
		return delta <= size + margin || delta >= period - margin;
	}

	// =========================
	// Getters
	// =========================

	/**
	 * @return the left edge of the view port, in cm
	 */
	public double originX() {
		return originX_cm;
	}

	/**
	 * @return the top edge of the view port, in cm
	 */
	public double originY() {
		return originY_cm;
	}

	/**
	 * @return the view port width, in cm
	 */
	public double width_cm() {
		return width_cm;
	}

	/**
	 * @return the view port height, in cm
	 */
	public double height_cm() {
		return height_cm;
	}

	// =========================
	// World helpers
	// =========================

	/**
	 * 
	 * @return Game.game().width_cm
	 */
	private static double worldWidth() {
		return GAME.width_cm;
	}

	/**
	 * 
	 * @return Game.game().height_cm
	 */
	private static double worldHeight() {
		return GAME.height_cm;
	}

}