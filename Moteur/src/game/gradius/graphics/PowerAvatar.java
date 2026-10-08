package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class PowerAvatar extends AnimationAvatar {

	private static final String SPRITE_SHEET = "src/game/gradius/graphics/vic_viper.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(28, 149, 16, 16), new SpriteSheetCuttingDimension(47, 149, 16, 16),
			new SpriteSheetCuttingDimension(67, 150, 14, 14), new SpriteSheetCuttingDimension(86, 150, 14, 14),
			new SpriteSheetCuttingDimension(103, 151, 12, 12), new SpriteSheetCuttingDimension(119, 151, 12, 12),
			new SpriteSheetCuttingDimension(136, 152, 10, 10), new SpriteSheetCuttingDimension(152, 152, 10, 10),
			new SpriteSheetCuttingDimension(169, 153, 8, 8) };

	public PowerAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET), CUTS, 1, 1, DEFAULT_ZORDER);
	}
}