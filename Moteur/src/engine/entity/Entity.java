// == ENTITY ==

package engine.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import engine.gal.arguments.Category;
import engine.geometry.Grid;
import engine.geometry.Grid.Cell;
import engine.geometry.ISU;
import engine.graphics.View;
import engine.move.Model;
import engine.shape.Bounding;
import engine.shape.iShape;
import game.Game;
import engine.gal.Bot;

public abstract class Entity {

	protected static Game GAME;
	protected static Model MODEL;
	protected static Grid GRID;
	protected static ISU ISU;
	protected static View VIEW;
	protected static double CM_PER_CELL;

	public static void bindToGame(Game game) {
		if (game == null) {
			throw new IllegalStateException("No current Game instance");
		}

		GAME = game;
		MODEL = game.model;
		GRID = game.grid;
		ISU = game.isu;
		CM_PER_CELL = game.cmPerCell;
		VIEW = game.view;
	}

	// =========================
	// Core fields
	// =========================
	protected final String name;

	private ISU.Dimension size; // dimension de l'entité en cm
	private ISU.Dimension step; // dimension d'un pas de déplacement en cm
	private Grid.Position position; // position dans la grille
	private ISU.Coord center; // coordonnées en cm du centre de l'entité

	private double orientation_degree;
	private boolean alive;

	private Bounding hitbox;

	private boolean boundingNeedToBeRebuild = true;
	private final Set<Cell> occupied;

	// optimizations d'Entity
	private Set<Long> cellFootprint;
	private Set<Long> scratchFootprint;

	private ISU.Vector linearSpeed;
	private double angularSpeed;

	private static final double EPSILON = 1e-9;

	// =========================
	// Constructor
	// =========================

	protected Entity(String name) {
		cellFootprint = new HashSet<>();
		scratchFootprint = new HashSet<>();
		alive = true;

		this.name = Objects.requireNonNull(name, "name cannot be null");

		this.hitbox = new Bounding();
		this.occupied = new HashSet<>();

		this.linearSpeed = ISU.new Vector(0, 0);
		this.angularSpeed = 0;
	}

	// =========================
	// Setters / configuration
	// =========================

	public void setSize(Grid.Dimension dimension) {
		Objects.requireNonNull(dimension, "dimension cannot be null");
		this.size = dimension.toISUDimension();
		this.boundingNeedToBeRebuild = true;
	}

	public void setSize(ISU.Dimension dimension) {
		this.size = Objects.requireNonNull(dimension, "dimension cannot be null");
		this.boundingNeedToBeRebuild = true;
	}

	public void setStep(ISU.Dimension step) {
		this.step = Objects.requireNonNull(step, "step cannot be null");
	}

	public void place(Grid.Position position) {
		setPosition(position);
		rebuildBoundingIfNeeded();
		deploy();
	}

	public void place(ISU.Coord center) {
		setCoord(center);
		rebuildBoundingIfNeeded();
		deploy();
	}

	public void placeRelativeTo(Entity anchor, double offsetX, double offsetY) {
		if (anchor == null || anchor.center() == null) {
			throw new IllegalArgumentException("anchor must be placed before placing entity");
		}

		place(ISU.new Coord(anchor.center().x() + offsetX, anchor.center().y() + offsetY));
	}

	protected void setPosition(Grid.Position position) {
		this.position = Objects.requireNonNull(position, "position cannot be null");
		ISU.Coord newCenter = position.toISUCoord();
		// New center object: the shared reference the shapes hold is now stale.
		if (newCenter != this.center) {
			this.boundingNeedToBeRebuild = true;
		}
		this.center = newCenter;
	}

	protected void setCoord(ISU.Coord center) {
		Objects.requireNonNull(center, "center cannot be null");
		// Only a replacement of the center object breaks the shapes' shared
		// reference; an in-place mutation (same object) keeps them in sync.
		if (center != this.center) {
			this.boundingNeedToBeRebuild = true;
		}
		this.center = center;
		this.position = center.toGridPosition();
	}

	public void kill() {
		if (!alive) {
			return;
		}

		this.alive = false;
		retract();
	}

	public void revive() {
		if (alive) {
			return;
		}

		this.alive = true;
		rebuildBoundingIfNeeded();
		deploy();
	}

