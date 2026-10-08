package game.gradius.stunt;

import java.util.LinkedList;

import engine.entity.Entity;
import engine.move.FollowerStunt;
import engine.move.Model;

public class DragonBodyStunt extends FollowerStunt {

	public DragonBodyStunt(Model model, Entity entity, Entity leaderEntity, LinkedList<MovementState> leaderHistory,
			int tickDelay) {
		super(model, entity, leaderEntity, leaderHistory, tickDelay);
	}

}
