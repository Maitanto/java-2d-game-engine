package engine.graphics.hud;

/**
 * Classe créee pour le cas où l'on veut afficher des éléments en bas du canvas.
 * Quand on resize ils se retrouvent en plein millieu, donc il faut calculer
 * leur position dynamiquement à partir de la taille actuelle du canvas
 */
public enum Anchor {
	TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

	public int calculateX(int widthCanvas) {
		switch (this) {
		case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT:
			return 0;
		case TOP_CENTER, CENTER, BOTTOM_CENTER:
			return widthCanvas / 2;
		case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT:
			return widthCanvas;
		}
		return -67;
	}

	public int calculateY(int heightCanvas) {
		switch (this) {
		case TOP_LEFT, TOP_CENTER, TOP_RIGHT:
			return 0;
		case CENTER_LEFT, CENTER, CENTER_RIGHT:
			return heightCanvas / 2;
		case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT:
			return heightCanvas;
		}
		return -67;
	}

	public boolean isHorizontallyCentered() {
		return this == TOP_CENTER || this == CENTER || this == BOTTOM_CENTER;
	}

	public boolean isRight() {
		return this == TOP_RIGHT || this == CENTER_RIGHT || this == BOTTOM_RIGHT;
	}

	public boolean isLeft() {
		return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
	}

	public boolean isVerticallyCentered() {
		return this == CENTER_LEFT || this == CENTER || this == CENTER_RIGHT;
	}

	public boolean isTop() {
		return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
	}

	public boolean isBottom() {
		return this == BOTTOM_LEFT || this == BOTTOM_CENTER || this == BOTTOM_RIGHT;
	}
}