	public void setLinearSpeed(ISU.Vector linearSpeed) {
		this.linearSpeed = linearSpeed;
	}

	public void setAngularSpeed(double angularSpeed) {
		this.angularSpeed = angularSpeed;
	}
	// =========================
	// Getters
	// =========================

	public String name() {
		return name;
	}

	public ISU.Coord center() {
		return center;
	}

	public Grid.Position position() {
		return position;
	}

	public ISU.Dimension size() {
		return size;
	}

	public ISU.Dimension step() {
		return step;
	}

	public double orientation() {
		return orientation_degree;
	}

	public Set<Cell> occupied() {
		return occupied;
	}

	protected Bounding hitbox() {
		return hitbox;
	}

	/**
	 * @return the shapes composing this entity's hitbox, for debug rendering
	 */
	public Iterable<iShape> hitboxShapes() {
		return hitbox.shapes();
	}

	public boolean alive() {
		return alive;
	}

	public boolean dead() {
		return !alive;
	}

	public ISU.Vector linearSpeed() {
		return this.linearSpeed;
	}

	public double angularSpeed() {
		return this.angularSpeed;
	}

	// =========================
	// Translation
	// =========================

	public void translate(Grid.Vector v) {
		Objects.requireNonNull(v, "vector cannot be null");
		ensurePositionIsSet();

		position.translate(v);
		setPosition(position);

		rebuildBoundingIfNeeded();
		deploy();
	}

	public void translate(ISU.Vector v) {
		Objects.requireNonNull(v, "vector cannot be null");
		ensureCenterIsSet();

		center.translate(v);
		setCoord(center);

		rebuildBoundingIfNeeded();
		deploy();
	}

	// =========================
	// Rotation
	// =========================

	/**
	 * Rotation autour du centre de l'entité.
	 *
	 * @param angle_degree angle ajouté à l'orientation actuelle
	 */
	public void turn(double angle_degree) {
		this.orientation_degree = normalizeAngle(this.orientation_degree + angle_degree);

		this.boundingNeedToBeRebuild = true; // orientation changed: Rect angles are baked in
		rebuildBoundingIfNeeded();
		deploy();
	}

	private double normalizeAngle(double angle_degree) {
		double angle = angle_degree % 360.0;

		if (angle < 0) {
			angle += 360.0;
		}

		return angle;
	}

	public void setOrientation(double angleDegree) {
		this.orientation_degree = normalizeAngle(angleDegree);
	}

	public void forceOrientation(double angleDegree) {
		this.orientation_degree = normalizeAngle(angleDegree);

		this.boundingNeedToBeRebuild = true; // orientation changed
		rebuildBoundingIfNeeded();
		deploy();
	}

	// =========================
	// Move
	// =========================

	public void moveNorth(int nStep) {
		ensureStepIsSet();
		moveNorth(nStep * step.y());
	}

	public void moveSouth(int nStep) {
		ensureStepIsSet();
		moveSouth(nStep * step.y());
	}

	public void moveEast(int nStep) {
		ensureStepIsSet();
		moveEast(nStep * step.x());
	}

	public void moveWest(int nStep) {
		ensureStepIsSet();
		moveWest(nStep * step.x());
	}

	/**
	 * Déplacement vers l'est en cm.
	 */
	public void moveEast(double length_cm) {
		translate(ISU.new Vector(length_cm, 0));
	}

	public void moveWest(double length_cm) {
		translate(ISU.new Vector(-length_cm, 0));
	}

	public void moveNorth(double length_cm) {
		translate(ISU.new Vector(0, -length_cm));
	}

	public void moveSouth(double length_cm) {
		translate(ISU.new Vector(0, length_cm));
	}

	// =========================
	// Bounding / collision
	// =========================

	public boolean intersects(Entity e) {
		Objects.requireNonNull(e, "entity cannot be null");
		return this.hitbox.intersects(e.hitbox);
	}

	public double distanceCenterToCenter(Entity e) {
		Objects.requireNonNull(e, "entity cannot be null");
		ensureCenterIsSet();
		e.ensureCenterIsSet();

		return this.center.distanceTo(e.center);
	}

	/**
	 * Les sous-classes doivent définir leur propre hitbox ici.
	 */
	protected abstract void setBounding();

