package engine.graphics;

import game.Game;
import engine.graphics.View.IBackground;
import oop.graphics.BufferedImage;
import oop.graphics.Graphics;

public class Background implements IBackground {

	public enum Tiling {
		FULL_HEIGHT, FULL_WIDTH
	}

	public static Tiling tiling = Tiling.FULL_HEIGHT;

	public String imagePath;
	public int x, y, w, h;

	private BufferedImage mapImage;
	private boolean initialized;

	public Background(String path, int x, int y, int w, int h) {
		this.initialized = false;
		imagePath = path;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;

	}

	public void initImages(Graphics g, int x, int y, int w, int h) {
		BufferedImage sprite = g.load(imagePath);
		this.mapImage = sprite.getSubimage(x, y, w, h);
		this.initialized = true;
	}

	public void paint(Graphics g) {
		if (!initialized) {
			initImages(g, x, y, w, h);
		}

		if (mapImage == null) {
			return;
		}

		Game game = Game.game();

		if (game == null) {
			return;
		}

		double worldW_cm = game.grid.width() * game.cmPerCell;
		double worldH_cm = game.grid.height() * game.cmPerCell;

		double tile_cm = (tiling == Tiling.FULL_HEIGHT) ? worldH_cm : worldW_cm;

		int tilePx = Math.max(1, (int) Math.round(tile_cm * game.pixelPerCm));

		int worldWpx = (int) Math.round(worldW_cm * game.pixelPerCm);
		int worldHpx = (int) Math.round(worldH_cm * game.pixelPerCm);

		for (int px = 0; px < worldWpx; px += tilePx) {
			for (int py = 0; py < worldHpx; py += tilePx) {
				g.drawImage(mapImage, px, py, tilePx, tilePx);
			}
		}
	}
}