package engine.move;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Objects;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.geometry.Grid.Cell;
import engine.logs.LoggerManager;
import game.Game;

public class Model {

	private static final Logger logger;
	private static final boolean LOGGING;
	private static final boolean FINER;
	private static final boolean SHOULD_DO_TICK_PROFILING_LOGGING;

	private static final DecimalFormat dfIndex;
	private static final DecimalFormat dfTime;

	static {
		// STATIC CONSTANTS INITIALIZATION
		logger = LoggerManager.getLogger(Model.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		FINER = logger.isLoggable(Level.FINER);
		SHOULD_DO_TICK_PROFILING_LOGGING = true;
		dfIndex = new DecimalFormat("00");
		dfTime = new DecimalFormat("####00");
	}

	// =========================
	// Fields
	// =========================

	// private final Grid grid;
	private final ISU isu;
	private final boolean SHOULD_DO_TICK_PROFILING;

	public final List<Entity> entities;

	private final Map<Entity, Stunt> stunts;

	private final Physics phy;

	public double delta_t;

	private ViewPort viewPort;
	private final Set<Entity> cullable = new HashSet<>();

	private final ArrayList<Entity> tickBuffer = new ArrayList<>();
	private final ArrayList<Entity> cullBuffer = new ArrayList<>();

	private final Set<TickSystem> tickSystems = new HashSet<>();

	// fields for tick time profiling
	public final int NB_LAST_TICK_TIME_SAVED = 96;
	private final int[] ARRAY_LAST_TICK_TIME_SAVED = new int[NB_LAST_TICK_TIME_SAVED];
	private int indexArrayTickTime = 0;
	private int tickTime = -1;
	private int sumTime = -1;
	private int minTime = 214748367;// big numbuh
	private int avgTime = -1;
	private int maxTime = -1;

	// =========================
	// Constructor
	// =========================

	public Model(Game game) {
		if (game == null) {
			throw new IllegalStateException("No current Game instance");
		}

		this.isu = game.isu;

		this.entities = new LinkedList<>();
		this.stunts = new HashMap<>();

		this.delta_t = 0.0;
		this.SHOULD_DO_TICK_PROFILING = true;

		phy = new Physics();
	}

	// =========================
	// Add / remove entities
	// =========================

	public void add(Entity entity) {
		if (!entities.contains(entity)) {
			entities.add(entity);
		}
	}

	public void add(Entity entity, Stunt stunt) {
		add(entity);
		setStunt(entity, stunt);
	}

	public void remove(Entity entity) {
		entities.remove(entity);
		stunts.remove(entity);
	}

	// =========================
	// Stunt
	// =========================

	public void setStunt(Entity entity, Stunt stunt) {
		ensureKnownEntity(entity);
		stunts.put(entity, stunt);
	}

	public Stunt stunt(Entity entity) {
		ensureKnownEntity(entity);
		return stunts.get(entity);
	}

	private void ensureKnownEntity(Entity entity) {
		if (!entities.contains(entity)) {
			throw new IllegalArgumentException("Entity is not in this model");
		}
	}

	// =========================
	// View port / culling
	// =========================

	/**
	 * Attaches a view port advanced on every tick.
	 *
	 * @param viewPort the camera to drive, or null to detach
	 */
	public void setViewPort(ViewPort viewPort) {
		this.viewPort = viewPort;
	}

	/**
	 * @return the attached view port, or null
	 */
	public ViewPort viewPort() {
		return viewPort;
	}

	/**
	 * Marks an entity to be killed automatically when it leaves the view port,
	 * typically a projectile. The entity must already be in the model.
	 *
	 * @param entity the entity to cull off-screen
	 */
	public void cullOffScreen(Entity entity) {
		ensureKnownEntity(entity);
		cullable.add(entity);
	}

	// =========================
	// Tick
	// =========================

	public void tick(double delta_t) {
		long startTickTime = System.currentTimeMillis();

		this.delta_t = delta_t;

		// On utilise la même liste au lieu d'en réallouer une à chaque tick
		tickBuffer.clear();
		tickBuffer.addAll(entities);

		for (int i = 0; i < tickBuffer.size(); i++) {
			Entity entity = tickBuffer.get(i);

			if (entity.dead()) {
				continue;
			}

			Stunt stunt = stunts.get(entity);

			if (stunt != null) {
				stunt.tick(delta_t);
			}

			if (entity.dead()) {
				continue;
			}

			phy.move(entity);

			long endTickTime = System.currentTimeMillis();

			if (SHOULD_DO_TICK_PROFILING)
				tickProfiling(startTickTime, endTickTime);
		}

		if (viewPort != null) {
			viewPort.tick(delta_t);

			if (!cullable.isEmpty()) {
				cullBuffer.clear();
				cullBuffer.addAll(cullable);
				for (int i = 0; i < cullBuffer.size(); i++) {
					Entity entity = cullBuffer.get(i);
					if (entity.dead()) {
						cullable.remove(entity);
						continue;
					}

					if (!viewPort.isVisible(entity)) {
						entity.kill();
						cullable.remove(entity);
					}
				}
			}
		}

		// Per-frame game systems (like View or the terrain generation), ticked after
		// the camera has advanced so they can react to the new viewport position.
		for (TickSystem system : tickSystems) {
			system.tick(delta_t);
		}
	}

	/**
	 * A lightweight per-frame system driven by the model tick, run after entities
	 * moved and the camera advanced.
	 */
	public interface TickSystem {
		void tick(double delta_t);
	}

	/**
	 * Registers a system ticked every frame.
	 *
	 * @param system the system to tick (must not be null)
	 */
	public void addTickSystem(TickSystem system) {
		tickSystems.add(Objects.requireNonNull(system, "system cannot be null"));
	}

	// =========================
	// Physics
	// =========================

	final class Physics {

		public ISU.Vector delta(Entity entity) {
			ISU.Vector speed = entity.linearSpeed();

			double delta_x = speed.x() * delta_t;
			double delta_y = speed.y() * delta_t;

			return isu.new Vector(delta_x, delta_y);
		}

		public void move(Entity entity) {
			rotate(entity);
			translate(entity);
		}

		private void rotate(Entity entity) {
			double angularSpeed = entity.angularSpeed();

			if (angularSpeed != 0.0) {
				entity.turn(angularSpeed * delta_t);
			}
		}

		private void translate(Entity entity) {
			ISU.Vector d = delta(entity);

			if (d.x() == 0.0 && d.y() == 0.0) {
				return;
			}

			// Déplacement en X
			if (d.x() != 0.0) {
				entity.translate(isu.new Vector(d.x(), 0));

				Entity other = intersectedEntity(entity);

				if (other != null) {
					entity.translate(isu.new Vector(-d.x(), 0));
					collision(entity, other);
				}
			}

			// Déplacement en Y
			if (d.y() != 0.0) {
				entity.translate(isu.new Vector(0, d.y()));

				Entity other = intersectedEntity(entity);

				if (other != null) {
					entity.translate(isu.new Vector(0, -d.y()));
					collision(entity, other);
				}
			}

		}

		private Entity intersectedEntity(Entity entity) {
			/**
			 * Phase large : seules les entités partageant au moins une cellule de la grille
			 * avec celle-ci sont susceptibles de l'intersecter. On parcourt les cellules
			 * occupées par cette entité plutôt que le monde entier, transformant ainsi un
			 * balayage en O(N) par déplacement en une opération proportionnelle au nb
			 * d'entités dans la case. Un ensemble des éléments visités permet d'éviter de
			 * tester deux fois une même entité lorsqu'elle chevauche plusieurs cellules.
			 */
			Set<Entity> visited = new HashSet<>();

			for (Cell cell : entity.occupied()) {
				for (Entity other : cell.entities()) {
					if (entity == other) {
						continue;
					}

					if (!visited.add(other)) {
						continue; // already tested via another shared cell
					}

					if (other.dead()) {
						continue;
					}

					if (!entity.category().interactsWith(other.category())) {
						continue;
					}

					if (entity.intersects(other)) {
						return other;
					}
				}
			}

			return null;
		}

		private void collision(Entity entity, Entity other) {
			Stunt stunt = stunts.get(entity);

			if (stunt != null) {
				stunt.collision(other);
			}

			Stunt otherStunt = stunts.get(other);

			if (otherStunt != null) {
				otherStunt.collision(entity);
			}
		}

	}

	// =========================
	// Profiling
	// =========================

	/**
	 * Je confirme que le tickProfiling fonctionne, je l'ai testé dans tous les
	 * sens. Si ça affiche que des 0 c'est que notre tick est très rapide pour le
	 * moment. À voir si ça reste le cas plus on ajoutera de contenu et d'entités.
	 * 
	 * @param startTickTime
	 * @param endTickTime
	 */
	private void tickProfiling(long startTickTime, long endTickTime) {
		long elapsedTickTime = endTickTime - startTickTime;
		tickTime = (int) elapsedTickTime;
		sumTime = -1;
		minTime = 214748367;// big numbuh
		avgTime = -1;
		maxTime = -1;

		this.ARRAY_LAST_TICK_TIME_SAVED[this.indexArrayTickTime++] = tickTime;
		this.indexArrayTickTime %= this.NB_LAST_TICK_TIME_SAVED;
		for (int i = 0; i < this.NB_LAST_TICK_TIME_SAVED; i++) {
			int localTime = this.ARRAY_LAST_TICK_TIME_SAVED[i];
			sumTime += localTime;
			avgTime = sumTime / NB_LAST_TICK_TIME_SAVED;
			if (localTime < minTime)
				minTime = localTime;
			if (localTime > maxTime)
				maxTime = localTime;
		}
		if (LOGGING && FINER && SHOULD_DO_TICK_PROFILING_LOGGING) {
			StringBuilder sb = new StringBuilder();
			sb.append("Paiting profiling \n");
			sb.append("   START: ");
			sb.append(startTickTime);
			sb.append(" ; END: ");
			sb.append(endTickTime);
			sb.append(" ; ELAPSED: ");
			sb.append(elapsedTickTime);
			sb.append('\n');
			sb.append("   time: ");
			sb.append(tickTime);
			sb.append(" ; min: ");
			sb.append(minTime);
			sb.append(" ; avg: ");
			sb.append(avgTime);
			sb.append(" ; max: ");
			sb.append(maxTime);
			sb.append(" ; sum: ");
			sb.append(sumTime);
			for (int i = 0; i < this.NB_LAST_TICK_TIME_SAVED; i++) {
				if (i % 8 == 0) {
					sb.append("\n   ");
				}
				sb.append("[");
				sb.append(dfIndex.format(i));
				sb.append(":");
				sb.append(dfTime.format(this.ARRAY_LAST_TICK_TIME_SAVED[i]));
				sb.append("]");
			}
			logger.log(Level.FINER, sb.toString());
		}
	}

	public int getTickTime() {
		return this.tickTime;
	}

	public int getMinTickTime() {
		return this.minTime;
	}

	public int getAvgTickTime() {
		return this.avgTime;
	}

	public int getMaxTickTime() {
		return this.maxTime;
	}

	public String getFormattedTickTime() {
		return dfTime.format(this.tickTime);
	}

	public String getFormattedMinTickTime() {
		return dfTime.format(this.minTime);
	}

	public String getFormattedAvgTickTime() {
		return dfTime.format(this.avgTime);
	}

	public String getFormattedMaxTickTime() {
		return dfTime.format(this.maxTime);
	}
}