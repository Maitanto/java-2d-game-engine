package game.gradius.entity;

import engine.shape.Circle;

class DragonBody extends DragonPart {

	public DragonBody() {
		super("dragon_body", ISU.new Dimension(20, 20));
	}

	@Override
	protected void setBounding() {
		clearBounding();

		addBounding(new Circle(center(), size().x() / 2));// new Rect(center(), size(), orientation()));
	}
}