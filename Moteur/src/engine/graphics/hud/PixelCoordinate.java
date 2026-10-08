package engine.graphics.hud;

import java.util.Objects;

public class PixelCoordinate {
	public int x,y;

	public PixelCoordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PixelCoordinate other = (PixelCoordinate) obj;
		return x == other.x && y == other.y;
	}

	@Override
	public String toString() {
		return "PixCoord[" + x + "," + y + "]";
	}

}
