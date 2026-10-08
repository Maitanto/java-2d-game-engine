package engine.graphics.hud;

import engine.entity.Entity;

public interface IFollower extends IHudElement {
	public final static boolean FOLLOWING_LABEL_SHOULD_STAY = false;

	public boolean isTargetEntityStillAlive();

	public Entity getTarget();
}
