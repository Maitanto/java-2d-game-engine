package engine.graphics.hud;

import oop.graphics.Color;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;

public interface IHudElement {
	public final static Color DEFAULT_COLOR = Colors.white;
	

	/**
	 * Draws the element in screen space.
	 *
	 * @param g the graphics
	 */
	void draw(Graphics g);

	/**
	 * Tells whether the element should be drawn.
	 *
	 * @return true if visible
	 */
	boolean isVisible();

	/**
	 * Sets the visibility.
	 *
	 * @param shown true to show, false to hide
	 */
	void setVisibility(boolean shown);

	/**
	 * Allows for some hud elements to be updated if the need
	 */
	public void update(int canvasWidth, int canvasHeight);

	/**
	 * Allows for some hud elements to tell the hud if they need to be deleted
	 */
	public boolean mustBeDeleted();

	/**
	 * 
	 * @return the offset in a PixelCoordinate format (not an actual cordinate)
	 */
	public PixelCoordinate offset();

	/**
	 * 
	 * @param position PixelCoordinate
	 * @param offset   PixelCoordinate
	 * @return PixelCoordinate(PC) from PC position and PC offset
	 */
	public static PixelCoordinate getNewPositionFromCurrentPositionAndOffset(PixelCoordinate position,
			PixelCoordinate offset) {
		return new PixelCoordinate(position.x + offset.x, position.y + offset.y);
	}
}