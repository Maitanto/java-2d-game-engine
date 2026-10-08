package game.gradius.stunt;

import java.util.List;

import engine.entity.Entity;
import engine.gal.CompositeGALStunt;
import engine.gal.arguments.Category;
import engine.gal.arguments.Direction;
import engine.move.Model;
import engine.move.ViewPort;
import game.gradius.entity.Mandibule;
import game.gradius.entity.Ship;
import game.gradius.entity.Boss;
import game.gradius.entity.BossProjectile;
import game.gradius.graphics.BossProjectileAvatar;

public class BossStunt extends CompositeGALStunt {

	Boss boss;
	Mandibule mt;
	Mandibule mb;

	private boolean hasArrived;

	/** Minimum delay between two thrown enemies, in ms. */
	private static final double THROW_COOLDOWN_MS = 500.0; // 1s by default
	private double lastThrowTime;

	/** Speed (cm/s) of the enemies the boss throws. */
	private static final double THROWN_ENEMY_SPEED = 67.0;

	// Charge attack state (driven from Java, triggered by the GAL "Charging" mode).
	private static final double CHARGE_FORWARD_SPEED = 60.0; // cm/s lunging at the player
	private static final double CHARGE_DISTANCE_CM = 60.0; // how far it lunges before recoiling
	private boolean charging;
	private boolean chargeRecoiling;
	private double chargeAnchorOffset;
	private double railSpeedX;

	public BossStunt(Model model, Boss boss) {
		super(model, boss, List.of(boss.mandibuleTop, boss.mandibuleBottom));
		this.mt = boss.mandibuleTop;
		this.mb = boss.mandibuleBottom;
		this.boss = boss;
		hasArrived = false;
		lastThrowTime = 0.0;
	}

	@Override
	public void tick(double elapsed_s) {
		super.tick(elapsed_s);
		ViewPort vp = model.viewPort();

		if (!hasArrived && (boss.center().x()) < vp.originX() + vp.width_cm()) {
			hasArrived = true;
			railSpeedX = vp.getRailSpeedX_cm();
			this.boss.setLinearSpeed(game.isu.new Vector(railSpeedX, 0.0));
		}

		updateCharge(elapsed_s);

		boss.updateMandibules(elapsed_s);

		mt.placeRelativeTo(boss);
		mb.placeRelativeTo(boss);
	}

	private void updateCharge(double elapsed_s) {
		ViewPort vp = model.viewPort();
		if (vp == null) {
			return;
		}
 
		if (boss.isCharging()) {
			double homeX = vp.originX() + chargeAnchorOffset;
 
			if (!charging) {
				charging = true;
				chargeRecoiling = false;
				chargeAnchorOffset = boss.center().x() - vp.originX();
				boss.setLinearSpeed(game.isu.new Vector(railSpeedX - CHARGE_FORWARD_SPEED, 0.0));
			} else if (!chargeRecoiling && (vp.originX() + chargeAnchorOffset - boss.center().x()) >= CHARGE_DISTANCE_CM) {
				chargeRecoiling = true;
				boss.setLinearSpeed(game.isu.new Vector(railSpeedX + CHARGE_FORWARD_SPEED, 0.0));
			} else if (chargeRecoiling && boss.center().x() >= homeX) {
				boss.place(game.isu.new Coord(homeX, boss.center().y()));
				boss.setLinearSpeed(game.isu.new Vector(railSpeedX, 0.0));
				chargeRecoiling = false;
			}
		} else if (charging) {
			boss.place(game.isu.new Coord(vp.originX() + chargeAnchorOffset, boss.center().y()));
			charging = false;
			chargeRecoiling = false;
			boss.setLinearSpeed(game.isu.new Vector(railSpeedX, 0.0));
		}
	}

	@Override
	public boolean startThrowing(Direction direction, double intensity) {
		// On check pour éviter que le Boss spawn un enemi à chaque tick.
		double now = System.currentTimeMillis();
		if (now - lastThrowTime < THROW_COOLDOWN_MS) {
			return true;
		}
		lastThrowTime = now;

		Ship ship = findShip();
		if (ship == null || boss.center() == null) {
			return true;
		}

		throwEnemyAt(ship);
		return true;
	}

	private void throwEnemyAt(Ship ship) {
		if(ship==null) return;
		BossProjectile enemy = new BossProjectile();
		enemy.place(game.isu.new Coord(boss.center().x(), boss.center().y()));
		model.add(enemy);
		model.cullOffScreen(enemy);

		// Vise le vaisseau (mais les projectiles ne le suivent pas)
		double dx = ship.center().x() - boss.center().x();
		double dy = ship.center().y() - boss.center().y();
		double len = Math.hypot(dx, dy);
		if (len < 1e-6) {
			dx = -1.0;
			dy = 0.0;
			len = 1.0;
		}
		double vx = (dx / len) * THROWN_ENEMY_SPEED;
		double vy = (dy / len) * THROWN_ENEMY_SPEED;

		EnemyStunt stunt = new EnemyStunt(model, enemy);
		stunt.setLinearSpeed(game.isu.new Vector(vx, vy));

		game.view.add(new BossProjectileAvatar(enemy));
	}

	private Ship findShip() {
		for (Entity e : model.entities) {
			if (e instanceof Ship s && s.alive()) {
				return s;
			}
		}
		return null;
	}

	@Override
	protected void collision(Entity other) {
		if (other != null && other.category() == Category.Projectile && boss.isAttacking()) {
			if (entity.bot() != null) {
				entity.bot().collision(other, 0.0);
			}
		}
	}

	@Override
	protected void collision(List<Entity> entities) {
		if (entities == null) {
			return;
		}
		for (Entity other : entities) {
			collision(other);
		}
	}
}