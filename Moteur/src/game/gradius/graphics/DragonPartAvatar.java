package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import oop.graphics.Graphics;

public abstract class DragonPartAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/vulture_dragon.png";
	private static final SpriteSheetCuttingDimension[] DEATH_CUTS = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(99, 169, 48, 48), new SpriteSheetCuttingDimension(148, 169, 48, 48),
			new SpriteSheetCuttingDimension(197, 169, 48, 48), new SpriteSheetCuttingDimension(246, 169, 48, 48),
			new SpriteSheetCuttingDimension(295, 169, 48, 48) };

	private boolean deathAnimationStarted;

	protected DragonPartAvatar(Entity entity, SpriteSheetCuttingDimension[] orientation_cuts) {
		this(entity, orientation_cuts, 1, 1);
	}

	protected DragonPartAvatar(Entity entity, SpriteSheetCuttingDimension[] orientation_cuts, int multX, int multY) {
		super(entity, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), orientation_cuts, multX, multY,ZORDER_BOSS-2);
		this.deathAnimationStarted = false;
		this.setCuts(orientation_cuts);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g); // permet de mettre un breakpoint uniquement sur dragon
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (entity.dead()) {
			if (!this.deathAnimationStarted) {
				this.deathAnimationStarted = true;
				this.setCuts(DEATH_CUTS);
				this.setPlayOnce(true); // on joue la mort qu'une fosi et on reste sur la dernière frame
			}
			super.updateAnimation(delta_t);
		} else {
			this.frameIndex = getOrientation(entity.orientation());
		}
	}

	protected double normalizeAngle(double angle) {
		double normalized = angle % 360.0;

		if (normalized < 0.0) {
			normalized += 360.0;
		}

		return normalized;
	}

	protected int getOrientation(double orientation) {
		double angle = normalizeAngle(orientation);
		if (angle >= 11.25 && angle < 33.75)
			return 1;
		if (angle >= 33.75 && angle < 56.25)
			return 2;
		if (angle >= 56.25 && angle < 78.75)
			return 3;
		if (angle >= 78.75 && angle < 101.25)
			return 4;
		if (angle >= 101.25 && angle < 123.75)
			return 5;
		if (angle >= 123.75 && angle < 146.25)
			return 6;
		if (angle >= 146.25 && angle < 168.75)
			return 7;
		if (angle >= 168.75 && angle < 191.25)
			return 8;
		if (angle >= 191.25 && angle < 213.75)
			return 9;
		if (angle >= 213.75 && angle < 236.25)
			return 10;
		if (angle >= 236.25 && angle < 258.75)
			return 11;
		if (angle >= 258.75 && angle < 281.25)
			return 12;
		if (angle >= 281.25 && angle < 303.75)
			return 13;
		if (angle >= 303.75 && angle < 326.25)
			return 14;
		if (angle >= 326.25 && angle < 348.75)
			return 15;
		return 0;
	}

}