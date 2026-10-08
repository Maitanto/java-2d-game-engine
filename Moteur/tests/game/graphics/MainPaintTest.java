package game.graphics;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.controller.Controller;
import engine.controller.KeyManager;
import engine.controller.MouseManager;
import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.arguments.Category;
import engine.gal.aut.AST2Aut;
import engine.gal.aut.Automaton;
import engine.graphics.FpsManager;
import engine.graphics.Background;
import engine.graphics.View;
import engine.graphics.avatars.AnimationAvatar;
import engine.graphics.avatars.ShapeAvatar;
import engine.graphics.hud.Anchor;
import engine.graphics.hud.AnchoredLabel;
import engine.graphics.hud.FollowerLabel;
import engine.graphics.hud.HealthBar;
import engine.graphics.hud.HealthBar.HealthBarStyle;
import engine.graphics.hud.Hud;
import engine.graphics.hud.Label;
import engine.graphics.hud.PixelCoordinate;
import engine.logs.LoggerManager;
import engine.move.Model;
import engine.move.Ticker;
import engine.move.Ticker.TickerListener;
import gal.ast.AST;
import gal.parser.Parser;
import game.Game;
import game.gradius.entity.Boss;
import game.gradius.entity.Cannon;
import game.gradius.entity.CannonSlot;
import game.gradius.entity.Dragon;
import game.gradius.entity.Enemy;

import game.gradius.entity.JetEngine;
import game.gradius.entity.Ship;
import game.gradius.entity.TerrainGenerator;
import game.gradius.entity.Power;
import game.gradius.entity.Obstacle;
import game.gradius.graphics.ObstacleAvatar;
import game.gradius.graphics.PowerAvatar;
import game.gradius.graphics.CannonAvatar;
import game.gradius.graphics.EnemyAvatar;

import game.gradius.graphics.JetEngineAvatar;

import game.gradius.graphics.ShipAvatar;
import game.gradius.spawn.ProjectileSpawner;
import game.gradius.stunt.ShipStunt;
import oop.graphics.Canvas;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;
import oop.graphics.VirtualKeyCodes;
import oop.tasks.Runnable;
import oop.tasks.Runtime;
import oop.tasks.Task;

public class MainPaintTest implements Runnable {

	private static final Logger logger;
	private static final boolean LOGGING;
	private static final boolean FINER;
	private static final int FPS;
	private static final boolean FPS_LOGGING;
	private static final DecimalFormat dfIndex;
	private static final DecimalFormat dfTime;

	private static final boolean SHOULD_DO_PAINT_PROFILING;
	private static final boolean SHOULD_DO_PAINT_PROFILING_LOGGING;
	public static final int WIDTH;
	public static final int HEIGHT;

