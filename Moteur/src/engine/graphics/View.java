package engine.graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import engine.geometry.ISU;
import engine.graphics.avatars.Avatar;
import engine.graphics.hud.Hud;
import engine.graphics.hud.PixelCoordinate;
import engine.move.Model.TickSystem;
import engine.entity.Entity;
import engine.geometry.Grid;
import engine.move.ViewPort;
import game.Game;
import oop.graphics.Canvas;
import oop.graphics.Color;
import oop.graphics.Graphics;
import oop.graphics.Graphics.Colors;

public class View implements TickSystem {

	private static final Game GAME;
	private static final int NB_CELL_MARGIN_CULLING;
	static {
		GAME = Game.game();
		NB_CELL_MARGIN_CULLING = 1;
	}

	private final List<List<Avatar>> buckets;
	private final Map<Entity, Avatar> byEntity;
	private final List<List<Avatar>> entitylessAvatarsBucket;
	private final List<List<Avatar>> visibleAvatarsBuckets;
	private final Set<Entity> gathered;
	private final Set<Avatar> dyingAvatars;

	private int avatarCount;

	private final ViewPort vp;
	private Canvas canvas;

	private Hud hud; // optionnal

	private boolean debugViewPort = false;

	public void setDebugViewPort(boolean on) {
		this.debugViewPort = on;
	}

	public void toggleDebugViewPort() {
		this.debugViewPort = !this.debugViewPort;
	}

	public boolean isDebugViewPort() {
		return debugViewPort;
	}

	private boolean cullingEnabled = true;
	private boolean cullingInDebugMode = false;

	public void setCullingEnabled(boolean enabled) {
		this.cullingEnabled = enabled;
	}

	public boolean isCullingEnabled() {
		return cullingEnabled;
	}

	public void setCullingInDebugMode(boolean enabled) {
		this.cullingInDebugMode = enabled;
	}

	public boolean isCullingInDebugMode() {
		return cullingInDebugMode;
	}

	/**
	 * Couleur du rectangle du viewport
	 */
	private Color viewPortDebugColor = Colors.green;

	/**
	 * Taille de la marge minimale entre le bord du canvas et le viewport
	 */
	public static final int VIEWPORT_MARGIN_PX = 12;

	private IBackground background;

	public static interface IBackground {
		void paint(Graphics g);
	}

	/**
	 * Sets a background painted under the avatars, or null.
	 *
	 * @param background the background, or null for none
	 */
	public void setBackground(IBackground background) {
		this.background = background;
	}

	/**
	 * Attache un HUD à la vue, il sera dessiné par-dessus la scène.
	 *
	 * @param hud le HUD à dessiner, ou null pour aucun
	 */
	public void setHUD(Hud hud) {
		this.hud = hud;
	}

	public View(ViewPort vp) {
		this.byEntity = new HashMap<>();
		this.gathered = new HashSet<>();
		this.dyingAvatars = new HashSet<>();
		this.vp = Objects.requireNonNull(vp, "view port cannot be null");
		this.buckets = new ArrayList<>(Avatar.MAX_ZORDER + 1);
		this.entitylessAvatarsBucket = new ArrayList<>(Avatar.MAX_ZORDER + 1);
		this.visibleAvatarsBuckets = new ArrayList<>(Avatar.MAX_ZORDER + 1);
		for (int z = Avatar.MIN_ZORDER; z <= Avatar.MAX_ZORDER; z++) {
			this.buckets.add(new ArrayList<>());
			this.entitylessAvatarsBucket.add(new ArrayList<>());
			this.visibleAvatarsBuckets.add(new ArrayList<>());
		}
		this.avatarCount = 0;
	}

	/**
	 * @return the view port driving this view
	 */
	public ViewPort viewPort() {
		return vp;
	}

