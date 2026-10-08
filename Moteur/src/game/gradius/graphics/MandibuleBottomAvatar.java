package game.gradius.graphics;

import engine.graphics.avatars.SpriteSheet.SpriteSheetCuttingDimension;
import game.gradius.entity.Mandibule;

public class MandibuleBottomAvatar extends MandibuleAvatar{
	
	private static final SpriteSheetCuttingDimension CUT = new SpriteSheetCuttingDimension(36, 636, 112, 80);

	public MandibuleBottomAvatar(Mandibule m) {
		super(m, CUT);

	}

}