	static {
		// CONSTANTS
		logger = LoggerManager.getLogger(MainPaintTest.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		FINER = logger.isLoggable(Level.FINER);
		dfIndex = new DecimalFormat("00");
		dfTime = new DecimalFormat("####00");

		FPS = 30;
		FPS_LOGGING = true;

		SHOULD_DO_PAINT_PROFILING = true;
		// to log, you need to activate logging to at least finer in jul logging
		// property file on top of this
		SHOULD_DO_PAINT_PROFILING_LOGGING = false;

		WIDTH = 1920;
		HEIGHT = 1080;

	}

	public static void main(String[] args) throws Exception {
		// RUNNING
		Runnable r = new MainPaintTest();
		Dimension windowSize = new Dimension(WIDTH, HEIGHT);
		Runtime.boot(windowSize, r);
		Runtime.shutdown();
	}

	public final int NB_LAST_TIME_PAINT_SAVED;
	private final int[] ARRAY_LAST_TIME_PAINT_SAVED;
	private int indexArrayTimePaint;
	private int paintingTime;
	private int sumTime;
	private int minTime;
	private int avgTime;
	private int maxTime;
	int o1 = 1;
	int o2 = 1;

	private MainPaintTest() {
		NB_LAST_TIME_PAINT_SAVED = 96;
		ARRAY_LAST_TIME_PAINT_SAVED = new int[NB_LAST_TIME_PAINT_SAVED];
		indexArrayTimePaint = 0;
		paintingTime = -1;
		sumTime = -1;
		minTime = 214748367;// big numbuh
		avgTime = -1;
		maxTime = -1;
	}

	@Override
	public void run() throws Exception {

		logger.info("STARTED SHIP TEST MAIN");

		Task task = Runtime.task();
		Canvas canvas = (Canvas) task.find("canvas");

		Game game = new Game(256, 32);
		Model model = game.model;

		// =========================
		// Category interactions
		// =========================

		Category.setInteraction(Category.Team, Category.Team, false);

		Category.setInteraction(Category.Team, Category.Obstacle, true);
		Category.setInteraction(Category.Obstacle, Category.Team, true);

		Category.setInteraction(Category.Team, Category.Adversary, true);
		Category.setInteraction(Category.Adversary, Category.Team, true);

		Category.setInteraction(Category.Team, Category.Power, true);
		Category.setInteraction(Category.Power, Category.Team, true);

		Category.setInteraction(Category.Projectile, Category.Obstacle, true);
		Category.setInteraction(Category.Obstacle, Category.Projectile, true);

		Category.setInteraction(Category.Projectile, Category.Adversary, true);
		Category.setInteraction(Category.Adversary, Category.Projectile, true);
		Category.setInteraction(Category.Adversary, Category.Adversary, false);

		// =========================
		// Ship composite entity
		// =========================

		Ship ship = new Ship();
		place(ship, 5, game.grid.height() / 2);

		Cannon topCannon = new Cannon(CannonSlot.TOP);
		Cannon bottomCannon = new Cannon(CannonSlot.BOTTOM);
		JetEngine jetengine = new JetEngine();

		ship.attachEngine(jetengine);
		ship.attachCannon(topCannon);
		ship.attachCannon(bottomCannon);

		Power power = new Power();
		Obstacle obstacle = new Obstacle(30, 20);

		// SPAWN DRAGON
		Dragon dragon = new Dragon(6);
		dragon.place(game.grid.new Position(ship.position().x() + 150, ship.position().y() + 10));
		dragon.forceOrientation(270);

		Enemy enemy = new Enemy();

		// Boss
		Boss boss = new Boss();
		place(boss, 80, game.grid.height() / 2);

		// D'abord ajouter les entities au model
		model.add(ship);
		model.add(topCannon);
		model.add(bottomCannon);
		model.add(power);
		model.add(obstacle);
		model.add(jetengine);
		model.add(enemy);
		// Ensuite seulement créer le bot / stunt GAL
		GALBot shipBot = new GALBot(ship);
		ship.bot(shipBot);

		ShipStunt shipStunt = new ShipStunt(model, ship, List.of(topCannon, bottomCannon));

		shipStunt.setMaxLinearSpeed(70.0);

		shipStunt.setMaxAngularSpeed(0.0);
		shipStunt.setBaseLinearSpeed(10.0, 0.0);

		Automaton shipAutomaton = loadAutomaton("src/game/gradius/gal/ship.gal", "Ship");
		shipBot.set(shipAutomaton);

		// =========================
		// View
		// =========================

		View view = game.view;
		model.addTickSystem(view);

		view.add(new ShipAvatar(ship));
		view.add(new CannonAvatar(topCannon));
		view.add(new CannonAvatar(bottomCannon));
		view.add(new PowerAvatar(power));
		view.add(new ObstacleAvatar(obstacle));

		view.add(new JetEngineAvatar(jetengine));
		view.add(new EnemyAvatar(enemy));

		TerrainGenerator terrain = new TerrainGenerator(model, view);
		terrain.generate(); // ou new TerrainGenerator(model, view, seedFixe) pour reproductible

		ProjectileSpawner projectileSpawner = new ProjectileSpawner(model, view);
		shipStunt.setProjectileSpawner(projectileSpawner);

		projectileSpawner.spawn(
				game.isu.new Coord(ship.center().x() + 3 * game.cmPerCell, ship.center().y() - 2 * game.cmPerCell),
				game.isu.new Vector(30.0, 0.0));

		Background bgView = new Background("src/game/gradius/graphics/map_gradius.png", 317, 204, 200, 200);
		view.setBackground(bgView);

		FpsManager fpsC = new FpsManager(Runtime.newTask("FPS_Management"), FPS, FPS_LOGGING);

		Hud hud = new Hud();
		HealthBar health = new HealthBar(ship, Anchor.BOTTOM_RIGHT, new PixelCoordinate(-50, -50), 42, Colors.white,
				Colors.blue, Colors.black, HealthBarStyle.RECT, false);
		hud.add(health);

		FollowerLabel flShipDebugBehavior = new FollowerLabel(() -> ship.debugInfoBehavior(), ship,
				new PixelCoordinate(0, -25), Colors.white, true);
		hud.add(flShipDebugBehavior);
		FollowerLabel flShipDebugMoves = new FollowerLabel(() -> ship.debugInfoMove(), ship, new PixelCoordinate(0, 20),
				Colors.white, true);
		hud.add(flShipDebugMoves);
		FollowerLabel dragonDebugBehavior = new FollowerLabel(() -> dragon.getHead().debugInfoBehavior(),
				dragon.getHead(), new PixelCoordinate(0, -25), Colors.white, true);
		hud.add(dragonDebugBehavior);
		FollowerLabel dragonDebugMoves = new FollowerLabel(() -> dragon.getHead().debugInfoMove(), dragon.getHead(),
				new PixelCoordinate(0, 20), Colors.white, true);
		hud.add(dragonDebugMoves);

		Label labelDebug = new Label(
				() -> "'TAB' to toggle debug mode. 'V' to toggle viewport debug mode. 'P' to pause the game.",
				new PixelCoordinate(6, 12), Colors.white, false, false);
		hud.add(labelDebug);
		Label labelFPS = new Label(
				() -> String.format(" FPS %s { min: %s; avg: %s ; max: %s (from the last %ds) } ",
						fpsC.getFormattedFps(), fpsC.getFormattedMinFps(), fpsC.getFormattedAvgFps(),
						fpsC.getFormattedMaxFps(), fpsC.NB_LAST_FPS_SAVED),
				new PixelCoordinate(12, 24), Colors.white, false, true);
		hud.add(labelFPS);
		Label labelPaintTime = new Label(
				() -> String.format(" PaintTime: %sms { min: %s ; avg: %s ; max: %s (from the last %d paints) }",
						dfTime.format(paintingTime), dfTime.format(minTime), dfTime.format(avgTime),
						dfTime.format(maxTime), NB_LAST_TIME_PAINT_SAVED),
				new PixelCoordinate(12, 36), Colors.white, false, true);
		hud.add(labelPaintTime);
		Label labelTickTime = new Label(
				() -> String.format(" TickTime:  %sms { min: %s ; avg: %s ; max: %s (from the last %d ticks) }",
						model.getFormattedTickTime(), model.getFormattedMinTickTime(), model.getFormattedAvgTickTime(),
						model.getFormattedMaxTickTime(), model.NB_LAST_TICK_TIME_SAVED),
				new PixelCoordinate(12, 48), Colors.white, false, true);
		hud.add(labelTickTime);

		AnchoredLabel labelPause = new AnchoredLabel(() -> "|| GAME IS PAUSED !", Anchor.CENTER,
				new PixelCoordinate(0, 0), Colors.white, true, false);
		hud.add(labelPause);

		view.setHUD(hud);

		canvas.set(new Canvas.PaintListener() {
			@Override
			public void visible(Canvas canvas) {
				fpsC.start(canvas);
			}

			@Override
			public void paint(Canvas canvas, Graphics g) {
				long startTimePaiting = System.currentTimeMillis();

				int windowWidth = canvas.getWidth();
				int windowHeight = canvas.getHeight();

				g.setColor(Colors.black);
				g.fillRect(0, 0, windowWidth, windowHeight);

				view.paint(canvas, g);

				fpsC.countFrame();

				long endTimePainting = System.currentTimeMillis();

				if (SHOULD_DO_PAINT_PROFILING)
					paintProfiling(startTimePaiting, endTimePainting);
			}

			@Override
			public void revoked(Canvas canvas) {
				Runtime.shutdown();
			}
		});

		Runnable toogleDebug = new Runnable() {

			@Override
			public void run() throws Exception {
				Game.toggleDebugMode();
				AnimationAvatar.showHitboxes = Game.debugMode;
			}

		};

		// Le KeyManager capte le clavier, mais il faut aussi lui donner
		// un Controller relié au stunt du vaisseau.
		KeyManager km = new KeyManager();

		// Appuyer sur tab fait apparaître/disparaître le label de fps.
		km.bind(VirtualKeyCodes.VK_TAB, toogleDebug);

		MouseManager mm = new MouseManager();
		// Cliquer sur le bouton du millieu de la souris print un msg dans la console.
		mm.bind(MouseManager.BNO_MIDDLE_BUTTON_MOUSE, () -> {
			try {
				toogleDebug.run();
				ShapeAvatar.drawShapes = Game.debugMode;
			} catch (Exception e) {
				logger.severe(e.getMessage());
			}
		});

		canvas.set(km);
		canvas.set(mm);

		km.addDelegate(new Controller(shipStunt));
		km.bind(VirtualKeyCodes.VK_V, () -> view.toggleDebugViewPort());
		canvas.set(km);

		/*
		 * Le Ticker se lance déjà dans son constructeur.
		 */
		Ticker ticker = new Ticker(model);
		ticker.start();
		km.bind(VirtualKeyCodes.VK_P, () -> ticker.toggleRunning());

		/**
		 * Qu'est ce qu'on fait à la pause et au redémarrage.
		 * 
		 * Exemple : Afficher/Cacher un label
		 */
		ticker.addListener(new TickerListener() {
			@Override
			public void starting() {
				labelPause.setVisibility(false);
			}

			@Override
			public void stopping() {
				labelPause.setVisibility(true);
			}
		});
	}

	private void place(Entity entity, int x, int y) {
		Game game = Game.game();
		entity.place(game.grid.new Position(x, y));
	}

	private Automaton loadAutomaton(String galFilePath, String automatonName) {
		try {
			AST ast = Parser.from_file(galFilePath);
			AST2Aut converter = new AST2Aut(ast);

			for (Automaton automaton : converter.getAutomata()) {
				if (automaton.name().equals(automatonName)) {
					return automaton;
				}
			}

			throw new IllegalArgumentException("Automaton not found: " + automatonName);

		} catch (Exception e) {
			throw new RuntimeException("Cannot load GAL automaton from: " + galFilePath, e);
		}
	}

	private void paintProfiling(long startTimePaiting, long endTimePainting) {
		long elapsedTimePainting = endTimePainting - startTimePaiting;
		paintingTime = (int) elapsedTimePainting;
		sumTime = -1;
		minTime = 214748367;// big numbuh
		avgTime = -1;
		maxTime = -1;

		this.ARRAY_LAST_TIME_PAINT_SAVED[this.indexArrayTimePaint++] = paintingTime;
		this.indexArrayTimePaint %= this.NB_LAST_TIME_PAINT_SAVED;
		for (int i = 0; i < this.NB_LAST_TIME_PAINT_SAVED; i++) {
			int localTime = this.ARRAY_LAST_TIME_PAINT_SAVED[i];
			sumTime += localTime;
			avgTime = sumTime / NB_LAST_TIME_PAINT_SAVED;
			if (localTime < minTime)
				minTime = localTime;
			if (localTime > maxTime)
				maxTime = localTime;
		}
		if (LOGGING && FINER && SHOULD_DO_PAINT_PROFILING_LOGGING) {
			StringBuilder sb = new StringBuilder();
			sb.append("Paiting profiling \n");
			sb.append("   START: ");
			sb.append(startTimePaiting);
			sb.append(" ; END: ");
			sb.append(endTimePainting);
			sb.append(" ; ELAPSED: ");
			sb.append(elapsedTimePainting);
			sb.append('\n');
			sb.append("   time: ");
			sb.append(paintingTime);
			sb.append(" ; min: ");
			sb.append(minTime);
			sb.append(" ; avg: ");
			sb.append(avgTime);
			sb.append(" ; max: ");
			sb.append(maxTime);
			sb.append(" ; sum: ");
			sb.append(sumTime);
			for (int i = 0; i < this.NB_LAST_TIME_PAINT_SAVED; i++) {
				if (i % 8 == 0) {
					sb.append("\n   ");
				}
				sb.append("[");
				sb.append(dfIndex.format(i));
				sb.append(":");
				sb.append(dfTime.format(this.ARRAY_LAST_TIME_PAINT_SAVED[i]));
				sb.append("]");
			}
			logger.log(Level.FINER, sb.toString());
		}
	}
}
