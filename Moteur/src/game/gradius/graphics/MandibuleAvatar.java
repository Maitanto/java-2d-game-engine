package game.gradius.graphics;

import engine.geometry.ISU;
import engine.graphics.avatars.SpriteAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import game.gradius.entity.Mandibule;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public abstract class MandibuleAvatar extends SpriteAvatar {

	protected MandibuleAvatar(Mandibule m, SpriteSheetCuttingDimension cut) {
		super(m, SpriteSheet.getSpriteSheet("src/game/gradius/graphics/Boss.png"), cut, 1, 1, ZORDER_BOSS-1);
	}

	@Override
	public void paint(Graphics g) {
		if (entity().center() == null || dead()) {
			return;
		}

		BufferedImage sprite = getSprite();
		if (sprite == null) {
			super.paint(g);
			return;
		}

		ISU.Coord coord = entity().center();
		ISU.Dimension size = entity().size();

		int width = Math.max(1, cmToPixel(size.x()));
		int height = Math.max(1, cmToPixel(size.y()));

		int xCenter = cmToPixel(coord.x());
		int yCenter = cmToPixel(coord.y());

		double angleRad = Math.toRadians(entity().orientation());

		Object saved = g.getTransform();
		g.translate(xCenter, yCenter);
		g.rotate(angleRad);
		g.drawImage(sprite, -width / 2, -height / 2, width, height);
		g.setTransform(saved);

		if (showHitboxes) {
			paintHitbox(g);
		}
	}

}
