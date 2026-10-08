package engine.move;

import java.util.List;
import java.util.logging.Logger;

import engine.entity.Entity;
import engine.geometry.ISU.Vector;
import engine.logs.LoggerManager;
import game.Game;

public class BasicStunt extends Stunt {

	private static Logger logger = LoggerManager.getLogger(BasicStunt.class.getName());

	private static final double DEFAULT_SPEED = 20.0;

	public BasicStunt(Model model, Entity entity) {
		super(model, entity);
		model.setStunt(entity, this);
	}

	public void set(int orientation) {

		double angle = normalizeAngle(orientation);

		if (angle >= 45.0 && angle < 135.0) {
			setLinearSpeed(Game.game().isu.new Vector(0, DEFAULT_SPEED));
		} else if (angle >= 135.0 && angle < 225.0) {
			setLinearSpeed(Game.game().isu.new Vector(-DEFAULT_SPEED, 0));
		} else if (angle >= 225.0 && angle < 315.0) {
			setLinearSpeed(Game.game().isu.new Vector(0, -DEFAULT_SPEED));
		} else {
			setLinearSpeed(Game.game().isu.new Vector(DEFAULT_SPEED, 0));
		}
	}

	@Override
	protected void collision(Entity other) {
		setLinearSpeed(Game.game().isu.new Vector(0, 0));
		setAngularSpeed(0);

		logger.finer("ATTENTION COLLISION avec " + other.name());
	}

	@Override
	protected void collision(List<Entity> entities) {
		setLinearSpeed(Game.game().isu.new Vector(0, 0));
		setAngularSpeed(0);

		logger.finer("ATTENTION COLLISION avec plusieurs entités");
	}

	@Override
	public void setLinearSpeed(Vector linearSpeed) {
		super.setLinearSpeed(linearSpeed);
	}

	@Override
	public void setAngularSpeed(double angularSpeed) {
		super.setAngularSpeed(angularSpeed);
	}

	private double normalizeAngle(double angle) {
		double normalized = angle % 360.0;

		if (normalized < 0.0) {
			normalized += 360.0;
		}

		return normalized;
	}

	@Override
	protected void tick(double d) {
		// TODO Auto-generated method stub

	}
}