package engine.geometry;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Objects;

import game.Game;

public class ISU {
	private static final double EPS = 1e-9;

	private static final DecimalFormat df = new DecimalFormat("0.00");

	// FIELDS

	private Axis xAxis, yAxis;
	private ISU isu;
	private Grid grid;

	// CONSTRUCTOR

	public ISU(Game game) {
		this.xAxis = new Axis(game.torusOnXaxis, game.width_cm);
		this.yAxis = new Axis(game.torusOnYaxis, game.height_cm);
		this.isu = this;
	}

	// SETTER

	void set(Grid grid) {
		this.grid = grid;
	}

	public double euclideanX(double originX, double x) {
		return xAxis.euclideanFrom(originX, x);
	}

	public double euclideanY(double originY, double y) {
		return yAxis.euclideanFrom(originY, y);
	}
	
	public ISU.Dimension mkDimension(ISU.Dimension d, double mult){
		return new ISU.Dimension(d.x_cm*mult, d.y_cm*mult);
	}

	// == DIMENSION (cm) ==

	public class Dimension {
		double x_cm, y_cm;

		// CONSTRUCTOR

		public Dimension(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			this.normalize();
		}

		// GEOMETRY

		void normalize() {
			if (xAxis == null || yAxis == null) {
				throw new IllegalStateException("Axis not init");
			}
			this.x_cm = xAxis.normalize(x_cm);
			this.y_cm = yAxis.normalize(y_cm);
		}

		// SETTER

		public void setxy(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			this.normalize();
		}

		// GETTER

		public ISU isu() {
			return isu;
		}

		// EQUALS / EQUIV

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (this == o)
				return true;
			if (!(o instanceof Dimension d))
				return false;
			return d.isu() == isu() && d.x_cm == x_cm && d.y_cm == y_cm;
		}

		@Override
		public int hashCode() {
			return Double.hashCode(x_cm) * 31 + Double.hashCode(y_cm);
		}

		/** Égalité à epsilon près (flottants). */
		public boolean equiv(Dimension d) {
			if (d == null)
				return false;
			return Math.abs(d.x_cm - x_cm) < EPS && Math.abs(d.y_cm - y_cm) < EPS;
		}

		// GETTER

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		// FACTORY

		ISU.Vector mkScaledVector(double factor) {
			return new Vector(this.x_cm * factor, this.y_cm * factor);
		}

		ISU.Vector mkScaledVector(double xFactor, double yFactor) {
			return new Vector(this.x_cm * xFactor, this.y_cm * yFactor);
		}

		ISU.Vector mkVector() {
			return new Vector(this.x_cm, this.y_cm);
		}

		// SHOW
		void show(PrintStream ps) {
			ps.printf(this.toString());
		}

		@Override
		public String toString() {
			return String.format(" x = %f cm \n y = %f cm\n", this.x_cm, this.y_cm);
		}

