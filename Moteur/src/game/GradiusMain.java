package game;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.controller.Controller;
import engine.controller.KeyManager;
import engine.controller.MouseManager;
import engine.entity.Entity;
import engine.gal.Bot;
import engine.gal.GALBot;
import engine.gal.GALStunt;
import engine.gal.arguments.Category;
import engine.gal.aut.AST2Aut;
import engine.gal.aut.Automaton;
import engine.graphics.Background;
import engine.graphics.FpsManager;
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
import game.gradius.entity.Boss;
import game.gradius.entity.Cannon;
import game.gradius.entity.CannonSlot;
import game.gradius.entity.Dragon;
import game.gradius.entity.Enemy;
import game.gradius.entity.JetEngine;
import game.gradius.entity.Ship;
import game.gradius.entity.TerrainGenerator;
import game.gradius.entity.Power;
import game.gradius.graphics.CannonAvatar;
import game.gradius.graphics.EnemyAvatar;
import game.gradius.graphics.JetEngineAvatar;
import game.gradius.graphics.PowerAvatar;
import game.gradius.graphics.ShipAvatar;
import game.gradius.spawn.ProjectileSpawner;
import game.gradius.stunt.EnemyStunt;
import game.gradius.stunt.ShipStunt;
import oop.graphics.Canvas;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;
import oop.graphics.VirtualKeyCodes;
import oop.tasks.Runnable;
import oop.tasks.Runtime;
import oop.tasks.Task;

public class GradiusMain implements Runnable {

	// ── Fenêtre ──────────────────────────────────────────────────────────────
	public static final int WIDTH;
	public static final int HEIGHT;
	private static final int FPS;
	// ── Monde ────────────────────────────────────────────────────────────────
	private static final int WORLD_W_CELLS;
	private static final int WORLD_H_CELLS;

	// ── Spawn configuration ──────────────────────────────────────────────────
	private static final int POWER_COOLDOWN_MS;
	private static final int DIST_MIN_TOO_FAR;
	private static final int DIST_MAX_TOO_FAR;
	private static final int LIMIT_ALIVE_ENEMY;
	private static final int MIN_ENEMY_DELAY;
	private static final int MAX_ENEMY_DELAY;
	private static final double ENEMY_SPAWN_MARGIN_Y;
	private static final int MAX_DRAGON_SEGMENTS;
	private static final int MIN_DRAGON_SEGMENTS;
	private static final int DRAGON_COOLDOWN_MS;
	private static final int BOSS_COOLDOWN_MS;
	private static final int MIN_ENNEMIES_SPAWNED_BEFORE_DRAGON;
	private static final int MIN_SCORE_BEFORE_BOSS;

	// ── Profiling paint ──────────────────────────────────────────────────────
	private static final int NB_PAINT_SAMPLES;
	private static final boolean SHOULD_DO_PAINT_PROFILING;
	private static final boolean SHOULD_DO_PAINT_PROFILING_LOGGING;

	// ── Logging ──────────────────────────────────────────────────────

	private static final Logger logger;
	private static final boolean LOGGING;
	private static final boolean FINER;
	private static final boolean FPS_LOGGING;
	private static final DecimalFormat dfIndex;
	private static final DecimalFormat dfTime;

	// ── Other ──────────────────────────────────────────────────────
	private static final double PLAYER_MAX_SPEED;
	private static final Random RANDOM;

