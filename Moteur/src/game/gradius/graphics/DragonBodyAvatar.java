package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class DragonBodyAvatar extends DragonPartAvatar {
	private static final SpriteSheetCuttingDimension[] ORIENTATION_CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(1, 101, 33, 33), new SpriteSheetCuttingDimension(35, 101, 33, 33),
			new SpriteSheetCuttingDimension(69, 101, 33, 33), new SpriteSheetCuttingDimension(103, 101, 33, 33),
			new SpriteSheetCuttingDimension(137, 101, 33, 33), new SpriteSheetCuttingDimension(171, 101, 33, 33),
			new SpriteSheetCuttingDimension(205, 101, 33, 33), new SpriteSheetCuttingDimension(239, 101, 33, 33),
			new SpriteSheetCuttingDimension(1, 135, 33, 33), new SpriteSheetCuttingDimension(35, 135, 33, 33),
			new SpriteSheetCuttingDimension(69, 135, 33, 33), new SpriteSheetCuttingDimension(103, 135, 33, 33),
			new SpriteSheetCuttingDimension(137, 135, 33, 33), new SpriteSheetCuttingDimension(171, 135, 33, 33),
			new SpriteSheetCuttingDimension(205, 135, 33, 33), new SpriteSheetCuttingDimension(239, 135, 33, 33) };

	public DragonBodyAvatar(Entity entity) {
		super(entity, ORIENTATION_CUTS);
	}
}