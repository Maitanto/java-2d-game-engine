package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.arguments.Category;
import engine.geometry.ISU;

public abstract class DragonPart extends Entity{
	public DragonPart(String name, ISU.Dimension size) {
		super(name);
		setSize(size);
		category(Category.Adversary);
	}
}
