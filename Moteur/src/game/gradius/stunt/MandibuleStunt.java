package game.gradius.stunt;

import java.util.List;

import engine.entity.Entity;
import engine.move.BasicStunt;
import engine.move.Model;
import game.gradius.entity.Mandibule;

public class MandibuleStunt extends BasicStunt {

	protected Mandibule m;

	public MandibuleStunt(Model model, Mandibule m) {
		super(model, m);
		this.m = m;
	}

	@Override
	protected void collision(Entity other) {
		// on fait rien, surtout on coupe pas la vitesse sinon ça pose problème avec la
		// synchronisation du reste du corps du boss
		m.owner().collisionsWithMandibule(); // on prévient le boss que ses mandibules sont attackés

	}

	@Override
	protected void collision(List<Entity> entities) {
		// on fait rien, surtout on coupe pas la vitesse sinon ça pose problème avec la
		// synchronisation du reste du corps du boss
		m.owner().collisionsWithMandibule(); // on prévient le boss que ses mandibules sont attackés
	}

}
