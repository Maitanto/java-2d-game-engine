package game.gradius.entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import engine.gal.arguments.Category;
import engine.graphics.View;
import engine.move.Model;
import engine.move.Model.TickSystem;
import engine.move.ViewPort;
import game.Game;
import game.gradius.graphics.TerrainKind;
import game.gradius.graphics.TileAvatar;

public class TerrainGenerator implements TickSystem {
	
	private final static Game GAME = Game.game();

	public enum Mode {
		ALL_AT_ONCE, CONTINUOUS
	}

	/**
	 * Generation mode. Switch here between full upfront generation and streaming.
	 */
	public static final Mode MODE = Mode.CONTINUOUS;

	/**
	 * How far ahead of the view port's right edge terrain is generated, in cm. A
	 * margin beyond the view port avoids popping at the edge.
	 */
	public static final double LOOKAHEAD_CM = 30.0;

	/**
	 * How far behind the view port's left edge a tile is kept before removal, cm.
	 */
	public static final double KEEP_BEHIND_CM = 20.0;

	private static final TerrainKind[] KINDS = { TerrainKind.BIG, TerrainKind.MEDIUM_1, TerrainKind.MEDIUM_2,
			TerrainKind.MEDIUM_3, TerrainKind.FLAT };

	private final Model model;
	private final View view;
	private final Random random;

	/**
	 * A live tile with the absolute (unwrapped) X span it was generated at, so we
	 * can decide when it has scrolled out behind the camera.
	 */
	private static final class Live {
		final Tile tile;
		final TileAvatar avatar;
		@SuppressWarnings("unused")
		final double absLeft, absRight;

		Live(Tile tile, TileAvatar avatar, double absLeft, double absRight) {
			this.tile = tile;
			this.avatar = avatar;
			this.absLeft = absLeft;
			this.absRight = absRight;
		}
	}

	// Streaming state, per row.
	private final Deque<Live> topTiles = new ArrayDeque<>();
	private final Deque<Live> bottomTiles = new ArrayDeque<>();
	private double topCursorAbs = 0.0; // absolute X up to which the top row is generated
	private double bottomCursorAbs = 0.0;

	public TerrainGenerator(Model model, View view) {
		this(model, view, new Random());
	}

	public TerrainGenerator(Model model, View view, long seed) {
		this(model, view, new Random(seed));
	}

	private TerrainGenerator(Model model, View view, Random random) {
		this.model = model;
		this.view = view;
		this.random = random;
	}

	/**
	 * Sets up the terrain according to the MODE. In ALL_AT_ONCE it fills the world
	 * immediately; in CONTINUOUS it seeds the visible span and registers a per-frame
	 * system on the model.
	 */
	public void generate() {
		// Terrain ignores terrain for collisions.
		Category.setInteraction(Category.Obstacle, Category.Obstacle, false);

		if (MODE == Mode.ALL_AT_ONCE) {
			fillWholeRow(GAME.width_cm, GAME.height_cm, true);
			fillWholeRow(GAME.width_cm, GAME.height_cm, false);
			return;
		}

		model.addTickSystem(this);
	}

	// =========================
	// ALL_AT_ONCE
	// =========================

	private void fillWholeRow(double worldW, double worldH, boolean topRow) {
		double cursor = 0.0;
		while (cursor < worldW) {
			TerrainKind kind = KINDS[random.nextInt(KINDS.length)];
			spawnTile(kind, cursor, worldH, topRow);
			cursor += kind.width_cm;
		}
	}

	// =========================
	// CONTINUOUS
	// =========================

	/**
	 * Per-frame: extend each row ahead of the view port and drop tiles behind it.
	 *
	 * @param delta_t elapsed time (unused; generation is position-driven)
	 */
	private void update(double delta_t) {
		ViewPort vp = model.viewPort();
		if (vp == null) {
			return;
		}

		double worldH = GAME.height_cm;

		// The view port origin is wrapped into [0, worldW). To drive absolute
		// cursors we track how far the camera has travelled; on a torus the rail
		// origin wraps, so we follow the rightmost visible absolute X by keeping
		// the cursor ahead of the wrapped origin within one period.
		double vpLeftAbs = alignAhead(vp.originX(), topCursorAbs, GAME.width_cm);
		double vpRightAbs = vpLeftAbs + vp.width_cm();

		extendRow(topTiles, true, vpRightAbs, worldH);
		extendRow(bottomTiles, false, vpRightAbs, worldH);

		double cutoff = vpLeftAbs - KEEP_BEHIND_CM;
		dropBehind(topTiles, cutoff);
		dropBehind(bottomTiles, cutoff);
	}

	/**
	 * Returns the absolute X of the wrapped origin, chosen in the period closest to
	 * (and not far behind) the current cursor, so absolute cursors keep increasing
	 * as the camera loops around the torus.
	 */
	private double alignAhead(double wrappedOrigin, double cursorAbs, double worldW) {
		// Candidate absolute positions equivalent to wrappedOrigin: ... + k*worldW.
		// Pick the one nearest to cursorAbs.
		double k = Math.floor((cursorAbs - wrappedOrigin) / worldW + 0.5);
		return wrappedOrigin + k * worldW;
	}

	private void extendRow(Deque<Live> row, boolean topRow, double vpRightAbs, double worldH) {
		double cursor = topRow ? topCursorAbs : bottomCursorAbs;

		while (cursor < vpRightAbs + LOOKAHEAD_CM) {
			TerrainKind kind = KINDS[random.nextInt(KINDS.length)];
			Live live = spawnTileAbs(kind, cursor, worldH, topRow);
			row.addLast(live);
			cursor += kind.width_cm;
		}

		if (topRow) {
			topCursorAbs = cursor;
		} else {
			bottomCursorAbs = cursor;
		}
	}

	private void dropBehind(Deque<Live> row, double cutoffAbs) {
		while (!row.isEmpty() && row.peekFirst().absRight < cutoffAbs) {
			Live live = row.pollFirst();
			model.remove(live.tile);
			view.remove(live.avatar);
		}
	}

	// =========================
	// Spawning
	// =========================

	/** Spawn for ALL_AT_ONCE (absolute == wrapped, world covered exactly once). */
	private void spawnTile(TerrainKind kind, double leftCm, double worldH, boolean topRow) {
		double h = kind.height_cm();
		double centerX = leftCm + kind.width_cm / 2.0;
		double centerY = topRow ? h / 2.0 : worldH - h / 2.0;

		Tile tile = new Tile(kind, topRow);
		tile.place(GAME.isu.new Coord(centerX, centerY));
		model.add(tile);
		view.add(new TileAvatar(tile));
	}

	/** Spawn for CONTINUOUS: place at wrapped X but track absolute span. */
	private Live spawnTileAbs(TerrainKind kind, double leftAbs, double worldH, boolean topRow) {
		double h = kind.height_cm();
		double worldW = GAME.width_cm;

		double centerXAbs = leftAbs + kind.width_cm / 2.0;
		double centerXWrapped = ((centerXAbs % worldW) + worldW) % worldW;
		double centerY = topRow ? h / 2.0 : worldH - h / 2.0;

		Tile tile = new Tile(kind, topRow);
		tile.place(GAME.isu.new Coord(centerXWrapped, centerY));
		model.add(tile);
		TileAvatar avatar = new TileAvatar(tile);
		view.add(avatar);

		return new Live(tile, avatar, leftAbs, leftAbs + kind.width_cm);
	}

	@Override
	public void tick(double delta_t) {
		this.update(delta_t);
	}
}