package game.gradius.stunt;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.entity.Entity;
import engine.gal.CompositeGALStunt;
import engine.gal.arguments.Direction;
import engine.logs.LoggerManager;
import engine.move.Model;
import game.gradius.entity.Ship;
import oop.utils.SoundPlayer;

/**
 * Basiquement un CompositeGALStunt mais qui confine son entity dans le viewport
 */
public class ShipStunt extends CompositeGALStunt {
	private static final boolean LOGGING;
	private static final Logger logger;

	private static final double NORMAL_SPEED_CM_PER_S;
	private static final double BOOSTED_SPEED_CM_PER_S;

	private static final double DASH_SPEED_CM_PER_S;
	private static final double DASH_DURATION_MS;
	private static final double DASH_COOLDOWN_S;

	private static SoundPlayer laserPlayer;

	public static boolean shouldPlayLaserSound = false;

	static {
		// LOGGER
		logger = LoggerManager.getLogger(ShipStunt.class.getName());
		LOGGING = logger.getLevel() != Level.OFF;
		// CONSTANTS
		NORMAL_SPEED_CM_PER_S = 20.0;
		BOOSTED_SPEED_CM_PER_S = 60.0;
		DASH_SPEED_CM_PER_S = 80.0;
		DASH_DURATION_MS = 150.0;
		DASH_COOLDOWN_S = 0.7;
		// SOUNDS
		try {
			laserPlayer = new SoundPlayer(new File("src/game/gradius/entity/laser.wav"));
			laserPlayer.volume(-10F);
		} catch (Exception e) {
			if (LOGGING)
				logger.severe(String.format("Erreur chargement son laser : %s", e.getMessage()));
		}
	}

	public static void toggleLaserSound() {
		shouldPlayLaserSound = !shouldPlayLaserSound;
	}

	private Ship ship; // on stocke direct le ship plutôt que Entity

	private double dashCooldownRemainingS;

	public ShipStunt(Model model, Ship ship, List<Entity> subEntities) {
		super(model, ship, subEntities);
		setMaxLinearSpeed(NORMAL_SPEED_CM_PER_S);
		this.ship = ship;
		if (LOGGING)
			logger.info("Created new ShipStunt");
	}

	@Override
	public void tick(double elapsed_s) {

		dashCooldownRemainingS = Math.max(0.0, dashCooldownRemainingS - elapsed_s);

		ship.tickPower(elapsed_s);
		updateSpeedPower(ship);

		if (ship.dashAnimationPlaying()) {
			ship.tickDashAnimation(elapsed_s);
			return;
		}

		super.tick(elapsed_s);

		if (model.viewPort() != null) {
			model.viewPort().confine(entity);
		}
	}

	private void updateSpeedPower(Ship ship) {
		if (ship.hasSpeedPower()) {
			setMaxLinearSpeed(BOOSTED_SPEED_CM_PER_S);
		} else {
			setMaxLinearSpeed(NORMAL_SPEED_CM_PER_S);
		}
	}

	@Override
	public boolean startDashing(Direction direction, double intensity) {
		if (direction == null) {
			return false;
		}

		if (dashCooldownRemainingS > 0.0) {
			return false;
		}

		if (actionDuration() > 0.0) {
			return false;
		}

		setMaxLinearSpeed(DASH_SPEED_CM_PER_S);

		boolean started = super.startMoving(direction, intensity, DASH_DURATION_MS);

		updateSpeedPower(ship);
		ship.startDashAnimation();

		if (!started) {

			return false;
		}

		dashCooldownRemainingS = DASH_COOLDOWN_S;
		return true;
	}

	@Override
	public boolean startThrowing(Direction direction, double intensity) {
		if (projectileSpawner() == null) {
			return false;
		}
		double current_time = System.currentTimeMillis();
		elapsed = current_time - last_time;
		if (elapsed > expected_time) {
			if (laserPlayer != null && shouldPlayLaserSound) {
				laserPlayer.play(1); // TODO cause de perte de fps somehow, sans ça lag bcp moins
			}
			projectileSpawner().spawnFrom(entity, direction, intensity);
			for (Entity subEntity : subEntities) {
				if (!subEntity.dead()) {
					projectileSpawner().spawnFrom(subEntity, direction, intensity);
				}
			}
			last_time = current_time;
		}
		return true;
	}
}
