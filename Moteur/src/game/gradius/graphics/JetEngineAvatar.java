package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class JetEngineAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET = "src/game/gradius/graphics/vic_viper.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(5, 17, 12, 8), new SpriteSheetCuttingDimension(5, 29, 12, 8),
			new SpriteSheetCuttingDimension(5, 42, 12, 8) };

	public JetEngineAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET), CUTS, 1, 1, ZORDER_PLAYER+1);

	}

}
