package game.gradius.graphics;

import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public enum TerrainKind {

	/** Big mountain: base + central peak + two shoulders. */
	BIG(new SpriteSheetCuttingDimension(24, 92, 1016, 316), 60.0, new Box[] { new Box(0.00, 1.00, 0.0, 0.30), // base,
																												// full
																												// width
			new Box(0.22, 0.48, 0.0, 0.75), // left shoulder
			new Box(0.48, 0.56, 0.0, 0.98), // central peak
			new Box(0.56, 0.80, 0.0, 0.60)// right shoulder
	}),

	/** Medium mountain variant 1: base + central peak. */
	MEDIUM_1(new SpriteSheetCuttingDimension(27, 508, 294, 118), 24.0,
			new Box[] { new Box(0.00, 1.00, 0.0, 0.30), new Box(0.40, 0.60, 0.0, 0.98) }),

	/** Medium mountain variant 2: base + central peak. */
	MEDIUM_2(new SpriteSheetCuttingDimension(360, 508, 330, 116), 24.0,
			new Box[] { new Box(0.00, 1.00, 0.0, 0.30), new Box(0.40, 0.60, 0.0, 0.98) }),

	/** Medium mountain variant 3: base + central peak. */
	MEDIUM_3(new SpriteSheetCuttingDimension(717, 544, 318, 80), 24.0,
			new Box[] { new Box(0.00, 1.00, 0.0, 0.30), new Box(0.35, 0.65, 0.0, 0.98) }),

	/** Flat ground: just a base (one single full box). */
	FLAT(new SpriteSheetCuttingDimension(24, 676, 320, 26), 24.0, new Box[] { new Box(0.00, 1.00, 0.0, 1.00) });

	/**
	 * World cm per source pixel of height. One knob for the whole terrain
	 * thickness; keeps every sprite's aspect ratio.
	 */
	public static final double CM_PER_SRC_PX = 0.12;

	/**
	 * A collision box as start/end fractions on each axis, all in [0, 1]. x0..x1:
	 * left-to-right span. y0..y1: span from the anchored edge (0) to the inner tip
	 * (1).
	 */
	public static final class Box {
		public final double x0, x1, y0, y1;

		public Box(double x0, double x1, double y0, double y1) {
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
		}
	}

	/** Source rectangle in background.png, in image pixels. */
	public final SpriteSheetCuttingDimension cut;

	/** Tile width in the world, in cm. */
	public final double width_cm;

	/** Collision boxes in normalized span coordinates. */
	public final Box[] boxes;

	TerrainKind(SpriteSheetCuttingDimension cut, double width_cm, Box[] boxes) {
		this.cut = cut;
		this.width_cm = width_cm;
		this.boxes = boxes;
	}

	/**
	 * @return the tile height in the world, in cm, from the source pixel height
	 */
	public double height_cm() {
		return cut.h * CM_PER_SRC_PX;
	}
}