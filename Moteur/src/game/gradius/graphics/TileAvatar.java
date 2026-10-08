package game.gradius.graphics;

import engine.graphics.avatars.SpriteAvatar;
import engine.graphics.avatars.SpriteSheet;
import game.gradius.entity.Tile;
import oop.graphics.Graphics;

public class TileAvatar extends SpriteAvatar {

	private static final String SPRITESHEET_PATH = "src/game/gradius/graphics/background.png";

	private final Tile tile;

	public TileAvatar(Tile tile) {
		super(tile, SpriteSheet.getSpriteSheet(SPRITESHEET_PATH), tile.kind().cut, 1, 1, MAX_ZORDER);
		this.tile = tile;
	}

	@Override
	public void paint(Graphics g) {
		if (tile.center() == null) {
			return;
		}
		
		if (SpriteAvatar.showHitboxes) {
			paintHitbox(g);
		}

		if (!tile.isTopRow()) {
			super.paint(g);
			return;
		}

		// Top row: mirror vertically around the tile center, draw once, restore.
		int yCenter = cmToPixel(tile.center().y());
		Object saved = g.getTransform();
		g.translate(0, yCenter);
		g.scale(1, -1);
		g.translate(0, -yCenter);
		paintSprite(g);
		g.setTransform(saved);
	}
}