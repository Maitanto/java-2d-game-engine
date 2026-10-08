package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.Avatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class BossProjectileAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/Boss.png";
	private static final SpriteSheetCuttingDimension[] CUTS_ALIVE = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(390, 242, 25, 25) };
	private static final SpriteSheetCuttingDimension[] CUTS_DEATH = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(238, 326, 15, 15),
			new SpriteSheetCuttingDimension(259, 326, 15, 15),
			new SpriteSheetCuttingDimension(279, 326, 15, 15), };
	
	private boolean deathAnimationStarted;

	public BossProjectileAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), CUTS_ALIVE, 1, 1, Avatar.DEFAULT_ZORDER);
		this.deathAnimationStarted = false;
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (entity.dead()) {
			if (!this.deathAnimationStarted) {
				this.deathAnimationStarted = true;
				this.setCuts(CUTS_DEATH);
				this.setPlayOnce(true); // on joue la mort qu'une fois et on reste sur la dernière frame
			}
		}
		super.updateAnimation(delta_t);
	}
}
