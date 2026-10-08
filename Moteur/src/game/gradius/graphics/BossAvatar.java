package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;

public class BossAvatar extends AnimationAvatar {

	private static final String SPRITE_SHEET_PATH_BOSS = "src/game/gradius/graphics/Boss.png";
	private static final String SPRITE_SHEET_PATH_DEATH = "src/game/gradius/graphics/vic_viper.png";
	private static final SpriteSheetCuttingDimension[] CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(218, 140, 104, 128) };
	private static final SpriteSheetCuttingDimension[] DEATH_CUTS = (new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14),
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14),
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14),
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14),
			new SpriteSheetCuttingDimension(143, 91, 16, 14), new SpriteSheetCuttingDimension(161, 91, 18, 14) });
	// Grâce aux optimisations dans ma SpriteSheet, c'est optimisé de faire comme ça
	// et plus simple que de refactoriser encore mon code pour faire autrement.
	// Ça à l'air moche comme ça, mais un tableau d'image répétée comme celui-ci est
	// plus opti qu'un tableau de même taille qu'avec des iamges différentes.

	private boolean deathAnimationStarted;

	public BossAvatar(Entity entity) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH_BOSS), CUTS, 1, 1, ZORDER_BOSS);
		deathAnimationStarted = false;
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (entity.dead()) {
			if (!this.deathAnimationStarted) {
				this.multX=0.7;
				this.multY=0.7;
				this.spriteSheet = SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH_DEATH);
				this.deathAnimationStarted = true;
				this.setCuts(DEATH_CUTS);
				this.setPlayOnce(true); // on joue la mort qu'une fois et on reste sur la dernière frame
			}
		}
		super.updateAnimation(delta_t);
	}

}
