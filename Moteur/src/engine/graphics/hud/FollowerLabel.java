package engine.graphics.hud;

import java.util.function.Supplier;

import engine.entity.Entity;
import engine.geometry.ISU;
import engine.graphics.View;
import engine.move.Model;
import game.Game;
import oop.graphics.Color;
import oop.graphics.Graphics;

public class FollowerLabel extends Label implements IFollower {

	protected static final boolean showFollowerLabelEvenOutsideOfViewport = true;

	protected static final double CM_PER_CELL;
	protected static final double PX_PER_CM;
	protected static final View VIEW;
	protected static final Model MODEL;
	protected static final ISU ISU;
	static {
		Game game = Game.game();
		CM_PER_CELL = game.cmPerCell;
		PX_PER_CM = game.pixelPerCm;
		VIEW = game.view;
		MODEL = game.model;
		ISU = game.isu;
	}

	protected Entity target;
	protected boolean mustBeDeleted;

	public FollowerLabel(String text, Entity target, PixelCoordinate offset, Color color, boolean isDebug) {
		this(() -> text, target, offset, color, isDebug);
	}

	public FollowerLabel(Supplier<String> text, Entity target, PixelCoordinate offset, Color color, boolean isDebug) {
		this(text, target, offset, true, Label.DEFAULT_FONT_NAME, Label.DEFAULT_FONT_SIZE, Label.DEFAULT_FONT_STYLE,
				color, true, isDebug);
	}

	public FollowerLabel(Supplier<String> text, Entity target, PixelCoordinate offset, boolean visible, String fontName,
			int fontSize, int fontStyle, Color color, boolean centeredText, boolean isDebug) {
		super(text, new PixelCoordinate(0, 0), new PixelCoordinate(0, 0), visible, fontName, fontSize, fontStyle, color,
				centeredText, isDebug);
		this.target = target;
		this.offset = offset;
		this.mustBeDeleted = false;
	}

	@Override
	public void update(int canvasWidth, int canvasHeigh) {
		// target shouldn't be null in the first place
		if (target == null || target.dead()) {
			setVisibility(false); // label will be deleted if target is dead.
			this.mustBeDeleted = true;
			return;
		}

		ISU.Coord center = target.center();

		PixelCoordinate screen = VIEW.worldToScreen(center);
		boolean onScreen = screen!=null;

		if (onScreen) {
			this.position = IHudElement.getNewPositionFromCurrentPositionAndOffset(screen, offset);
		}

		boolean wanted = isDebug() ? Game.debugMode : true;
		setVisibility(wanted && (onScreen));
	}

	@Override
	public void draw(Graphics g) {
		if (target == null || target.dead() || this.mustBeDeleted) {
			return;
		}
		super.draw(g);
	}

	/**
	 * getter to check if the target entity is still alive
	 * 
	 * @return true if target entity is still alive
	 */
	@Override
	public boolean isTargetEntityStillAlive() {
		return this.target.alive();
	}

	@Override
	public boolean mustBeDeleted() {
		return this.mustBeDeleted;
	}

	@Override
	public Entity getTarget() {
		return this.target;
	}
}