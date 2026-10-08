package engine.gal.actions;

import engine.entity.Entity;

public class Hit extends GALAction {

	private final int damage;

	public Hit(int damage) {
		super();

		if (damage < 0) {
			throw new IllegalArgumentException("damage cannot be negative");
		}

		this.damage = damage;
	}

	@Override
	public boolean exec(Entity e) {
		if (e == null || e.bot() == null) {
			return false;
		}
		
		int currentLife = e.bot().life();
		int newLife = Math.max(0, currentLife - damage);
		e.bot().sethealth(newLife);

		return true;
	}
}