	/**
	 * Rebuilds the hitbox shapes only if they are stale (size/orientation changed,
	 * or the center reference was replaced). Otherwise the existing shapes still
	 * track the entity (their center is the same object the entity mutates in
	 * place), so we save the per-frame cost of recreating every shape.
	 */
	private void rebuildBoundingIfNeeded() {
		if (boundingNeedToBeRebuild) {
			setBounding();
			boundingNeedToBeRebuild = false;
		}
	}

	protected void clearBounding() {
		this.hitbox = new Bounding();
	}

	protected void addBounding(iShape shape) {
		Objects.requireNonNull(shape, "shape cannot be null");
		this.hitbox.add(shape);
	}

	// =========================
	// GAL
	// =========================

	public Grid grid() {
		return GRID;
	}

	private Bot bot;
	private Category category = Category.Obstacle;

	public Category category() {
		return category;
	}

	public void category(Category category) {
		if (category == null) {
			throw new IllegalArgumentException("category cannot be null");
		}

		this.category = category;
	}

	public Bot bot() {
		return bot;
	}

	public void bot(Bot bot) {
		this.bot = bot;
	}

	public String debugInfoMove() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.center().toStringRounded());
		sb.append("{lSpeed:");
		sb.append(this.linearSpeed.toStringRounded());
		sb.append(";aSpeed:");
		sb.append(this.angularSpeed);
		sb.append("}");
		return sb.toString();
	}

	public String debugInfoBehavior() {
		StringBuilder sb = new StringBuilder();
		if (this.bot != null && bot.state() != null) {
			sb.append("Hp:");
			sb.append(this.bot.life());
			sb.append(" | State:");
			sb.append(this.bot.state().mode());
			sb.append(" | Action:");
			sb.append(this.bot.lastAction() != null ? this.bot.lastAction().toString() : "none");
		}

		return sb.toString();
	}

	// =========================
	// Deploy in grid
	// =========================

	public void deploy() {
		// On calcule d'abord le footprint de la nouvelle cellule. Si elle correspond à
		// celle du dernier déploiement, l'entité occupe toujours exactement les mêmes
		// cellules : rien à faire.
		// Cela évite les opérations de retrait et d'occupation répétées dans le cas
		// très courant des petits déplacements.
		fillFootprint(scratchFootprint);

		if (scratchFootprint.equals(cellFootprint)) {
			return;
		}

		retract();

		for (long packed : scratchFootprint) {
			int x = (int) (packed >> 32);
			int y = (int) packed;
			occupy(GRID.new Position(x, y));
		}

		Set<Long> tmp = cellFootprint;
		cellFootprint = scratchFootprint;
		scratchFootprint = tmp;
	}

	private void fillFootprint(Set<Long> out) {
		out.clear();

		for (iShape.Box box : this.hitbox.boundingBoxes()) {
			int minX = toCellIndexMin(box.minX());
			int maxX = toCellIndexMax(box.maxX());
			int minY = toCellIndexMin(box.minY());
			int maxY = toCellIndexMax(box.maxY());

			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					out.add(((long) x << 32) | (y & 0xffffffffL));
				}
			}
		}
	}

	private int toCellIndexMin(double coord_cm) {
		double cmPerCell = Game.game().cmPerCell;
		return (int) Math.floor(coord_cm / cmPerCell + 0.5 + EPSILON);
	}

	private int toCellIndexMax(double coord_cm) {
		double cmPerCell = Game.game().cmPerCell;
		return (int) Math.floor(coord_cm / cmPerCell + 0.5 - EPSILON);
	}

	private void occupy(Grid.Position position) {
		Cell cell = GRID.occupy(this, position);
		this.occupied.add(cell);
	}

	public void retract() {
		for (Cell cell : this.occupied) {
			GRID.retract(this, cell);
		}

		this.occupied.clear();
		this.cellFootprint.clear();
	}

	// =========================
	// Guards
	// =========================

	private void ensurePositionIsSet() {
		if (position == null) {
			throw new IllegalStateException("Entity position is not set");
		}
	}

	private void ensureCenterIsSet() {
		if (center == null) {
			throw new IllegalStateException("Entity center is not set");
		}
	}

	private void ensureStepIsSet() {
		if (step == null) {
			throw new IllegalStateException("Entity step is not set");
		}
	}

}