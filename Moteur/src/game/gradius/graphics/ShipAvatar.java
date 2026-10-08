package game.gradius.graphics;

import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.SpriteSheet;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import game.gradius.entity.Ship;
import oop.graphics.Graphics;

public class ShipAvatar extends AnimationAvatar {
	private static final String SPRITE_SHEET_PATH = "src/game/gradius/graphics/vic_viper.png";

	private enum State {
		LIVE(new SpriteSheetCuttingDimension[] { new SpriteSheetCuttingDimension(88, 13, 34, 18) }),
		DASH(new SpriteSheetCuttingDimension[] { new SpriteSheetCuttingDimension(53, 13, 34, 18),
				new SpriteSheetCuttingDimension(18, 13, 34, 18), new SpriteSheetCuttingDimension(158, 13, 34, 18),
				new SpriteSheetCuttingDimension(123, 13, 34, 18) }),
		// on rend l'anim de mort artificiellement plus longue. Grâce aux optimisations
		// faites dans SpriteSheet on ne redécoupe pas la même image 400 fois, c'est
		// donc actuellemnt très rapide et pas problématique de faire comme ça.
		// Certe c'est un peu du bricolage, mais j'avais pas envie de refactoriser
		// AnimationAvatar à nouveau juste pour autoriser un certain nombre de répétion
		// d'une animation avant la mort au lieu d'une seule fois alors que je peux
		// juste faire ça.
		DEAD(new SpriteSheetCuttingDimension[] { new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14), new SpriteSheetCuttingDimension(143, 91, 16, 14),
				new SpriteSheetCuttingDimension(161, 91, 18, 14) });

		SpriteSheetCuttingDimension[] cuts;

		State(SpriteSheetCuttingDimension[] frames) {
			this.cuts = frames;
		}
	}

	private final Ship ship;
	private State state;
	
	private static final double BLINK_PERIOD_S = 0.16;
	private double blinkTimeS;

	public ShipAvatar(Ship ship) {
		super(ship, SpriteSheet.getSpriteSheet(SPRITE_SHEET_PATH), State.LIVE.cuts, 1, 1, ZORDER_PLAYER);
		this.ship = ship;
		this.state = State.LIVE;
	}
	
	@Override
	public void paint(Graphics g) {
		if (ship.isInvincible()) {
			boolean on = ((long) (blinkTimeS / BLINK_PERIOD_S)) % 2 == 0;
			if (!on) {
				if(showHitboxes) paintHitbox(g);
				return;
			}
		}
		super.paint(g);
		
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (ship.isInvincible()) {
			blinkTimeS += delta_t;
		} else {
			blinkTimeS = 0.0;
		}
		
		boolean dashing = this.ship.dashAnimationPlaying();
		if (this.ship.dead()) {
			if (this.state != State.DEAD) {
				this.state = State.DEAD;
				this.setCuts(State.DEAD.cuts);
				this.setPlayOnce(true);
				this.multY = 2;
			}

		} else {
			if (dashing && this.state != State.DASH) {
				this.setCuts(State.DASH.cuts);
				this.setPlayOnce(false);
				this.state = State.DASH;
			} else if (!dashing && this.state != State.LIVE) {
				this.setCuts(State.LIVE.cuts);
				this.setPlayOnce(false);
				this.state = State.LIVE;
			}
		}
		super.updateAnimation(delta_t);
	}
}