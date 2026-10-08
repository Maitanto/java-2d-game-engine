package game.gradius.entity;

import java.util.LinkedList;

import engine.entity.Entity;
import engine.gal.GALBot;
import engine.gal.aut.AST2Aut;
import engine.gal.aut.Automaton;
import engine.geometry.Grid;
import engine.geometry.ISU;
import engine.move.FollowerStunt.MovementState;
import gal.ast.AST;
import gal.parser.Parser;
import game.gradius.graphics.DragonBodyAvatar;
import game.gradius.graphics.DragonHeadAvatar;
import game.gradius.stunt.DragonBodyStunt;
import game.gradius.stunt.DragonHeadStunt;

public class Dragon extends Entity {

	private DragonPart[] dragon_parts;
	private int dragon_parts_index;
	private final static int TICK_DELAY;

	static {
		TICK_DELAY = 30;
	}

	private class DragonBot extends GALBot {

		private Automaton automaton2;
		private boolean isPhase2 = false;
		private final int hpSwitch;

		public DragonBot(Entity entity) {
			super(entity);
			Automaton automaton1 = loadAutomaton("src/game/gradius/gal/passiveDragon.gal", "PassiveDragon");
			set(automaton1);
			this.automaton2 = loadAutomaton("src/game/gradius/gal/agressiveDragon.gal", "AgressiveDragon");
			healthPercent(30);
			hpSwitch = 15;
		}

		@Override
		public void tick(double elapsed_ms) {
			super.tick(elapsed_ms);

			if (!isPhase2 && this.life() <= hpSwitch) {
				this.isPhase2 = true;

				if (this.automaton2 != null) {
					set(automaton2);
				}
			}
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
	}

	public Dragon(int nb_segments) {
		super("Dragon");
		dragon_parts_index = 0;
		dragon_parts = new DragonPart[nb_segments + 1];
		DragonHead head = new DragonHead();
		MODEL.add(head);
		GALBot dragonBot = new DragonBot(head);
		dragonBot.healthPercent(30);
		head.bot(dragonBot);
		dragon_parts[dragon_parts_index++] = head;

		DragonHeadStunt headStunt = new DragonHeadStunt(MODEL, head);
		MODEL.add(head, headStunt);
		headStunt.setMaxLinearSpeed(30);
		headStunt.setMaxAngularSpeed(45);

		DragonHeadAvatar headAvatar = new DragonHeadAvatar(head);
		VIEW.add(headAvatar);
		// headAvatar.set_z_order(Avatar.MAX_ZORDER - 1);
		// z_order now set in constructor of DragonPartAvatar
		LinkedList<MovementState> leaderHistory = headStunt.getHistory();
		Entity previousPart = head;
		for (int i = 0; i < nb_segments; i++) {
			DragonBody body = new DragonBody();
			DragonBodyStunt bodyStunt = new DragonBodyStunt(MODEL, body, previousPart, leaderHistory, TICK_DELAY);
			MODEL.add(body, bodyStunt);
			DragonBodyAvatar bodyAvatar = new DragonBodyAvatar(body);
			VIEW.add(bodyAvatar);
			bodyAvatar.set_z_order(Math.max(headAvatar.z_order() - i - 1, 0));
			dragon_parts[dragon_parts_index++] = body;
			previousPart = body;
			leaderHistory = bodyStunt.getMyHistory();
		}
		place(ISU.new Coord(0, 0));
	}
	
	@Override
	public ISU.Coord center() {
	    DragonPart head = getHead();
	    return (head != null) ? head.center() : super.center();
	}

	public DragonPart getHead() {
		return dragon_parts[0];
	}

	public int partCount() {
		return dragon_parts.length; //count all, even the head
	}

	public int bodyCount() {
		return dragon_parts.length - 1; // index 0 is the head
	}

	@Override
	public void place(ISU.Coord center) {
		super.place(center);

		for (Entity part : dragon_parts) {
			part.place(center);
		}
	}

	@Override
	public void place(Grid.Position pos) {
		super.place(pos);

		for (Entity part : dragon_parts) {
			part.place(pos);
		}
	}

	@Override
	public void forceOrientation(double orientation) {
		getHead().forceOrientation(orientation);
	}

	@Override
	public void kill() {
		getHead().kill();
	}

	@Override
	protected void setBounding() {
		clearBounding();
	}

}