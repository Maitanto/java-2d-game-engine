package engine.gal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;
import engine.geometry.ISU.Vector;
import engine.move.Model;
import game.Game;

public class CompositeGALStunt extends GALStunt {
	
	protected static Game game = Game.game();

	protected final List<Entity> subEntities;
	protected Vector baseLinearSpeed;
	protected double elapsed;
	protected double last_time;
	protected static double expected_time;
	
	public CompositeGALStunt(Model model, Entity mainEntity, List<Entity> subEntities) {
		super(model, mainEntity);
		expected_time = 200;

		if (subEntities == null) {
			throw new IllegalArgumentException("subEntities cannot be null");
		}

		this.subEntities = new ArrayList<>();
		this.baseLinearSpeed = game.isu.new Vector(0.0, 0.0);

		for (Entity subEntity : subEntities) {
			addSubEntity(subEntity);
		}
	}

	public void addSubEntity(Entity subEntity) {
		if (subEntity == null) {
			throw new IllegalArgumentException("subEntity cannot be null");
		}

		if (subEntity == entity) {
			throw new IllegalArgumentException("mainEntity cannot be a subEntity of itself");
		}

		if (!subEntities.contains(subEntity)) {
			subEntities.add(subEntity);
		}
	}

	public void removeSubEntity(Entity subEntity) {
		subEntities.remove(subEntity);
	}

	public List<Entity> subEntities() {
		return Collections.unmodifiableList(subEntities);
	}

	@Override
	public void setLinearSpeed(Vector linearSpeed) {
		this.targetDirection = linearSpeed;

		Vector realSpeed = game.isu.new Vector(baseLinearSpeed.x() + linearSpeed.x(),
				baseLinearSpeed.y() + linearSpeed.y());

		entity.setLinearSpeed(realSpeed);

		for (Entity subEntity : subEntities) {
			if (!subEntity.dead()) {
				subEntity.setLinearSpeed(game.isu.new Vector(0.0, 0.0));
			}
		}
	}

	@Override
	public void setAngularSpeed(double angularSpeed) {
		super.setAngularSpeed(angularSpeed);

		for (Entity subEntity : subEntities) {
			if (!subEntity.dead()) {
				subEntity.setAngularSpeed(0.0);
			}
		}
	}

	public void setBaseLinearSpeed(double x_cmPer_s, double y_cmPer_s) {
		this.baseLinearSpeed = game.isu.new Vector(x_cmPer_s, y_cmPer_s);
		setLinearSpeed(targetDirection);
	}

	@Override
	protected void collision(Entity other) {
		if (other != null && other.category() == Category.Power && entity instanceof PowerReceiver receiver) {
			receiver.activatePower();
		}

		super.collision(other);
	}

	@Override
	public boolean startThrowing(Direction direction, double intensity) {
		if (projectileSpawner() == null) {
			return false;
		}
		double current_time = System.currentTimeMillis();
		elapsed = current_time - last_time;
		if (elapsed > expected_time) {

			projectileSpawner().spawnFrom(entity, direction, intensity);
			for (Entity subEntity : subEntities) {
				if (!subEntity.dead()) {
					projectileSpawner().spawnFrom(subEntity, direction, intensity);
				}
			}
			last_time = current_time;
		}
		return true;
	}
}