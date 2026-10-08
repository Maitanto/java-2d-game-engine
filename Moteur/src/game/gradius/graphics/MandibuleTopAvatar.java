package game.gradius.graphics;

import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import game.gradius.entity.Mandibule;

public class MandibuleTopAvatar extends MandibuleAvatar {

	private static final SpriteSheetCuttingDimension CUT = new SpriteSheetCuttingDimension(21, 51, 112, 80);

	public MandibuleTopAvatar(Mandibule m) {
		super(m, CUT);

	}

}
