package game.gradius.entity;

import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.arguments.Category;
import engine.gal.aut.AST2Aut;
import engine.gal.aut.Automaton;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.move.BasicStunt;
import engine.shape.Circle;
import gal.ast.AST;
import gal.parser.Parser;
import game.gradius.graphics.BossAvatar;
import game.gradius.graphics.MandibuleBottomAvatar;
import game.gradius.graphics.MandibuleTopAvatar;
import game.gradius.stunt.BossStunt;
import game.gradius.stunt.MandibuleStunt;

public class Boss extends Entity {
	private static final double OPEN_AMOUNT_FOR_GOOD_CLOSE = 3;
	public static final int LIFE_AMOUNT = 250;

	private double base_speed;

	public final Mandibule mandibuleTop;
	public final Mandibule mandibuleBottom;
	public double openAmount;
	public double maxOpenAmount;
	public double minOpenAmount;

	public Boss() {
		super("Boss");
		base_speed = 0;

		setStep(ISU.new Dimension(CM_PER_CELL, CM_PER_CELL));
		category(Category.Adversary);

		//placement temporaire pour éviter d'avoir des valeurs à null
		place(GRID.new Position(0, GRID.height() / 2));
		
		

		mandibuleTop = new Mandibule(this, true);
		mandibuleBottom = new Mandibule(this, false);

		this.openAmount = OPEN_AMOUNT_FOR_GOOD_CLOSE;
		this.minOpenAmount = OPEN_AMOUNT_FOR_GOOD_CLOSE; // fully closed
		this.maxOpenAmount = OPEN_AMOUNT_FOR_GOOD_CLOSE; // pas la valeur définitive de maxOpenAmount, celle-ci est
															// vraiment
		// set dans le setBounding

		// boss.openMandibules();
		MODEL.add(mandibuleTop);
		MODEL.add(mandibuleBottom);
		MODEL.add(this);

		// The bot MUST exist before the GAL stunts are constructed !
		GALBot bossBot = new GALBot(this);
		bossBot.sethealth(LIFE_AMOUNT);
		this.bot(bossBot);

		BasicStunt sm1 = new MandibuleStunt(MODEL, mandibuleTop);
		BasicStunt sm2 = new MandibuleStunt(MODEL, mandibuleBottom);
		sm1.setLinearSpeed(ISU.new Vector(base_speed, 0));
		sm2.setLinearSpeed(ISU.new Vector(base_speed, 0));

		BossStunt bossStunt = new BossStunt(MODEL, this);
		bossStunt.setBaseLinearSpeed(base_speed, 0);

		Automaton bossAutomaton = loadAutomaton("src/game/gradius/gal/boss.gal", "Boss");
		bossBot.set(bossAutomaton);

		mandibuleTop.placeRelativeTo(this);
		mandibuleBottom.placeRelativeTo(this);

		VIEW.add(new MandibuleTopAvatar(mandibuleTop));
		VIEW.add(new MandibuleBottomAvatar(mandibuleBottom));
		VIEW.add(new BossAvatar(this));

	}

	@Override
	protected void setBounding() {
		clearBounding();

		setSize(ISU.new Dimension(40 * CM_PER_CELL, 30.0 * CM_PER_CELL));
		addBounding(new Circle(center(), size().y() / 2));
		this.maxOpenAmount = size().y() / 12;
	}

//	public void openMandibules() {
//		for(Mandibule m : mandibules) {
//			if(m!=null) {
//				m.openMandibule();
//			}
//		}
//	}
//	public void closeMandibules() {
//		for(Mandibule m : mandibules) {
//			if(m!=null) {
//				m.closeMandibule();
//			}
//		}
//	}

//	private void syncMandibules() {
//		for(Mandibule m : mandibules) {
//			if(!m.dead()) {
//				m.placeRelativeTo(this);
//			}
//		}
//	}

	// LIFE
	@Override
	public void kill() {
		if (dead()) {
			return;
		}

		super.kill();

		mandibuleTop.kill();
		mandibuleBottom.kill();
	}

	// MOVEMENT

	@Override
	public void place(Grid.Position position) {
		super.place(position);
		// syncMandibules();
	}

	@Override
	public void place(ISU.Coord center) {
		super.place(center);
		// syncMandibules();
	}

	@Override
	public void translate(ISU.Vector v) {
		super.translate(v);
		// syncMandibules();
	}

	@Override
	public void translate(Grid.Vector v) {
		super.translate(v);
		// syncMandibules();
	}

	public void collisionsWithMandibule() {
		// on prévient le boss quand ses mandibules sont attaqué.
		// TODO pas utilisé pour l'instant
	}

	private static Automaton loadAutomaton(String galFilePath, String automatonName) {
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

	/**
	 * @return true si le boss est dans le state d'attaque, false si il est dans le
	 *         state defense.
	 */
	public boolean isAttacking() {
		if (bot() == null || bot().state() == null) {
			return false;
		}
		// Le state d'attaque est "Fighting" dans boss.gal.
		return "Fighting".equals(bot().state().mode().toString());
	}

	/**
	 * @return true si le boss est dans sa phase "Charge", false sinon
	 */
	public boolean isCharging() {
		if (bot() == null || bot().state() == null) {
			return false;
		}
		return "Charging".equals(bot().state().mode().toString());
	}

	/** À quel vitesse les mandibules s'ouvrent ou se ferment */
	private static final double OPEN_SPEED_PER_S = 12.0;

	public void updateMandibules(double elapsed_s) {
		// Mandibules sont ouvertes durant les phases d'attaques (Fighting and
		// Charging), elles sont fermés dans les phases de défense (Blocking)
		double target = (isAttacking()) ? maxOpenAmount : minOpenAmount;
		double step = OPEN_SPEED_PER_S * elapsed_s;

		if (openAmount < target) {
			openAmount = Math.min(target, openAmount + step);
		} else if (openAmount > target) {
			openAmount = Math.max(target, openAmount - step);
		}
	}

}