	/**
	 * Returns a flat snapshot of all avatars in z-order (low to high), insertion
	 * order within each layer. Beware this allocates a new list.
	 *
	 * @return a newly allocated, z-ordered list of the current avatars
	 */
	public List<Avatar> avatars() {
		List<Avatar> flat = new ArrayList<>(avatarCount);
		for (int z = 0; z < buckets.size(); z++) {
			flat.addAll(buckets.get(z));
		}
		return flat;
	}

	/**
	 * @return the number of avatars currently registered, across all layers
	 */
	public int avatarCount() {
		return avatarCount;
	}

	/**
	 * Registers an avatar for painting, filing it in the bucket matching its
	 * current z-order. Safe to call before or after {@code set_z_order}: a later
	 * z-order change re-files the avatar automatically (see set_z_order in Avatar).
	 * Appending keeps insertion order within the layer (stable). Re-adding an
	 * avatar already present is a no-op.
	 *
	 * @param avatar the avatar to add (must not be null)
	 */
	public void add(Avatar avatar) {
		Objects.requireNonNull(avatar, "avatar cannot be null");

		List<Avatar> bucket = buckets.get(clampZ(avatar.z_order()));
		if (bucket.contains(avatar)) {
			return;
		}
		bucket.add(avatar);
		avatarCount++;

		// Index by entity for grid-based render culling; entityless avatars go to a
		// side list that is always painted.
		Entity e = avatar.entity();
		if (e != null) {
			byEntity.put(e, avatar);
		} else {
			entitylessAvatarsBucket.get(clampZ(avatar.z_order())).add(avatar);
		}
	}

	/**
	 * Unregisters an avatar. Searches its current bucket first (the common case),
	 * then falls back to a full scan in case the z-order was mutated without going
	 * through onZOrderChanged method.
	 *
	 * @param avatar the avatar to remove
	 */
	public void remove(Avatar avatar) {
		if (avatar == null) {
			return;
		}

		dyingAvatars.remove(avatar);

		Entity e = avatar.entity();
		if (e != null) {
			byEntity.remove(e);
		} else {
			entitylessAvatarsBucket.get(clampZ(avatar.z_order())).remove(avatar);
		}

		List<Avatar> bucket = buckets.get(clampZ(avatar.z_order()));
		if (bucket.remove(avatar)) {
			avatarCount--;
			return;
		}

		// Fallback: avatar not where its z-order says it should be.
		for (int z = 0; z < buckets.size(); z++) {
			if (buckets.get(z).remove(avatar)) {
				avatarCount--;
				return;
			}
		}
	}

	/**
	 * Moves an avatar from its old layer to the one matching its new z-order.
	 * Called by set_z_order(int) method in avatar after the field has already been
	 * updated. The avatar is appended to the destination bucket, so it becomes the
	 * topmost within its new layer.
	 *
	 * @param avatar     the avatar whose z-order changed
	 * @param oldZ_order the z-order it had before the change
	 */
	public void onZOrderChanged(Avatar avatar, int oldZ_order) {
		List<Avatar> from = buckets.get(clampZ(oldZ_order));
		if (!from.remove(avatar)) {
			// Wasn't where expected; scan so we never duplicate it.
			for (int z = 0; z < buckets.size(); z++) {
				if (buckets.get(z).remove(avatar)) {
					break;
				}
			}
		}
		buckets.get(clampZ(avatar.z_order())).add(avatar);

		// Mirror the move in the entityless side list if applicable.
		if (avatar.entity() == null) {
			entitylessAvatarsBucket.get(clampZ(oldZ_order)).remove(avatar);
			entitylessAvatarsBucket.get(clampZ(avatar.z_order())).add(avatar);
		}
	}

	/**
	 * Advances every avatar's animation by the given time step, in z-order.
	 *
	 * @param delta_t elapsed time since the previous tick, in seconds
	 */
	public void updateAnimations(double delta_t) {
		for (int z = 0; z < buckets.size(); z++) {
			List<Avatar> bucket = buckets.get(z);
			for (int i = 0; i < bucket.size(); i++) {
				bucket.get(i).updateAnimation(delta_t);
			}
		}
	}

