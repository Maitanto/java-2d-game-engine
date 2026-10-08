package engine.graphics.avatars;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class SpriteSheet {

	private static final Map<String, SpriteSheet> SPRITESHEETS;

	static {
		SPRITESHEETS = new HashMap<>();
	}

	public static SpriteSheet getSpriteSheet(String path) {
		SpriteSheet s = SPRITESHEETS.get(path);
		if (s == null) {
			s = new SpriteSheet(path);
		}
		return s;
	}

	protected final String PATH;
	protected final Map<SpriteSheetCuttingDimension, BufferedImage> SPRITES;
	protected BufferedImage img;

	private SpriteSheet(String path) {
		SPRITESHEETS.put(path, this);
		this.PATH = path;
		this.SPRITES = new HashMap<>();
		this.img = null;
	}

	/**
	 * dumb class to deal more easily with sprites cutting
	 */
	public static class SpriteSheetCuttingDimension {
		public int x, y, h, w;

		public SpriteSheetCuttingDimension(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}

		@Override
		public int hashCode() {
			return Objects.hash(h, w, x, y);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (obj instanceof SpriteSheetCuttingDimension other) {
				return h == other.h && w == other.w && x == other.x && y == other.y;
			}
			return false;
		}

	}

	private void init(Graphics g) {
		if (this.img == null)
			this.img = g.load(PATH);
	}

	/**
	 * Load a sprite base with a give name
	 * 
	 * @param spriteName cannot give same name to different sprites
	 * @param cutting    the 'dimension' of the cut to do in the spritesheet
	 * @return the sprite corresponding to the cut in the spritesheet
	 * 
	 */
	protected BufferedImage getSprite(Graphics g, SpriteSheetCuttingDimension cutting) {
		this.init(g);
		return this.getSprite(cutting);
	}
	
	private BufferedImage getSprite(SpriteSheetCuttingDimension cutting) {
		BufferedImage sprite = SPRITES.get(cutting);
		if (sprite == null)
			sprite = img.getSubimage(cutting.x, cutting.y, cutting.w, cutting.h);
		SPRITES.put(cutting, sprite);
		return sprite;
	}

	/**
	 * Load multiple sprites with default names
	 * 
	 * @param g          graphic object
	 * @param spriteCuts the cut dimensions to apply to the spritesheet
	 * @return the sprites corresponding to the cuts in the spritesheet
	 */
	protected BufferedImage[] getSprites(Graphics g, SpriteSheetCuttingDimension[] spriteCuts) {
		this.init(g);
		return this.getSprites(spriteCuts);
	}
	
	private BufferedImage[] getSprites(SpriteSheetCuttingDimension[] spriteCuts) {
		int nbSprites = spriteCuts.length;
		BufferedImage[] sprites = new BufferedImage[nbSprites];
		for (int i = 0; i < nbSprites; i++) {
			sprites[i] = this.getSprite(spriteCuts[i]);
		}
		return sprites;
	}

	@Override
	public String toString() {
		return "SPRITESHEET[" + this.PATH + "]";
	}

}
