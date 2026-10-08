package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

//import engine.geometry.ISU;
//import engine.graphics.avatars.AnimationAvatar;
//import engine.graphics.avatars.ShapeAvatar;
//import game.Game;
//import oop.graphics.BufferedImage;
//import oop.graphics.Graphics;

// DragonHeadAvatar devrait avoir sa propre classe qui implémente RessourceAvatar
public class DragonHeadAvatar extends DragonPartAvatar {
	private static final SpriteSheetCuttingDimension[] ORIENTATION_CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(1, 1, 49, 49), new SpriteSheetCuttingDimension(51, 1, 49, 49),
			new SpriteSheetCuttingDimension(101, 1, 49, 49), new SpriteSheetCuttingDimension(151, 1, 49, 49),
			new SpriteSheetCuttingDimension(201, 1, 49, 49), new SpriteSheetCuttingDimension(251, 1, 49, 49),
			new SpriteSheetCuttingDimension(301, 1, 49, 49), new SpriteSheetCuttingDimension(351, 1, 49, 49),
			new SpriteSheetCuttingDimension(1, 51, 49, 49), new SpriteSheetCuttingDimension(51, 51, 49, 49),
			new SpriteSheetCuttingDimension(101, 51, 49, 49), new SpriteSheetCuttingDimension(151, 51, 49, 49),
			new SpriteSheetCuttingDimension(201, 51, 49, 49), new SpriteSheetCuttingDimension(251, 51, 49, 49),
			new SpriteSheetCuttingDimension(301, 51, 49, 49), new SpriteSheetCuttingDimension(351, 51, 49, 49) };

	public DragonHeadAvatar(Entity entity) {
		super(entity, ORIENTATION_CUTS);
	}
}