	/**
	 * Clamps a z-order to the valid bucket range. et_z_order(int) method in Avatar
	 * already validates, so this is a defensive guard for any avatar whose z-order
	 * was set through another path.
	 */
	private static int clampZ(int z) {
		if (z < Avatar.MIN_ZORDER)
			return Avatar.MIN_ZORDER;
		if (z > Avatar.MAX_ZORDER)
			return Avatar.MAX_ZORDER;
		return z;
	}

	/**
	 * @return left edge of the extent rendered this frame, in cm: the real view
	 *         port origin normally, or 0 in the whole-map debug overview
	 */
	private double renderOriginX() {
		return debugViewPort ? 0 : vp.originX();
	}

	/**
	 * @return top edge of the extent rendered this frame, in cm
	 */
	private double renderOriginY() {
		return debugViewPort ? 0 : vp.originY();
	}

	/**
	 * @return width of the extent rendered this frame, in cm
	 */
	private double renderWidth() {
		return debugViewPort ? Game.game().width_cm : vp.width_cm();
	}

	/**
	 * @return height of the extent rendered this frame, in cm
	 */
	private double renderHeight() {
		return debugViewPort ? Game.game().height_cm : vp.height_cm();
	}

	/**
	 * Usable drawing width in pixels: the canvas minus the margins on both sides.
	 */
	private int usableW() {
		return Math.max(1, canvas.getWidth() - 2 * VIEWPORT_MARGIN_PX);
	}

	/**
	 * Usable drawing height in pixels: the canvas minus the margins.
	 */
	private int usableH() {
		return Math.max(1, canvas.getHeight() - 2 * VIEWPORT_MARGIN_PX);
	}

	/**
	 * Facteur d'échelle uniforme (cm vers pixels de l'appareil) pour l'image
	 * actuelle. La même échelle est appliquée aux deux axes ; la zone d'affichage
	 * est donc encadrée par des bandes noires (sans jamais être étirée) : elle est
	 * ajustée à l'intérieur de la zone utilisable, et l'espace restant forme des
	 * bandes noires.
	 *
	 * @return l'échelle uniforme
	 */
	private double scale() {
		double fitX = usableW() / renderWidth();
		double fitY = usableH() / renderHeight();
		return Math.min(fitX, fitY);
	}

	/**
	 * @return horizontal pixel offset that centers the scaled extent in the canvas
	 */
	private double offsetX() {
		return (canvas.getWidth() - renderWidth() * scale()) / 2.0;
	}

	/**
	 * @return vertical pixel offset that centers the scaled extent in the canvas
	 */
	private double offsetY() {
		return (canvas.getHeight() - renderHeight() * scale()) / 2.0;
	}

	/**
	 * @return le rectangle à l'écran réellement occupé par la zone rendue, en
	 *         pixels {x, y, largeur, hauteur}. Le rendu est découpé selon ce
	 *         rectangle afin qu'aucun élément situé hors de la zone d'affichage ne
	 *         déborde sur les bandes noires.
	 */
	private int[] renderRectPx() {
		double s = scale();
		int x = (int) Math.round(offsetX());
		int y = (int) Math.round(offsetY());
		int w = (int) Math.round(renderWidth() * s);
		int h = (int) Math.round(renderHeight() * s);
		return new int[] { x, y, w, h };
	}

	/**
	 * Projette une coordonnée du monde (en cm) en pixels d'écran
	 * 
	 * @return PixelCoordinate (pixels d'écran)
	 */
	public PixelCoordinate worldToScreen(ISU.Coord worldPoint) {
		Objects.requireNonNull(worldPoint, "world point cannot be null");

		if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
			throw new RuntimeException("canvas.getWidth() <= 0 ou canvas.getHeight() <= 0");
		}

		Game game = Game.game();

		double originX = renderOriginX();
		double originY = renderOriginY();
		double rw = renderWidth();
		double rh = renderHeight();

		boolean subWindowX = rw < game.width_cm;
		boolean subWindowY = rh < game.height_cm;

