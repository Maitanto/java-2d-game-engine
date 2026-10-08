package game.gradius.graphics;

import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import game.gradius.entity.Projectile;
import oop.graphics.Graphics;

public class ProjectileAvatar extends AnimationAvatar {

	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/vic_viper.png";
	private static final SpriteSheetCuttingDimension[] CUTS_LASER = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(126, 121, 10, 5), new SpriteSheetCuttingDimension(137, 121, 10, 5) };
	private static final SpriteSheetCuttingDimension[] CUTS_BLUE_ORB = new SpriteSheetCuttingDimension[] {
			new SpriteSheetCuttingDimension(89, 93, 10, 8), new SpriteSheetCuttingDimension(105, 92, 14, 10),
			new SpriteSheetCuttingDimension(124, 91, 16, 12) };

	private final Projectile projectile;
	private boolean isBlueOrb;

	public ProjectileAvatar(Projectile projectile) {
		super(projectile, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), CUTS_LASER, 1, 1, ZORDER_PLAYER-1);
		this.projectile = projectile;
		this.isBlueOrb = false;
	}

	@Override
	public void paint(Graphics g) {
		if (this.projectile.isBlueOrb() && !this.isBlueOrb) {
			this.setCuts(CUTS_BLUE_ORB);
			this.isBlueOrb = true;
		} else if (!this.projectile.isBlueOrb() && this.isBlueOrb) {
			this.setCuts(CUTS_LASER);
			this.isBlueOrb = false;
		}
		super.paint(g);
	}
}