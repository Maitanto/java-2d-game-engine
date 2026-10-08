package engine.gal.condition;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;

public abstract class GALCondition implements iGALCondition {

	protected Direction direction;
	protected Category category;

	public static final True TRUE = new True();
	public static final False FALSE = new False();

	protected GALCondition(Direction direction, Category category) {
		this.direction = direction;
		this.category = category;
	}

	protected GALCondition() {
	}

	@Override
	public abstract boolean eval(Entity e);

}