package game.gradius.stunt;

import java.util.List;

import engine.entity.Entity;
import engine.move.BasicStunt;
import engine.move.Model;
import game.gradius.entity.Ship;

public class EnemyStunt extends BasicStunt {

	public EnemyStunt(Model model, Entity entity) {
		super(model, entity);
	}

	@Override
	protected void collision(Entity other) {
		if (other.getClass() == Ship.class)
			entity.kill();
	}

	@Override
	protected void collision(List<Entity> entities) {
		entity.kill();
	}

}