		public String toStringRounded() {
			return "{" + df.format(x_cm) + "," + df.format(y_cm) + "}";
		}

	}

	// == POINT ==

	public class Coord {
		double x_cm, y_cm;

		// CONSTRUCTOR

		public Coord(double x_cm, double y_cm) {
			this.x_cm = x_cm;
			this.y_cm = y_cm;
			this.normalize();
		}

		void normalize() {
			if (xAxis == null || yAxis == null) {
				throw new IllegalStateException("Axis not initialized");
			}
			this.x_cm = xAxis.normalize(x_cm);
			this.y_cm = yAxis.normalize(y_cm);
		}

		// SHOW

		void show(PrintStream ps) {
			ps.print(this.toString());
		}

		@Override
		public String toString() {
			return "(" + x_cm + "," + y_cm + ")";
		}

		public String toStringRounded() {
			return "(" + df.format(x_cm) + "," + df.format(y_cm) + ")";
		}

		public ISU isu() {
			return isu;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(x_cm, y_cm);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Coord other = (Coord) obj;
			return Double.doubleToLongBits(x_cm) == Double.doubleToLongBits(other.x_cm)
					&& Double.doubleToLongBits(y_cm) == Double.doubleToLongBits(other.y_cm);
		}

		/** Égalité à epsilon près (flottants). */
		public boolean equiv(Coord c) {
			if (c == null)
				return false;
			return Math.abs(c.x_cm - x_cm) < EPS && Math.abs(c.y_cm - y_cm) < EPS;
		}

		// FACTORY

		public ISU.Vector mkVectorToward(Coord target) {
			return new Vector(target.x_cm - this.x_cm, target.y_cm - this.y_cm);
		}

		// CONVERSION

		public Grid.Position toGridPosition() {
			if (Game.game() == null || grid == null) {
				throw new IllegalStateException("ISU not linked to game/grid");
			}

			double cmPerCell = Game.game().cmPerCell;

			int x_cell = (int) Math.floor((this.x_cm + cmPerCell / 2.0) / cmPerCell);
			int y_cell = (int) Math.floor((this.y_cm + cmPerCell / 2.0) / cmPerCell);

			return isu().grid.new Position(x_cell, y_cell);
		}

		// TRANSLATION

		public void translate(ISU.Vector v) {
			this.x_cm += v.x_cm;
			this.y_cm += v.y_cm;
			this.normalize();
		}

		public ISU.Coord mkTranslated(ISU.Vector v) {
			return new Coord(this.x_cm + v.x_cm, this.y_cm + v.y_cm);
		}

		// COPY

		public ISU.Coord mkCopy() {
			return new Coord(this.x_cm, this.y_cm);
		}

		// ROTATION

		/**
		 * @apiNote rotation around the origin (0,0)
		 * @param angle_degree
		 */
		public void rotation(double angle_degree) {
			this.rotateAround(new Coord(0, 0), angle_degree);
		}

		/**
		 * @apiNote rotation around the given center
		 * @param center
		 * @param angle_degree
		 */
		public void rotateAround(Coord center, double angle_degree) {
			double tempX = this.x_cm;
			double tempY = this.y_cm;
			this.x_cm = center.x_cm + (tempX - center.x_cm) * Math.cos(Math.toRadians(angle_degree))
					- (tempY - center.y_cm) * Math.sin(Math.toRadians(angle_degree));
			this.y_cm = center.y_cm + (tempX - center.x_cm) * Math.sin(Math.toRadians(angle_degree))
					+ (tempY - center.y_cm) * Math.cos(Math.toRadians(angle_degree));
			this.normalize();
		}

		// DISTANCE

		public double distanceTo(Coord pt) {
			double distX = xAxis.distance(this.x_cm, pt.x_cm);
			double distY = yAxis.distance(this.y_cm, pt.y_cm);
			return Math.sqrt(distX * distX + distY * distY);
		}

		private ISU getEnclosingInstance() {
			return ISU.this;
		}

		public double x() {
			return this.x_cm;
		}

		public double y() {
			return this.y_cm;
		}

		public void setxy(double x, double y) {
			this.x_cm = x;
			this.y_cm = y;
			this.normalize();
		}

	}

	// == VECTOR ==

	/**
	 * @apiNote The Vector class defines canonical vectors with origin in (0,0)
	 *          pointing at a target coordinate.
	 * @apiNote Canonical vectors are defined by their target Coord.
	 */
	public class Vector {

		double x_cm, y_cm;

		// CONSTRUCTOR

		public Vector(double targetX_cm, double targetY_cm) {
			this.x_cm = targetX_cm;
			this.y_cm = targetY_cm;
		}

		public double x() {
			return x_cm;
		}

		public double y() {
			return y_cm;
		}

		// OPERATOR

		void add(Vector v) {
			this.x_cm += v.x_cm;
			this.y_cm += v.y_cm;
		}

		public void scale(double factor) {
			this.x_cm *= factor;
			this.y_cm *= factor;
		}

		void scale(double xFactor, double yFactor) {
			this.x_cm *= xFactor;
			this.y_cm *= yFactor;
		}

		/**
		 * @apiNote produit scalaire
		 * @param v
		 * @return le produit scalaire de `this` et du vecteur v
		 */
		double dot(ISU.Vector v) {
			return (this.x_cm * v.x_cm) + (this.y_cm * v.y_cm);
		}

		double norm() {
			return Math.sqrt(this.dot(this));
		}

		/**
		 * @apiNote rend le vecteur unitaire, ie. de norme = 1
		 */
		void unity() {
			double norm = this.norm();
			if (norm == 0) {
				throw new IllegalStateException("Cannot normalize zero vector");
			}
			this.x_cm /= norm;
			this.y_cm /= norm;
		}

		// TURN

		/**
		 * @apiNote turn the vector itself
		 * @implNote the center of the rotation is the origin of the vector
		 * @param angle_degree
		 */
		void turn(int angle_degree) {
			double tempX = this.x_cm;
			double tempY = this.y_cm;
			this.x_cm = tempX * Math.cos(Math.toRadians(angle_degree)) - tempY * Math.sin(Math.toRadians(angle_degree));
			this.y_cm = tempX * Math.sin(Math.toRadians(angle_degree)) + tempY * Math.cos(Math.toRadians(angle_degree));
		}

		@Override
		public String toString() {
			return "[vx=" + x_cm + ", xy=" + y_cm + "]";
		}

		public String toStringRounded() {
			return "[" + df.format(x_cm) + "," + df.format(y_cm) + "]";
		}

	}
}
