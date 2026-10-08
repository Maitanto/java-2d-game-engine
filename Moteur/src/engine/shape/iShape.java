package engine.shape;

public interface iShape {

	boolean intersects(iShape shape);

	boolean intersects(Circle circle);

	boolean intersects(Rect rect);

	Box boundingBox();

	public interface Box {
		double minX();

		double maxX();

		double minY();

		double maxY();
	}

}
