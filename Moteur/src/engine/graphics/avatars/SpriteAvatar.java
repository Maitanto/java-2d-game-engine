package engine.graphics.avatars;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public abstract class SpriteAvatar extends RessourceAvatar {

	private SpriteSheetCuttingDimension cut;
	private BufferedImage sprite;

	protected SpriteAvatar(Entity entity, SpriteSheet spriteSheet, SpriteSheetCuttingDimension cut, int multX,
			int multY, int z_order) {
		super(entity, spriteSheet, multX, multY, z_order);
		this.setCut(cut);
	}

	public SpriteSheetCuttingDimension getCut() {
		return cut;
	}

	public void setCut(SpriteSheetCuttingDimension cut) {
		this.cut = cut;
		this.sprite = null; // on met sprite à null comme ça au prochain coup de paint on recharge l'image
							// selon le nouveau cut
	}

	public BufferedImage getSprite() {
		return sprite;
	}

	@Override
	public void paint(Graphics g) {
		if (sprite == null) {
			sprite = spriteSheet.getSprite(g, cut);
		}

		// si l'entité est morte ou n'a plus de centre, on skip
		if (dead() || entity().center() == null) {
			return;
		}

		// Draw the sprite
		ISU.Coord coord = entity().center();
		ISU.Dimension size = entity().size();

		int width = (int)(Math.max(1, this.cmToPixel(size.x())) * multX);
		int height = (int)(Math.max(1, this.cmToPixel(size.y())) * multY);

		int xCenter = this.cmToPixel(coord.x());
		int yCenter = this.cmToPixel(coord.y());

		int xTopLeft = xCenter - width / 2;
		int yTopLeft = yCenter - height / 2;

		g.drawImage(sprite, xTopLeft, yTopLeft, width, height);

		// check if need to show debug collision boxes
		super.paint(g);
	}
	
	protected void paintSprite(Graphics g) {
		if (sprite == null) {
			sprite = spriteSheet.getSprite(g, cut);
		}
 
		// si l'entité est morte ou n'a plus de centre, on skip
		if (dead() || entity().center() == null) {
			return;
		}
 
		// Draw the sprite
		ISU.Coord coord = entity().center();
		ISU.Dimension size = entity().size();

		int width = (int)(Math.max(1, this.cmToPixel(size.x())) * multX);
		int height = (int)(Math.max(1, this.cmToPixel(size.y())) * multY);
 
		int xCenter = this.cmToPixel(coord.x());
		int yCenter = this.cmToPixel(coord.y());
 
		int xTopLeft = xCenter - width / 2;
		int yTopLeft = yCenter - height / 2;
 
		g.drawImage(sprite, xTopLeft, yTopLeft, width, height);
	}

}