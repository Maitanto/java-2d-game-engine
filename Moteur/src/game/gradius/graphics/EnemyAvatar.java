package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class EnemyAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/small_enemies.png";
	private static final String SPRITE_SHEET_PATH_DEATH = "src/game/gradius/graphics/vic_viper.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(8, 21, 15, 15), new SpriteSheetCuttingDimension(24, 21, 15, 15),
			new SpriteSheetCuttingDimension(41, 21, 15, 15) };
	private static final SpriteSheetCuttingDimension[] DEATH_CUTS = (new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14),
			new SpriteSheetCuttingDimension(161, 91, 18, 14) }); // 2ème frame dédoublé pour la faire durer plus
																	// longtemps. SpriteSheet est optimisé donc donner
																	// deux fois la même image n'est pas couteux, au
																	// contraire.

	private boolean deathAnimationStarted;

	public EnemyAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), CUTS, 1, 1, DEFAULT_ZORDER);
		this.deathAnimationStarted = false;
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (entity.dead()) {
			if (!this.deathAnimationStarted) {
				this.spriteSheet = SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH_DEATH);
				this.deathAnimationStarted = true;
				this.setCuts(DEATH_CUTS);
				this.setPlayOnce(true); // on joue la mort qu'une fois et on reste sur la dernière frame
			}
		}
		super.updateAnimation(delta_t);
	}

}
