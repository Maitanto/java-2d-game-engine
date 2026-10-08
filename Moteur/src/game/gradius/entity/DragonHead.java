package game.gradius.entity;

import engine.shape.Circle;

class DragonHead extends DragonPart {

	public DragonHead() {
		super("dragon_head", ISU.new Dimension(25, 25));
	}

	@Override
	protected void setBounding() {
		clearBounding();
		addBounding(new Circle(center(), size().x() / 3));// new Rect(center(), size(), orientation()));
	}

}