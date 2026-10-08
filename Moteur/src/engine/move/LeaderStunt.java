package engine.move;

import java.util.LinkedList;
import java.util.List;

import engine.entity.Entity;
import engine.gal.GALStunt;
import engine.geometry.ISU;
import engine.move.FollowerStunt.MovementState;
import game.Game;

public class LeaderStunt extends GALStunt {

	private final LinkedList<MovementState> history = new LinkedList<>();

	public LeaderStunt(Model model, Entity entity) {
		super(model, entity);
	}

	public LinkedList<MovementState> getHistory() {
		return history;
	}

	public void setSpeedFromOrientation(double speed) {
		double angleRad = Math.toRadians(entity.orientation());
		ISU.Vector direction = Game.game().isu.new Vector(Math.cos(angleRad) * speed, Math.sin(angleRad) * speed);
		setLinearSpeed(direction);
	}

	@Override
	protected void collision(Entity entity) {
		super.collision(entity);
	}

	@Override
	protected void collision(List<Entity> entities) {
		super.collision(entities);
	}

	@Override
	public void tick(double elapsed_s) {
		super.tick(elapsed_s);
		history.addLast(new MovementState(entity.center().mkCopy(), entity.orientation()));
		// setSpeedFromOrientation(20);
	}
}