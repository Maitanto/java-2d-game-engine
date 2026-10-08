package engine.graphics.avatars;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public abstract class AnimationAvatar extends RessourceAvatar {

	/**
	 * // attention c'est des secondes
	 */
	private static double DEFAULT_FRAME_DURATION_S;

	static {
		DEFAULT_FRAME_DURATION_S = 0.1;
	}

	/**
	 * // attention c'est des secondes
	 */
	protected double frameDurationS;
	protected double time;
	protected int frameIndex;

	private SpriteSheetCuttingDimension[] cuts;
	private BufferedImage[] frames;

	private boolean playOnce;
	private boolean animationFinished;

	protected AnimationAvatar(Entity entity, SpriteSheet spriteSheet, SpriteSheetCuttingDimension[] cuts, int multX,
			int multY, int z_order) {
		super(entity, spriteSheet, multX, multY, z_order);
		this.frameDurationS = DEFAULT_FRAME_DURATION_S;
		this.setCuts(cuts);
	}

	public SpriteSheetCuttingDimension[] getCuts() {
		return cuts;
	}

	public void setCuts(SpriteSheetCuttingDimension[] cuts) {
		this.cuts = cuts;
		this.frames = null;
		this.time = 0.0;
		this.frameIndex = 0;
		this.animationFinished = false;
	}

	/**
	 *
	 * @param playOnce true to play the current cuts once
	 */
	public void setPlayOnce(boolean playOnce) {
		this.playOnce = playOnce;
	}

	/**
	 * @return true once a one-shot animation has reached its last frame
	 */
	public boolean isAnimationFinished() {
		return animationFinished;
	}

	/**
	 * Ready to remove when the entity is dead and either there is no one-shot death
	 * animation to play, or it has finished. This lets a death animation play fully
	 * before the avatar is dropped.
	 */
	@Override
	public boolean isReadyToRemove() {
		if (!dead()) {
			return false;
		}
		return !playOnce || animationFinished;
	}

	public BufferedImage[] getFrames() {
		return frames;
	}

	@Override
	public void updateAnimation(double delta_t) {
		if (frames == null || frames.length == 0) {
			return; // cannot init frames without a Graphics so skip until paint loads them
		}

		time += delta_t;

		while (time >= frameDurationS) {
			time -= frameDurationS;

			if (playOnce) {
				// Advance until the last frame, then hold and mark finished.
				if (frameIndex >= frames.length - 1) {
					frameIndex = frames.length - 1;
					animationFinished = true;
					break;
				}
				frameIndex++;
			} else {
				frameIndex = (frameIndex + 1) % frames.length;
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		if (frames == null) {
			frames = spriteSheet.getSprites(g, cuts); // spriteSheet and cuts are not supposed to be null
		}

		// Keep painting while alive, and while a one-shot (death) animation is
		// still playing; stop only when there is no position, or once a one-shot
		// animation has finished.
		if (entity().center() == null) {
			return;
		}
		if (dead() && !playOnce) {
			return;
		}
		if (animationFinished) {
			return;
		}

		// draw animation
		BufferedImage frame = frames[frameIndex];

		ISU.Coord coord = entity().center();
		ISU.Dimension size = entity().size();

		int width = (int)(Math.max(1, this.cmToPixel(size.x())) * multX);
		int height = (int)(Math.max(1, this.cmToPixel(size.y())) * multY);

		int xCenter = this.cmToPixel(coord.x());
		int yCenter = this.cmToPixel(coord.y());

		int xTopLeft = xCenter - width / 2;
		int yTopLeft = yCenter - height / 2;

		g.drawImage(frame, xTopLeft, yTopLeft, width, height);

		// check if need to show debug collision boxes
		super.paint(g);
	}
}