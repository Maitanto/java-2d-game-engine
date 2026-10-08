package engine.graphics.hud;

import java.util.function.Supplier;

import oop.graphics.Font;

public interface ILabel extends IHudElement {
	public void changeFont(String name, int style, int size);

	public String getFontName();

	public int getFontStyle();

	public int getFontSize();

	public void set(Supplier<String> newText);

	public final static String DEFAULT_FONT_NAME = "Monospaced";
	public final static int DEFAULT_FONT_SIZE = 12;
	public final static int DEFAULT_FONT_STYLE = Font.PLAIN;
}
