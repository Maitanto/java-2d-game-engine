package engine.graphics.hud;

import engine.entity.Entity;
import oop.graphics.Color;
import oop.graphics.Graphics;

public class HealthBar extends FollowerLabel implements IHealthBar, IAnchored {
private final static int SMALL_BOTTOM_MARGIN = 3;
	/**
	 * enum private car on détermine localement le type directement grâce au
	 * constructeur utilisé
	 * 
	 */
	private enum HealthBarType {
		ABSOLUTE, ANCHORED, FOLLOWING
	}

	public enum HealthBarStyle {
		CIRCLES, RECT
	}

	private int hp, totalhp;
	private int length, height;
	int widthOfOneHp;

	private final Anchor anchor;
	private final HealthBarType type;
	private final HealthBarStyle style;

	public Color colorPrimary;
	public Color colorSecondary;
	public boolean drawText;

	public HealthBar(Entity entity, Anchor anchor, PixelCoordinate offset, int length, Color colorText,
			Color colorPrimary, Color colorSecondary, HealthBarStyle style, boolean drawText) {
		this(length, true, new PixelCoordinate(0, 0), offset, entity, anchor, HealthBarType.ANCHORED, colorText,
				colorPrimary, colorSecondary, style, true);
	}

	public HealthBar(Entity entity, Anchor anchor, PixelCoordinate offset, int length, int height, Color colorText,
			Color colorPrimary, Color colorSecondary, HealthBarStyle style, boolean b) {
		this(length, height, true, new PixelCoordinate(0, 0), offset, entity, anchor, HealthBarType.ANCHORED, colorText,
				colorPrimary, colorSecondary, style, true);
	}

	public HealthBar(Entity entity, PixelCoordinate position, PixelCoordinate offset, int length, Color colorText,
			Color colorPrimary, Color colorSecondary, HealthBarStyle style, boolean drawText) {
		this(length, true, position, offset, entity, null, HealthBarType.ABSOLUTE, colorText, colorPrimary,
				colorSecondary, style, false);
	}

	public HealthBar(Entity entity, PixelCoordinate offset, int length, Color colorText, Color colorPrimary,
			Color colorSecondary, HealthBarStyle style, boolean drawText) {
		this(length, true, new PixelCoordinate(0, 0), offset, entity, null, HealthBarType.FOLLOWING, colorText,
				colorPrimary, colorSecondary, style, false);
		// we use new PixelCoordinate(0,0) here instead of
		// FollowerLabel.view.worldToScreen(entity.center()) because view isn't
		// initialized yet at this stade of the execution
	}

	public HealthBar(int length, boolean visible, PixelCoordinate position, PixelCoordinate offset, Entity entity,
			Anchor anchor, HealthBarType type, Color colorText, Color colorPrimary, Color colorSecondary,
			HealthBarStyle style, boolean drawText) {
		this(length, length / entity.bot().life(), visible, position, offset, entity, anchor, type, colorText,
				colorPrimary, colorSecondary, style, drawText);
	}

	public HealthBar(int length, int height, boolean visible, PixelCoordinate position, PixelCoordinate offset,
			Entity entity, Anchor anchor, HealthBarType type, Color colorText, Color colorPrimary, Color colorSecondary,
			HealthBarStyle style, boolean drawText) {
		super(() -> "", entity, offset, visible, Label.DEFAULT_FONT_NAME, height, Label.DEFAULT_FONT_STYLE, colorText,
				true, false);
		// we let the text supplier empty here, because text will be initialized after.
		this.position = position;
		this.anchor = anchor;
		this.type = type;
		this.style = style;
		this.length = length;
		this.height = height;
		this.colorPrimary = colorPrimary;
		this.colorSecondary = colorSecondary;
		this.drawText = drawText;
		this.totalhp = target.bot().life();
		this.hp = this.totalhp;
		this.set(() -> this.toString());
		this.widthOfOneHp = length / totalhp;
	}

	@Override
	public void draw(Graphics g) {
		if (style == HealthBarStyle.CIRCLES) {
			for (int i = 0; i < totalhp; i++) {
				if (hp > i) {
					g.setColor(colorPrimary);
					g.fillOval(position.x + (i * widthOfOneHp) - length / 2, position.y - height+SMALL_BOTTOM_MARGIN, widthOfOneHp, height);
				} else {
					g.setColor(colorSecondary);
					g.drawOval(position.x + i * widthOfOneHp - length / 2, position.y - height+SMALL_BOTTOM_MARGIN, widthOfOneHp, height);
				}
			}
		} else if (style == HealthBarStyle.RECT) {
			g.setColor(colorSecondary);
			g.fillRect(position.x - (length / 2), position.y - height+SMALL_BOTTOM_MARGIN, length, height);
			g.setColor(colorPrimary);
			g.fillRect(position.x - (length / 2), position.y - height+SMALL_BOTTOM_MARGIN,
					length - ((this.totalhp - this.hp) * widthOfOneHp), height);
		}

		if (drawText)
			super.draw(g);
	}

	@Override
	public void update(int canvasWidth, int canvasHeigh) {
		if (target == null || target.dead()) {
			this.mustBeDeleted = true;
			return;
		}
		hp = target.bot().life(); // we don't put the bot in a local var because bot can be switch around mid game
		switch (this.type) {
		case ANCHORED: {
			this.position.x = anchor.calculateX(canvasWidth) + offset.x;
			this.position.y = anchor.calculateY(canvasHeigh) + offset.y;
			return;
		}
		case FOLLOWING: {
			super.update(canvasWidth, canvasHeigh);
			return;
		}
		case ABSOLUTE: {
			return;
		}
		}

	}

	@Override
	public Anchor anchor() {
		return this.anchor;
	}

	@Override
	public int getCurrentHP() {
		return this.hp;
	}

	@Override
	public void setCurrentHP(int hp) {
		this.hp = hp;
	}

	@Override
	public int getTotalHP() {
		return this.totalhp;
	}

	@Override
	public void setTotalHP(int hp) {
		this.totalhp = hp;
	}

	@Override
	public String toString() {
		return String.format("%d/%d", hp, totalhp);
	}

}
