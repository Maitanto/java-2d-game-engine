package game.gradius.entity;

public enum CannonSlot {
	TOP, BOTTOM;

	// TODO est ce que cette enum est vraiment nécessaire ?

	public boolean isTop() {
		return this == TOP;
	}

	public boolean isBottom() {
		return this == BOTTOM;
	}
}