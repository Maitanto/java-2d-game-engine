package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class CannonAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/cannon_sprites.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(24, 19, 14, 5), new SpriteSheetCuttingDimension(42, 19, 14, 5),
			new SpriteSheetCuttingDimension(60, 19, 14, 5) };

	public CannonAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), CUTS, 2, 2, ZORDER_PLAYER-1);
	}

}