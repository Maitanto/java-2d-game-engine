package engine.move;

import java.util.List;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.geometry.ISU.Vector;
import game.Game;

public abstract class Stunt {

	protected final Model model;
	protected final Entity entity;

	protected ISU.Vector targetDirection;
	protected double targetAngle;

	protected Stunt(Model model, Entity entity) {
		this.model = model;
		this.entity = entity;

		this.targetDirection = Game.game().isu.new Vector(0, 0);
		this.targetAngle = entity.orientation();
	}

	public void setLinearSpeed(Vector linearSpeed) {
		this.targetDirection = linearSpeed;
		entity.setLinearSpeed(linearSpeed);
	}

	public void setAngularSpeed(double angularSpeed) {
		entity.setAngularSpeed(angularSpeed);
	}

	// Collision

	protected abstract void collision(Entity entity);

	protected abstract void collision(List<Entity> entities);

	protected abstract void tick(double d);
}