		double wx = subWindowX ? game.isu.euclideanX(originX, worldPoint.x()) : worldPoint.x();
		double wy = subWindowY ? game.isu.euclideanY(originY, worldPoint.y()) : worldPoint.y();
		
		final double MARGIN = game.pixelPerCm*game.cmPerCell;

		// Outside the rendered extent: not visible on the canvas. Returning null
		// lets callers hide rather than draw in the black band.
		if (wx < originX-MARGIN || wx > originX + rw + MARGIN || wy < originY - MARGIN || wy > originY + rh + MARGIN) {
			return null;
		}

		double s = scale();
		int px = (int) Math.round(offsetX() + (wx - originX) * s);
		int py = (int) Math.round(offsetY() + (wy - originY) * s);

		return new PixelCoordinate(px, py);
	}

	public void paint(Canvas c, Graphics g) {
		Objects.requireNonNull(c, "canvas cannot be null");
		Objects.requireNonNull(g, "graphics cannot be null");

		this.canvas = c;

		if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
			throw new RuntimeException("canvas.getWidth() <= 0 || canvas.getHeight() <= 0");
		}

		// Source unique de vérité pour l'étendue rendue partagée avec

		// worldToScreen afin que les Labels se projettent exactement sur ce qui est
		// affiché.
		double renderOriginX = renderOriginX();
		double renderOriginY = renderOriginY();
		double renderWidth = renderWidth();
		double renderHeight = renderHeight();

		// Uniform scale (no stretching); centering offsets handle letterbox.
		double s = scale();
		double sx = s;
		double sy = s;

		// Clip to the rectangle actually occupied by the view port, not the whole
		// canvas. This is what enforces the camera: anything outside the view port
		// falls in the black bands and is masked,
		// instead of leaking because the canvas is larger than the view port.
		int[] rect = renderRectPx();

		Object savedTransform = g.getTransform();
		g.setClip(rect[0], rect[1], rect[2], rect[3]);

		// Render broad phase: pick the avatars near the view port once, reused by
		// every wrap pass below.
		gatherVisible();

		paintScene(g, savedTransform, renderOriginX, renderOriginY, sx, sy, 0, 0);

		// Toric wrap-around
		double worldW = GAME.width_cm;
		double worldH = GAME.height_cm;

		boolean wrapX = GAME.torusOnXaxis && renderOriginX + renderWidth > worldW;
		boolean wrapY = GAME.torusOnYaxis && renderOriginY + renderHeight > worldH;

		if (wrapX) {
			paintScene(g, savedTransform, renderOriginX, renderOriginY, sx, sy, worldW, 0);
		}
		if (wrapY) {
			paintScene(g, savedTransform, renderOriginX, renderOriginY, sx, sy, 0, worldH);
		}
		if (wrapX && wrapY) {
			paintScene(g, savedTransform, renderOriginX, renderOriginY, sx, sy, worldW, worldH);
		}

		// En mode debug, dessine un rectangle là où est le viewport réel.
		if (debugViewPort) {
			paintViewPortRect(g, savedTransform, renderOriginX, renderOriginY, sx, sy);
		}

		// Restore transform, remove the clip, then draw the frame and HUD over the
		// full canvas (including the black bands).
		g.setTransform(savedTransform);
		g.setClip(0, 0, canvas.getWidth(), canvas.getHeight());

		// In normal mode, outline the view port edge so the player can tell where
		// the game ends and the black letterbox bands begin.
		if (!debugViewPort) {
			Color previous = g.getColor();
			g.setColor(viewPortDebugColor);
			g.drawRect(rect[0], rect[1], rect[2], rect[3]);
			g.setColor(previous);
		}

		// hud is optionnal, on dessine uniquement si présent (càd non null)
		if (hud != null) {
			hud.draw(g, canvas.getWidth(), canvas.getHeight());
		}
	}

	/**
	 * Paint the scene
	 * 
	 * @param g
	 * @param baseTransform
	 * @param renderOriginX
	 * @param renderOriginY
	 * @param sx
	 * @param sy
	 * @param worldShiftX_cm
	 * @param worldShiftY_cm
	 */
	private void paintScene(Graphics g, Object baseTransform, double renderOriginX, double renderOriginY, double sx,
			double sy, double worldShiftX_cm, double worldShiftY_cm) {
		g.setTransform(baseTransform);

		double txPix = offsetX() - (renderOriginX - worldShiftX_cm) * sx;
		double tyPix = offsetY() - (renderOriginY - worldShiftY_cm) * sy;

		g.translate((int) Math.round(txPix), (int) Math.round(tyPix));
		g.scale(sx / Game.game().pixelPerCm, sy / GAME.pixelPerCm);

		if (background != null) {
			background.paint(g);
		}

		// Paint only the avatars gathered as visible this frame (filled once per
		// frame by gatherVisible(), shared across wrap passes), plus entityless
		// avatars which the grid cannot locate. Layer by layer, insertion order.
		for (int z = 0; z < buckets.size(); z++) {
			List<Avatar> vis = visibleAvatarsBuckets.get(z);
			for (int i = 0; i < vis.size(); i++) {
				vis.get(i).paint(g);
			}
			List<Avatar> none = entitylessAvatarsBucket.get(z);
			for (int i = 0; i < none.size(); i++) {
				none.get(i).paint(g);
			}
		}
	}

	/**
	 * Fills visibleAvatarsBuckets with the avatars whose entity occupies a grid
	 * cell overlapping the view port. This is the render broad phase: instead of
	 * painting every avatar in the world (and letting each one cull itself), we ask
	 * the grid which entities are near the camera and paint only those. Called once
	 * per frame, before the wrap passes, since the visible world region is the same
	 * for every pass (the toric shift is handled by the transform).
	 */
	private void gatherVisible() {
		for (int z = 0; z < visibleAvatarsBuckets.size(); z++) {
			visibleAvatarsBuckets.get(z).clear();
		}
		gathered.clear();

		// Détermine si on doit utiliser la grille pour filtrer les avatars
		boolean shouldCull = cullingEnabled;
		if (debugViewPort && !cullingInDebugMode) {
			shouldCull = false;
		}

		if (!shouldCull) {
			// Culling désactivé : on affiche tous les avatars ayant une entité
			for (Avatar a : byEntity.values()) {
				visibleAvatarsBuckets.get(clampZ(a.z_order())).add(a);
			}

			// On ajoute également les avatars dont l'entité est morte mais dont l'animation
			// continue
			for (Avatar a : dyingAvatars) {
				visibleAvatarsBuckets.get(clampZ(a.z_order())).add(a);
			}
		} else {
			// Culling activé : filtrage spatial via la grille
			Grid grid = GAME.grid;
			double cmPerCell = GAME.cmPerCell;

			// Always cull to the REAL view port, even in debug overview, so debug mode
			// shows only what the camera actually frames.
			double originX = vp.originX();
			double originY = vp.originY();
			double rw = vp.width_cm();
			double rh = vp.height_cm();

			// View port rectangle in cell indices. A margin of one cell avoids popping
			// for entities straddling the edge.
			int minX = (int) Math.floor(originX / cmPerCell) - NB_CELL_MARGIN_CULLING;
			int maxX = (int) Math.floor((originX + rw) / cmPerCell) + NB_CELL_MARGIN_CULLING;
			int minY = (int) Math.floor(originY / cmPerCell) - NB_CELL_MARGIN_CULLING;
			int maxY = (int) Math.floor((originY + rh) / cmPerCell) + NB_CELL_MARGIN_CULLING;

			int gw = grid.width();
			int gh = grid.height();

			for (int cx = minX; cx <= maxX; cx++) {
				// Wrap on the toric X axis so cells past the seam are still found.
				int wx = ((cx % gw) + gw) % gw;
				for (int cy = minY; cy <= maxY; cy++) {
					int wy = ((cy % gh) + gh) % gh;

					for (Entity e : grid.cellAt(wx, wy).entities()) {
						if (!gathered.add(e)) {
							continue; // entity spans several visible cells; gather once
						}
						Avatar a = byEntity.get(e);
						if (a != null) {
							visibleAvatarsBuckets.get(clampZ(a.z_order())).add(a);
						}
					}
				}
			}

			// Dead-but-still-animating avatars have left the grid, so add them here so
			// their death animation is painted to completion.
			for (Avatar a : dyingAvatars) {
				visibleAvatarsBuckets.get(clampZ(a.z_order())).add(a);
			}
		}
	}

	/**
	 * Draws the real view port as a rectangle, in the current (whole-map) render
	 * frame. Handles the toric seam by drawing a second copy shifted by one world
	 * period when the view port overflows the map edge.
	 *
	 * @param g             the graphics
	 * @param baseTransform the canvas transform to start from
	 * @param renderOriginX left edge of the rendered extent, in cm
	 * @param renderOriginY top edge of the rendered extent, in cm
	 * @param sx            cm -> pixel factor on x
	 * @param sy            cm -> pixel factor on y
	 */
	private void paintViewPortRect(Graphics g, Object baseTransform, double renderOriginX, double renderOriginY,
			double sx, double sy) {
		g.setTransform(baseTransform);

		Color previous = g.getColor();
		g.setColor(viewPortDebugColor);

		double vw = vp.width_cm();
		double vh = vp.height_cm() - 1; // -1 pour que le bas s'affiche correctement

		drawRectCm(g, renderOriginX, renderOriginY, vp.originX(), vp.originY(), vw, vh, sx, sy);

		if (GAME.torusOnXaxis && vp.originX() + vw > GAME.width_cm) {
			drawRectCm(g, renderOriginX, renderOriginY, vp.originX() - GAME.width_cm, vp.originY(), vw, vh, sx, sy);
		}
		if (GAME.torusOnYaxis && vp.originY() + vh > GAME.height_cm) {
			drawRectCm(g, renderOriginX, renderOriginY, vp.originX(), vp.originY() - GAME.height_cm, vw, vh, sx, sy);
		}

		g.setColor(previous);
	}

	/**
	 * Draws a rectangle given in world cm into the current render frame, in device
	 * pixels (the canvas transform is assumed reset to baseTransform).
	 * 
	 * 
	 * @param g             graphism object
	 * @param renderOriginX origin x
	 * @param renderOriginY origin y
	 * @param rectX_cm      x position of rect
	 * @param rectY_cm      y position of rect
	 * @param w_cm          width
	 * @param h_cm          height
	 * @param sx            screen x
	 * @param sy            screen y
	 */
	private void drawRectCm(Graphics g, double renderOriginX, double renderOriginY, double rectX_cm, double rectY_cm,
			double w_cm, double h_cm, double sx, double sy) {
		int x = (int) Math.round(offsetX() + (rectX_cm - renderOriginX) * sx);
		int y = (int) Math.round(offsetY() + (rectY_cm - renderOriginY) * sy);
		int w = (int) Math.round(w_cm * sx);
		int h = (int) Math.round(h_cm * sy);

		g.drawRect(x, y, w, h);
	}

	@Override
	public void tick(double delta_t) {
		List<Avatar> avatars = this.avatars();
		for (int i = 0; i < avatars.size(); i++) {
			Avatar a = avatars.get(i);
			a.updateAnimation(delta_t);

			// Supprimer les avatars dont l'entité est morte et dont l'animation de mort est
			// terminée (si il y en a une). L'entité lié à cette Avatar est retirée du
			// modèle séparément. Cela permet d'avoir des Avatars qui terminent leur
			// animation dont l'entité est déjà retirée du modèle.
			if (a.isReadyToRemove()) {
				dyingAvatars.remove(a);
				remove(a);
			} else if (a.dead()) {
				// Pour permettre de continuer à dessiner des avatars dont l'entity serait morte
				dyingAvatars.add(a);
			}
		}
	}
}