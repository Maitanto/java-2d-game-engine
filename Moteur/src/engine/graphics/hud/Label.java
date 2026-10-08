package engine.graphics.hud;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.logs.LoggerManager;
import game.Game;
import oop.graphics.Color;
import oop.graphics.Font;
import oop.graphics.Graphics;

public class Label implements ILabel {
	protected static final Logger logger;
	protected static final boolean LOGGING;
	protected static final boolean INFO;
	static {
		// Logger
		logger = LoggerManager.getLogger(Label.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		INFO = logger.isLoggable(Level.INFO);
	}

	private Supplier<String> text;
	private boolean visible;
	private boolean isDebug;

	private String fontName;
	private int fontStyle;
	private int fontSize;

	private boolean mustBeDeleted;

	public boolean centeredText;

	/**
	 * pixel coordinate
	 */
	public PixelCoordinate position;
	public PixelCoordinate offset;

	public Color color;

	public Label(String text, PixelCoordinate position, boolean centeredText, boolean isDebug) {
		this(text, position, true, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, DEFAULT_FONT_STYLE, DEFAULT_COLOR,
				centeredText, isDebug);
	}

	public Label(String text, PixelCoordinate position, Color color, boolean centeredText, boolean isDebug) {
		this(text, position, true, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, DEFAULT_FONT_STYLE, color, centeredText,
				isDebug);
	}

	public Label(Supplier<String> text, PixelCoordinate position, Color color, boolean centeredText, boolean isDebug) {
		this(text, position, new PixelCoordinate(0, 0), true, DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE, DEFAULT_FONT_STYLE,
				color, centeredText, isDebug);
	}

	public Label(String text, PixelCoordinate position, boolean visible, String fontName, int fontSize, int fontStyle,
			Color color, boolean centeredText, boolean isDebug) {
		this(() -> text, position, new PixelCoordinate(0, 0), visible, fontName, fontSize, fontStyle, color,
				centeredText, isDebug);
	}

	public Label(Supplier<String> text, PixelCoordinate position, PixelCoordinate offset, boolean visible,
			String fontName, int fontSize, int fontStyle, Color color, boolean centeredText, boolean isDebug) {
		this.text = text;
		this.position = position;
		this.visible = visible;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.fontStyle = fontStyle;
		this.color = color;
		this.centeredText = centeredText;
		this.mustBeDeleted = false;
		this.offset = offset;
		this.isDebug = isDebug;
		if (LOGGING && INFO) {
			logger.log(Level.INFO, String.format("Created new Label %s with this font {%s, %d, %d}.", this.toString(),
					this.fontName, this.fontStyle, this.fontSize));
		}
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		Font f = g.getFont(fontName, fontStyle, fontSize);
		g.setFont(f);

		String text = this.text.get();
		g.drawString(text, position.x - ((centeredText) ? f.getWidth(text) / 2 : 0), position.y);
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @return whether this label is a debug label (shown only in debug mode)
	 */
	public boolean isDebug() {
		return isDebug;
	}

	@Override
	public void setVisibility(boolean shown) {
		this.visible = shown;
	}

	@Override
	public void changeFont(String name, int style, int size) {
		if (LOGGING && INFO) {
			logger.log(Level.INFO, String.format("Label %s set a new font, from {%s, %d, %d} to {%s, %d, %d}.",
					this.toString(), this.fontName, this.fontStyle, this.fontSize, name, style, size));
		}
		this.fontName = name;
		this.fontStyle = style;
		this.fontSize = size;
	}

	@Override
	public String getFontName() {
		return fontName;
	}

	@Override
	public int getFontStyle() {
		return fontStyle;
	}

	@Override
	public int getFontSize() {
		return fontSize;
	}

	@Override
	public void update(int canvasWidth, int canvasHeigh) {
		// pas besoin de maj la position d'un label immobile
		// on regarde uniquement si on doit l'afficher si c'est un label de debug.
		if (isDebug) {
			this.setVisibility(Game.debugMode);
		}
	}

	@Override
	public boolean mustBeDeleted() {
		return mustBeDeleted;
	}

	public void delete() {
		if (LOGGING && INFO) {
			logger.log(Level.INFO, String.format("Label %s set to delete soon.", this.toString()));
		}
		mustBeDeleted = true;
	}

	@Override
	public void set(Supplier<String> newText) {
		if (LOGGING && INFO) {
			logger.log(Level.INFO, String.format("Label %s set to this new text : %s", this.toString(), newText));
		}
		this.text = newText;
	}

	@Override
	public PixelCoordinate offset() {
		return offset;
	}

	@Override
	public String toString() {
		return ((isDebug) ? "Debug" : "") + "Label[" + text + "]" + ((visible) ? "0" : "1");
	}

}