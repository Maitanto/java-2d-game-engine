package engine.gal;

import engine.gal.arguments.Direction;

public interface iAllGALActions {

	// TODO: à compléter avec les autres actions GAL plus tard

	// MOVE

	/**
	 * @apiNote asks for moving
	 *          <UL>
	 *          <LI>at celerity = intensity * maximal celerity</LI>
	 *          <LI>during duration (in ms)</LI>
	 *          <LI>in the given direction</LI>
	 *          </UL>
	 * @param direction
	 * @param intensity   : in [0,1] ≃ % but a negative intensity means do not
	 *                    change celerity
	 * @param duration_ms
	 */
	boolean startMoving(Direction direction, double intensity, double duration_ms);

	// TURN

	/**
	 * @apiNote asks for turning.
	 * @param angle_deg = the desired angle
	 * @param intensity = in [0,1] ≃ % of the maximal angular speed
	 */
	boolean startTurning(double angle_deg, double intensity);

	// THROW

	/**
	 * @apiNote asks for throwing / firing in the given direction.
	 * @param direction direction of the throw
	 * @param intensity in [0,1]
	 */
	boolean startThrowing(Direction direction, double intensity);
	
	boolean startDashing(Direction direction, double intensity);

}