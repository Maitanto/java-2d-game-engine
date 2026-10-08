package game;

import engine.entity.Entity;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.graphics.View;
import engine.move.Model;
import engine.move.ViewPort;

import java.io.PrintStream;

//Ne pas oublier d'ajouter l'option "-cp bin -Djava.util.logging.config.file=jul.properties" aux paramètres de lancement

public class Game {

	public static boolean debugMode = false;

	public static void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public static int score = 0;

	// CONSTANT

	// public final car la taille du monde ne change pas

	public final boolean torusOnXaxis = true; // vrai si l'axe X est une boucle fermée
	public final boolean torusOnYaxis = true; // vrai si l'axe Y est une boucle fermée

	public final double cmPerCell = 3.7; // échelle qui relie l'unité ncell à cm
	public final int pixelPerCm = 2; // échelle qui relie l'unité pixel à cm

	// FIELDS

	public final int width_ncell; // largeur du monde en nombre de cellules
	public final int height_ncell; // hauteur du monde en nombre de celluls

	public final double width_cm; // largeur du monde en cm
	public final double height_cm; // hauteur du monde en cm

	public final Grid grid; // permet la création de coordonnées en unités ncell
	public final ISU isu; // permet la création de coordonnées en unités cm
	public final Model model;
	public final View view;
	// public final Picture pict; // permet la création de coordonnées en unités
	// pixel, ne sera
	// utilisé qu'à
	// partir de Task2

	// CONSTRUCTORS

	public Game(int w_ncell, int h_ncell) {
		this.width_ncell = w_ncell;
		this.height_ncell = h_ncell;

		this.width_cm = this.cmPerCell * this.width_ncell;
		this.height_cm = this.cmPerCell * this.height_ncell;

		assert (width_ncell * cmPerCell == width_cm);
		assert (height_ncell * cmPerCell == height_cm);

		Game.game = this;
		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.model = new Model(this);
		ViewPort vp = new ViewPort(0, 0, w_ncell / 4 * cmPerCell, h_ncell * cmPerCell); // Toute la hauteur, mais 1/4 du
																						// la longueur, car notre jeu
																						// sera rès long mais pas haut.
		vp.rail(10, 0);
		model.setViewPort(vp);
		this.view = new View(vp);

		Entity.bindToGame(this);

	}

	public Game(double w_cm, double h_cm) {

		// erreur des arrondies => epsilon
		double w_cells = w_cm / cmPerCell;
		double h_cells = h_cm / cmPerCell;

		double epsilon = 1e-9;

		if (Math.abs(w_cells - Math.round(w_cells)) > epsilon || Math.abs(h_cells - Math.round(h_cells)) > epsilon) {
			throw new IllegalArgumentException(
					"La taille doit être un multiple de la taille d'une cellule qui vaut : " + cmPerCell);
		}

		this.width_ncell = (int) Math.round(w_cells);
		this.height_ncell = (int) Math.round(h_cells);

		this.height_cm = h_cm;
		this.width_cm = w_cm;

		assert (width_ncell * cmPerCell == width_cm);
		assert (height_ncell * cmPerCell == height_cm);

		Game.game = this;
		this.isu = new ISU(this);
		this.grid = new Grid(this);
		this.model = new Model(this);
		ViewPort vp = new ViewPort(0, 0, w_cm / 2, h_cm);
		vp.rail(10, 0);
		model.setViewPort(vp);
		this.view = new View(vp);

		// Rebind entity world references to THIS game so entities created next
		// deploy into this fresh grid/model (and not a previously built game).
		engine.entity.Entity.bindToGame(this);
	}

	// GETTER

	private static Game game;

	public static Game game() {
		return game;
	}

	// SHOW

	public void show(PrintStream ps) {
		ps.printf(this.toString());
		ps.printf("\n width in cm = %f \n height in cm = %f\n width in cell = %d \n height in cell = %d", this.width_cm,
				this.height_cm, this.width_ncell, this.height_ncell);
	}

}