package engine.graphics.avatars;

import engine.entity.Entity;
import oop.graphics.Graphics;

/**
 * intermediary "private" class, not supposed to be created outside of this
 * package
 * 
 * it's only use is to put there stuff common to both SpriteAvatar and
 * AnimationAvatar
 */
abstract class RessourceAvatar extends ShapeAvatar {

	public static boolean showHitboxes;

	protected SpriteSheet spriteSheet;

	protected RessourceAvatar(Entity entity, SpriteSheet spriteSheet, int multX, int multY, int z_order) {
		super(entity, multX, multY, z_order);
		this.spriteSheet = spriteSheet;
	}

	public static void toggleDebug() {
		showHitboxes = !showHitboxes;
	}
	@Override
	public void paint(Graphics g) {
		if (showHitboxes) {
			super.paint(g);
		}
	}

}
