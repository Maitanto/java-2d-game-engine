package game.gradius.graphics;

import engine.entity.Entity;
import engine.graphics.avatars.ShapeAvatar;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;

public class ObstacleAvatar extends ShapeAvatar {

	public ObstacleAvatar(Entity entity) {
		super(entity, 1, 1, MAX_ZORDER);
	}

	@Override
	public void paint(Graphics g) {
		if (dead() || entity().center() == null) {
			return;
		}

		int width = Math.max(1, this.cmToPixel(entity().size().x()));
		int height = Math.max(1, this.cmToPixel(entity().size().y()));

		int xCenter = this.cmToPixel(entity().center().x());
		int yCenter = this.cmToPixel(entity().center().y());

		int xTopLeft = xCenter - width / 2;
		int yTopLeft = yCenter - height / 2;

		g.setColor(Colors.red);
		g.fillRect(xTopLeft, yTopLeft, width, height);

		g.setColor(Colors.white);
		g.drawRect(xTopLeft, yTopLeft, width, height);
	}
}