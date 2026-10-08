package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class Laser1Avatar extends AnimationAvatar {
	private static final String SPRITE_SHEET = "src/game/gradius/graphics/small_enemies.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(126, 121, 10, 4), new SpriteSheetCuttingDimension(137, 121, 10, 4) };

	public Laser1Avatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET), CUTS, 1, 1, DEFAULT_ZORDER);
	}

}