	static {
		// CONSTANTS
		logger = LoggerManager.getLogger(GradiusMain.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		FINER = logger.isLoggable(Level.FINER);
		dfIndex = new DecimalFormat("00");
		dfTime = new DecimalFormat("####00");

		FPS = 30;
		FPS_LOGGING = false;

		NB_PAINT_SAMPLES = 96;
		SHOULD_DO_PAINT_PROFILING = true;
		// to log, you need to activate logging to at least finer in jul logging
		// property file on top of this
		SHOULD_DO_PAINT_PROFILING_LOGGING = false;

		WIDTH = 1920;
		HEIGHT = 1080;
		WORLD_W_CELLS = 256;
		WORLD_H_CELLS = 32;

		POWER_COOLDOWN_MS = 15000;
		DIST_MIN_TOO_FAR = 32;
		DIST_MAX_TOO_FAR = 128;
		LIMIT_ALIVE_ENEMY = 8;
		ENEMY_SPAWN_MARGIN_Y = 0.20;
		MIN_ENEMY_DELAY = 800;
		MAX_ENEMY_DELAY = 1700;
		MAX_DRAGON_SEGMENTS = 8;
		MIN_DRAGON_SEGMENTS = 2;
		DRAGON_COOLDOWN_MS = 45000; // ~45 s entre dragons
		BOSS_COOLDOWN_MS = 60000; // ~1min
		MIN_ENNEMIES_SPAWNED_BEFORE_DRAGON = 32;
		MIN_SCORE_BEFORE_BOSS = 67;

		PLAYER_MAX_SPEED = 80.0;

		RANDOM = new Random();
	}

	public static void main(String[] args) throws Exception {
		Runtime.boot(new Dimension(WIDTH, HEIGHT), new GradiusMain());
		Runtime.shutdown();
	}

	private final int[] paintSamples;
	private int indexArrayTimePaint;
	private int timePaint, minPaint, avgPaint, maxPaint, sumPaint;

	private GradiusMain() {
		paintSamples = new int[NB_PAINT_SAMPLES];
		indexArrayTimePaint = 0;
		timePaint = -1;
		sumPaint = -1;
		minPaint = 214748367;// big numbuh
		avgPaint = -1;
		maxPaint = -1;
	}

	private Dragon currentDragon = null;

	@Override
	public void run() throws Exception {
		if (LOGGING)
			logger.info("=== GradiusMain : démarrage ===");

		Task task = Runtime.task();
		Canvas canvas = (Canvas) task.find("canvas");

		Game game = new Game(WORLD_W_CELLS, WORLD_H_CELLS);
		Model model = game.model;

		View view = game.view;
		model.addTickSystem(view);

		Background bgView = new Background("src/game/gradius/graphics/map_gradius.png", 317, 204, 200, 200);
		view.setBackground(bgView); // on doit utiliser un method reference operator sinon ça marche pas

		// ── Interactions de catégories ────────────────────────────────────────
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

		// ── Ship ──────────────────────────────────────────────────────────────
		Ship ship = new Ship();
		ship.place(game.grid.new Position(5, game.grid.height() / 2));

		Cannon topCannon = new Cannon(CannonSlot.TOP);
		ship.attachCannon(topCannon);
		Cannon bottomCannon = new Cannon(CannonSlot.BOTTOM);
		ship.attachCannon(bottomCannon);
		JetEngine jetengine = new JetEngine();
		ship.attachEngine(jetengine);

		model.add(ship);
		model.add(topCannon);
		model.add(bottomCannon);

		GALBot shipBot = new GALBot(ship);
		ship.bot(shipBot);
		ShipStunt shipStunt = new ShipStunt(model, ship, List.of(topCannon, bottomCannon));
		shipStunt.setMaxLinearSpeed(PLAYER_MAX_SPEED);
		shipStunt.setMaxAngularSpeed(0.0);
		shipStunt.setBaseLinearSpeed(model.viewPort().getRailSpeedX_cm(), 0.0);

		Automaton shipAutomaton = loadAutomaton("src/game/gradius/gal/ship.gal", "Ship");
		shipBot.set(shipAutomaton);

		view.add(new ShipAvatar(ship));
		view.add(new CannonAvatar(topCannon));
		view.add(new CannonAvatar(bottomCannon));
		view.add(new JetEngineAvatar(jetengine));

		// ── Projectiles & Terrain ─────────────────────────────────────────────
		ProjectileSpawner projectileSpawner = new ProjectileSpawner(model, view);
		shipStunt.setProjectileSpawner(projectileSpawner);

		TerrainGenerator terrain = new TerrainGenerator(model, view);
		terrain.generate();

		// ── HUD ──────────────────────────────────────────────────

		FpsManager fpsC = new FpsManager(task, FPS, FPS_LOGGING);
		Hud hud = new Hud();

		HealthBar health = new HealthBar(ship, Anchor.BOTTOM_RIGHT, new PixelCoordinate(-50, -50), 51, Colors.white,
				Colors.blue, Colors.black, HealthBarStyle.CIRCLES, false);
		health.setVisibility(true);
		hud.add(health);

		FollowerLabel flShipBehavior = new FollowerLabel(() -> ship.debugInfoBehavior(), ship,
				new PixelCoordinate(0, -25), Colors.white, true);
		hud.add(flShipBehavior);

		FollowerLabel flShipMoves = new FollowerLabel(() -> ship.debugInfoMove(), ship, new PixelCoordinate(0, 20),
				Colors.white, true);
		hud.add(flShipMoves);

		AnchoredLabel labelScore = new AnchoredLabel(() -> String.format("Score %d", Game.score), Anchor.TOP_CENTER,
				new PixelCoordinate(0, 12), Colors.white, true, false);
		labelScore.update(WIDTH, HEIGHT);
		hud.add(labelScore);

		Label labelHelp = new Label(() -> "TAB: debug | V: viewport | P: pause | M: mute/unmute laser sounds",
				new PixelCoordinate(6, 12), Colors.white, false, false);
		hud.add(labelHelp);

		Label labelFPS = new Label(() -> String.format(" FPS %s { min: %s; avg: %s; max: %s (last %ds) }",
				fpsC.getFormattedFps(), fpsC.getFormattedMinFps(), fpsC.getFormattedAvgFps(), fpsC.getFormattedMaxFps(),
				fpsC.NB_LAST_FPS_SAVED), new PixelCoordinate(12, 24), Colors.white, false, true);
		labelFPS.setVisibility(false);
		hud.add(labelFPS);
		Label labelPaintTime = new Label(
				() -> String.format(" PaintTime: %sms { min: %s ; avg: %s ; max: %s (from the last %d paints) }",
						dfTime.format(timePaint), dfTime.format(minPaint), dfTime.format(avgPaint),
						dfTime.format(maxPaint), NB_PAINT_SAMPLES),
				new PixelCoordinate(12, 36), Colors.white, false, true);
		labelPaintTime.setVisibility(false);
		hud.add(labelPaintTime);
		Label labelTick = new Label(
				() -> String.format(" TickTime: %sms { min: %s; avg: %s; max: %s (last %d) }",
						model.getFormattedTickTime(), model.getFormattedMinTickTime(), model.getFormattedAvgTickTime(),
						model.getFormattedMaxTickTime(), model.NB_LAST_TICK_TIME_SAVED),
				new PixelCoordinate(12, 48), Colors.white, false, true);
		labelTick.setVisibility(false);
		hud.add(labelTick);

		AnchoredLabel labelPause = new AnchoredLabel(() -> "|| GAME IS PAUSED !", Anchor.CENTER,
				new PixelCoordinate(0, 0), Colors.white, true, false);
		labelPause.setVisibility(false);
		hud.add(labelPause);

		view.setHUD(hud);

		// ── GAME DIRECTOR (Gestion des spawns en continu) ──────────────────────
		Entity directorEntity = new Entity("Director") {
			@Override
			protected void setBounding() {
				clearBounding();
			}
		};
		directorEntity.place(game.grid.new Position(0, 0));

		Bot directorBot = new Bot(directorEntity) {
			private long lastEnemySpawnTime = System.currentTimeMillis();
			private long nextEnemyDelay = 500; // Le premier ennemi arrive très vite (0.5s)
			private long lastPowerSpawnTime = System.currentTimeMillis();
			private int totalEnemiesSpawned = 0; // On part de 0 !

			/**
			 * On fait en sorte que le dragon puisse spawn plusieurs fois dans la game
			 */
			private long lastDragonTime = System.currentTimeMillis();
			private boolean firstDragonDone = false;

			private int bossSpawnCount = 0; // Compteur de boss apparus
			private long lastBossDeathTime = 0; // Temps de la mort du boss précédent
			private Boss currentBoss = null; // boss vivant courant

			private Power currentPower = null; // one power at a time or it's annoying

			@Override
			public void tick(double elapsed_ms) {
				super.tick(elapsed_ms);
				long now = System.currentTimeMillis();

				int aliveEnemies = 0;
				for (Entity e : model.entities) {
					if ((e instanceof Enemy || e instanceof Power) && e.alive()) {
						int distBehind = (ship.position().x() - e.position().x() + WORLD_W_CELLS) % WORLD_W_CELLS;
						if (distBehind > DIST_MIN_TOO_FAR && distBehind < DIST_MAX_TOO_FAR) {
							e.kill();
						} else {
							aliveEnemies++;
						}
					}
				}

				// Spawn d'ennemis espacé et organique
				if (now - lastEnemySpawnTime > nextEnemyDelay) {
					if (aliveEnemies < LIMIT_ALIVE_ENEMY) {
						spawnOneEnemy(model, game, view, ship);
						totalEnemiesSpawned++;
					}
					lastEnemySpawnTime = now;

					// Le prochain ennemi apparaîtra dans un délai aléatoire entre 0.8s et 2.5s
					nextEnemyDelay = MIN_ENEMY_DELAY + (long) (Math.random() * MAX_ENEMY_DELAY);
				}

				// Statut du boss
				boolean bossAlive = currentBoss != null && currentBoss.alive();

				// Mort du Boss
				if (currentBoss != null && currentBoss.dead()) {
					Game.score += Boss.LIFE_AMOUNT;
					lastBossDeathTime = now; // On enregistre le timestamp de la mort
					currentBoss = null;
					bossAlive = false;
				}

				// Spawn du Dragon
				boolean dragonAlive = currentDragon != null && currentDragon.getHead() != null
						&& currentDragon.getHead().alive();

				if (!dragonAlive && currentDragon != null) {
					Game.score += currentDragon.partCount();
					lastDragonTime = now;
					currentDragon = null;
				}

				boolean isFirstBossActive = (bossSpawnCount == 1) && bossAlive;

				boolean firstReady = !firstDragonDone && totalEnemiesSpawned >= MIN_ENNEMIES_SPAWNED_BEFORE_DRAGON;
				boolean cooldownReady = firstDragonDone && (now - lastDragonTime > DRAGON_COOLDOWN_MS);

				// Pour le premier boss, on évite d'avoir le dragon en même temps
				if (!dragonAlive && !isFirstBossActive && (firstReady || cooldownReady)) {
					currentDragon = spawnDragon(model, game, hud, ship);
					firstDragonDone = true;
				}

				// Spawn occasionnel de bonus Power (toutes les 20 secondes)
				if (currentPower == null && now - lastPowerSpawnTime > POWER_COOLDOWN_MS) {
					// 1 power à la fois, on les tue ailleurs
					// if(currentPower!=null) currentPower.kill();
					currentPower = spawnPower(model, game, view, ship);
					lastPowerSpawnTime = now;
				}

				// Spawn du Boss
				int requiredScore = MIN_SCORE_BEFORE_BOSS * (bossSpawnCount + 1);
				boolean bossCooldownReady = (bossSpawnCount == 0) || (now - lastBossDeathTime >= BOSS_COOLDOWN_MS); // 1
																													// minute
																													// (60
																													// 000
																													// ms)

				if (!bossAlive && Game.score >= requiredScore && bossCooldownReady) {
					currentBoss = spawnBoss(model, game, hud, ship);
					bossSpawnCount++;
					bossAlive = true;

					if (bossSpawnCount == 1 && dragonAlive) {
						currentDragon.kill();
						dragonAlive = false;
					}
				}
			}
		};
		directorEntity.bot(directorBot);
		model.add(directorEntity);
		new GALStunt(model, directorEntity);

		// ── Canvas / rendu ────────────────────────────────────────────────────
		canvas.set(new Canvas.PaintListener() {
			@Override
			public void visible(Canvas canvas) {
				fpsC.start(canvas);
			}

			@Override
			public void paint(Canvas canvas, Graphics g) {
				long t0 = System.currentTimeMillis();
				g.setColor(Colors.black);
				g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
				view.paint(canvas, g);
				fpsC.countFrame();
				if (SHOULD_DO_PAINT_PROFILING)
					recordPaintTime(t0, System.currentTimeMillis());
			}

			@Override
			public void revoked(Canvas canvas) {
				Runtime.shutdown();
			}
		});

		// ── Toggle debug ──────────────────────────────────────────────────────
		Runnable toggleDebug = () -> {
			Game.toggleDebugMode();
			AnimationAvatar.showHitboxes = Game.debugMode;
		};

		// ── Clavier & Souris ──────────────────────────────────────────────────
		KeyManager km = new KeyManager();
		km.bind(VirtualKeyCodes.VK_TAB, toggleDebug);
		km.bind(VirtualKeyCodes.VK_V, () -> view.toggleDebugViewPort());
		km.bind(VirtualKeyCodes.VK_M, () -> ShipStunt.toggleLaserSound());
		km.bind(VirtualKeyCodes.VK_NUMPAD0, () -> spawnPower(model, game, view, ship));
		km.bind(VirtualKeyCodes.VK_NUMPAD1, () -> spawnOneEnemy(model, game, view, ship));
		km.bind(VirtualKeyCodes.VK_NUMPAD2, () -> spawnDragon(model, game, hud, ship));
		km.bind(VirtualKeyCodes.VK_NUMPAD3, () -> spawnBoss(model, game, hud, ship));
		km.bind(VirtualKeyCodes.VK_K, () -> {
			if (currentDragon != null)
				currentDragon.kill();
		});
		km.addDelegate(new Controller(shipStunt));
		canvas.set(km);

		MouseManager mm = new MouseManager();
		mm.bind(MouseManager.BNO_MIDDLE_BUTTON_MOUSE, () -> {
			try {
				toggleDebug.run();
				ShapeAvatar.drawShapes = Game.debugMode;
			} catch (Exception ex) {
				if (LOGGING)
					logger.severe(ex.getMessage());
			}
		});
		canvas.set(mm);

		// ── Ticker + pause ────────────────────────────────────────────────────
		Ticker ticker = new Ticker(model);
		ticker.start();
		km.bind(VirtualKeyCodes.VK_P, () -> ticker.toggleRunning());

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

	// =========================================================================
	// Helpers de spawn
	// =========================================================================

	private static Enemy spawnOneEnemy(Model model, Game game, View view, Ship ship) {
		Enemy enemy = new Enemy();
		Random rng = new Random();

		// Variation sur l'axe X pour que les ennemis ne soient pas alignés
		int spawnX = (ship.position().x() + (int) (model.viewPort().width_cm() / game.cmPerCell) + (rng.nextInt(17)+9))
				% WORLD_W_CELLS;

		int minY = (int) Math.ceil(WORLD_H_CELLS * ENEMY_SPAWN_MARGIN_Y);
		int maxY = (int) Math.floor(WORLD_H_CELLS * (1.0 - ENEMY_SPAWN_MARGIN_Y));
		int ey = minY + rng.nextInt(maxY - minY + 1);

		enemy.place(game.grid.new Position(spawnX, ey));
		model.add(enemy);

		double vx = -(10.0 + rng.nextDouble() * 10.0);
		double vy = (rng.nextDouble() - 0.5) * 8.0;
		EnemyStunt stunt = new EnemyStunt(model, enemy);
		stunt.setLinearSpeed(game.isu.new Vector(vx, vy));

		view.add(new EnemyAvatar(enemy));
		return enemy;
	}

	private static Power spawnPower(Model model, Game game, View view, Ship ship) {
		Power power = new Power();
		Random rng = new Random();

		int spawnX = (ship.position().x() + (int) (model.viewPort().width_cm() / game.cmPerCell) + (rng.nextInt(13)+7))
				% WORLD_W_CELLS;
		int minY = (int) Math.ceil(WORLD_H_CELLS * ENEMY_SPAWN_MARGIN_Y);
		int maxY = (int) Math.floor(WORLD_H_CELLS * (1.0 - ENEMY_SPAWN_MARGIN_Y));
		int ey = minY + rng.nextInt(maxY - minY + 1);

		power.place(game.grid.new Position(spawnX, ey));
		model.add(power);
		view.add(new PowerAvatar(power));
		return power;
	}

	private static Dragon spawnDragon(Model model, Game game, Hud hud, Ship ship) {
		Dragon dragon = new Dragon(RANDOM.nextInt(MAX_DRAGON_SEGMENTS + 1 - MIN_DRAGON_SEGMENTS) + MIN_DRAGON_SEGMENTS);

		// On le fait apparaître à 80 cases devant le joueur pour qu'il ait largement le
		// temps de se déplier
		int spawnX = (ship.position().x() + 80) % WORLD_W_CELLS;

		// Placé sous la ligne de sol (WORLD_H_CELLS - 4)
		dragon.place(game.grid.new Position(spawnX, WORLD_H_CELLS - 4));
		dragon.forceOrientation(270);

		FollowerLabel dragonBehavior = new FollowerLabel(() -> dragon.getHead().debugInfoBehavior(), dragon.getHead(),
				new PixelCoordinate(0, -25), Colors.white, true);
		hud.add(dragonBehavior);

		FollowerLabel dragonMoves = new FollowerLabel(() -> dragon.getHead().debugInfoMove(), dragon.getHead(), new PixelCoordinate(0, 20),
				Colors.white, true);
		hud.add(dragonMoves);

		// ATTENTION : On n'utilise PLUS model.add(dragon) ici !
		// L'entité composée s'ajoute d'elle-même. Cela règle le bug de téléportation
		// physique.
		if (LOGGING)
			logger.info("Boss Dragon généré (20 kills atteints) à X=" + spawnX);

		return dragon;
	}

	private static Boss spawnBoss(Model model, Game game, Hud hud, Ship ship) {
		Boss boss = new Boss();

		// On le fait apparaître à 80 cases devant le joueur pour lui laisser le temps
		// de se préparer
		int spawnX = (ship.position().x() + 80) % WORLD_W_CELLS;
		boss.place(game.grid.new Position(spawnX, game.grid.height() / 2));

		FollowerLabel bossBehavior = new FollowerLabel(() -> boss.debugInfoBehavior(), boss,
				new PixelCoordinate(-300, -25), Colors.white, true);
		hud.add(bossBehavior);

		FollowerLabel bossMoves = new FollowerLabel(() -> boss.debugInfoMove(), boss, new PixelCoordinate(-300, 20),
				Colors.white, true);
		hud.add(bossMoves);
		HealthBar healthBarBoss = new HealthBar(boss, Anchor.BOTTOM_CENTER, new PixelCoordinate(0, -50), 666, 16,
				Colors.white, Colors.red, Colors.black, HealthBarStyle.RECT, false);
		hud.add(healthBarBoss);

		if (LOGGING)
			logger.info("Boss final généré (Score >= 67) à X=" + spawnX);

		return boss;
	}

	// =========================================================================
	// Helpers techniques
	// =========================================================================

	private Automaton loadAutomaton(String path, String name) {
		try {
			AST ast = Parser.from_file(path);
			AST2Aut converter = new AST2Aut(ast);
			for (Automaton a : converter.getAutomata()) {
				if (a.name().equals(name))
					return a;
			}
			throw new IllegalArgumentException("Automate introuvable : " + name);
		} catch (Exception e) {
			throw new RuntimeException("Impossible de charger " + path, e);
		}
	}

	private void recordPaintTime(long t0, long t1) {
		timePaint = (int) (t1 - t0);
		paintSamples[indexArrayTimePaint++ % NB_PAINT_SAMPLES] = timePaint;
		sumPaint = 0;
		minPaint = Integer.MAX_VALUE;
		maxPaint = 0;
		for (int v : paintSamples) {
			sumPaint += v;
			if (v > 0 && v < minPaint)
				minPaint = v;
			if (v > maxPaint)
				maxPaint = v;
		}
		avgPaint = sumPaint / NB_PAINT_SAMPLES;
		if (LOGGING && FINER && SHOULD_DO_PAINT_PROFILING_LOGGING) {
			StringBuilder sb = new StringBuilder();
			sb.append("Paiting profiling \n");
			sb.append("   START: ");
			sb.append(t0);
			sb.append(" ; END: ");
			sb.append(t1);
			sb.append('\n');
			sb.append("   time: ");
			sb.append(timePaint);
			sb.append(" ; min: ");
			sb.append(minPaint);
			sb.append(" ; avg: ");
			sb.append(avgPaint);
			sb.append(" ; max: ");
			sb.append(maxPaint);
			sb.append(" ; sum: ");
			sb.append(sumPaint);
			for (int i = 0; i < NB_PAINT_SAMPLES; i++) {
				if (i % 8 == 0) {
					sb.append("\n   ");
				}
				sb.append("[");
				sb.append(dfIndex.format(i));
				sb.append(":");
				sb.append(dfTime.format(this.paintSamples[i]));
				sb.append("]");
			}
			logger.log(Level.FINER, sb.toString());
		}
	}
}