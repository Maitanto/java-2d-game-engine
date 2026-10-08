package game.gradius.stunt;

import java.util.List;

import engine.entity.Entity;
import engine.move.Model;
import engine.move.Stunt;
import game.Game;
import game.gradius.entity.Enemy;
import game.gradius.entity.Projectile;

public class ProjectileStunt extends Stunt {

	public ProjectileStunt(Model model, Entity entity) {
		super(model, entity);
	}

	@Override
	protected void collision(Entity entity) {
		this.entity.kill();
		if (entity instanceof Enemy) {
			Game.score+=Enemy.SCORE_VALUE;
			entity.kill();
		}
	}

	@Override
	protected void collision(List<Entity> entities) {
		this.entity.kill();
		for (Entity en : entities) {
			if (en instanceof Enemy) {
				Game.score+=Enemy.SCORE_VALUE;
				en.kill();
			}
		}
	}

	@Override
	protected void tick(double d) {
		if (entity instanceof Projectile projectile) {
			projectile.updatePowerAnimation();
		}
	}
}