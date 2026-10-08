package engine.geometry;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import engine.entity.Entity;

import game.Game;

public class Grid {

	// FIELDS

	private ISU isu;
	protected Axis xAxis, yAxis; // getter ?

	private int width_ncell, height_ncell;

	private Cell[][] grid;

	// CONSTRUCTOR

	public Grid(Game game) {

		this.width_ncell = game.width_ncell;
		this.height_ncell = game.height_ncell;

		this.xAxis = new Axis(game.torusOnXaxis, this.width_ncell);
		this.yAxis = new Axis(game.torusOnYaxis, this.height_ncell);

		this.isu = game.isu;
		this.grid = new Cell[this.height_ncell][this.width_ncell];
		isu.set(this);
		init();

	}

	// INIT

	private void init() {
		for (int i = 0; i < this.width_ncell; i++) {
			for (int j = 0; j < this.height_ncell; j++) {
				grid[j][i] = new Cell(new Position(i, j));
			}
		}
	}

	// GETTER

	public int width() {
		return this.width_ncell;
	}

	public int height() {
		return this.height_ncell;
	}

	public Grid.Cell cellAt(Grid.Position p) {
		return grid[p.y_ncell][p.x_ncell];
	}

	public Grid.Cell cellAt(int x, int y) {
		return grid[y][x];
	}

	public Cell occupy(Entity e, Position p) {
		Cell cell = this.cellAt(p);

		if (!cell.contains(e)) {
			cell.add(e);
		}

		return cell;
	}

	public void retract(Entity e, Cell cell) {
		if (cell != null) {
			cell.remove(e);
		}
	}

	// SHOW

	void show(PrintStream ps) {
		ps.printf(this.toString());
		ps.printf(" w = %d \n h = %d", this.width_ncell, this.height_ncell);
	}

	// == DIMENSION (nb cell) ==

	public class Dimension {
		int x_ncell, y_ncell;

		// CONSTRUCTOR

		public Dimension(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
			this.normalize();
		}

		// GETTER

		public int x() {
			return this.x_ncell;
		}

		public int y() {
			return this.y_ncell;
		}

		// GEOMETRY

		void normalize() {
			this.x_ncell = Grid.this.xAxis.normalize(x_ncell);
			this.y_ncell = Grid.this.yAxis.normalize(y_ncell);
		}

		// EQUALS / EQUIV
		@Override
		public boolean equals(Object o) {
			if (o instanceof Grid.Dimension) {
				return this.equiv((Grid.Dimension) o);
			}
			return false;
		}

		boolean equiv(Dimension d) {
			return ((this.x_ncell == d.x_ncell) && (this.y_ncell == d.y_ncell));
		}

		// CONVERSION

		public ISU.Dimension toISUDimension() {
			return isu.new Dimension(this.x_ncell * Game.game().cmPerCell, this.y_ncell * Game.game().cmPerCell);
		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf(" x = %d \n y = %d", this.x_ncell, this.y_ncell);
		}

	}

	// == VECTOR ==

	public class Vector {

		private int x_ncell, y_ncell;

		// CONSTRUCTOR

		public Vector(int x_ncell, int y_ncell) {
			this.x_ncell = x_ncell;
			this.y_ncell = y_ncell;
		}

		public int x() {
			return x_ncell;
		}

		public int y() {
			return y_ncell;
		}

		// OPERATION

		void add(Vector v) {
			this.x_ncell += v.x_ncell;
			this.y_ncell += v.y_ncell;

		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf("Le vecteur " + this.toString() + " vaut :");
		}

		public boolean equals(Object o) {
			if (o instanceof Vector) {
				Vector vect = (Vector) o;
				if (vect.x() == x_ncell && vect.y() == y_ncell) {
					return true;
				}
			}
			return false;
		}

	}

	// == POINT ==

	public class Position extends Dimension {

		// CONSTRUCTOR

		public Position(int x_ncell, int y_ncell) {
			super(x_ncell, y_ncell);
		}
		// COPY ? if needed

		Grid.Position copy() {
			return new Position(this.x_ncell, this.y_ncell);
		}

		// EQUALS
		@Override
		public boolean equals(Object o) {
			if (o instanceof Grid.Position) {
				return super.equiv((Grid.Dimension) o);
			}
			return false;
		}

		// TRANSLATION

		public void translate(Vector v) {
			this.x_ncell += v.x_ncell;
			this.y_ncell += v.y_ncell;
			super.normalize();
		}

		void moveNorth(int n_ncell) {
			this.y_ncell -= n_ncell;
			this.y_ncell = Grid.this.yAxis.normalize(this.y_ncell);
		}

		// ROTATION ? if needed

		void rotateAround(Grid.Position position, int angle_degree) {
			int tempX = this.x_ncell;
			int tempY = this.y_ncell;
			double angle_radian = Math.toRadians(angle_degree);
			this.x_ncell = (int) (position.x_ncell + (tempX - position.x_ncell) * Math.cos(angle_radian)
					- (tempY - position.y_ncell) * Math.sin(angle_radian));
			this.y_ncell = (int) (position.y_ncell + (tempX - position.x_ncell) * Math.sin(angle_radian)
					+ (tempY - position.y_ncell) * Math.cos(angle_radian));
			super.normalize();

		}

		// DISTANCE

		double distanceTo(Position p) {

			double distX = Grid.this.xAxis.distance(this.x_ncell, p.x_ncell);
			double distY = Grid.this.yAxis.distance(this.y_ncell, p.y_ncell);
			return Math.sqrt(distX * distX + distY * distY);
		}

		// CONVERSION

		public ISU.Coord toISUCoord() {
			return isu.new Coord(this.x_ncell * Game.game().cmPerCell - Game.game().cmPerCell / 2,
					this.y_ncell * Game.game().cmPerCell - Game.game().cmPerCell / 2);
		}

		public ISU.Coord toISUCoordCentered() {
			return isu.new Coord(this.x_ncell * Game.game().cmPerCell, this.y_ncell * Game.game().cmPerCell);
		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf("LA position " + this.toString() + " est :");
			super.show(ps);
		}

	}

	// === CELL ===

	public class Cell {

		Grid.Dimension size;
		Grid.Position position;
		List<Entity> entities;

		// CONSTRUCTOR

		Cell(Position p) {
			this.position = p;
			this.size = new Grid.Dimension(1, 1);
			this.entities = new ArrayList<Entity>();

		}

		// ADD

		public void add(Entity e) {
			this.entities.add(e);
		}

		// GETTER

		public Grid.Position position() {
			return position;
		}

		public Iterable<Entity> entities() {
			return entities;
		}

		public boolean isEmpty() {
			return entities.isEmpty();
		}

		// REMOVE

		public void remove(Entity e) {
			this.entities.remove(e);
		}

		// PREDICATE

		boolean contains(Entity e) {
			return this.entities.contains(e);
		}

		// SHOW

		void show(PrintStream ps) {
			ps.printf("La cellule " + this.toString() + " est à la position : (%d,%d) et contient %d entitées",
					this.position.x_ncell, this.position.y_ncell, this.entities.size());
		}

	}
}