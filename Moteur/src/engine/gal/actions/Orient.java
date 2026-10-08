package engine.gal.actions;

import engine.entity.Entity;
import engine.gal.arguments.Direction;

public class Orient extends GALAction {

    private final Direction direction;

    public Orient(Direction direction) {
        super(1.0);
        this.direction = direction;
    }

    @Override
    public boolean exec(Entity e) {
        if (e == null) return false;

        double targetAngle;

        if (direction == Direction.F) {
            targetAngle = e.orientation();
        } else if (direction == Direction.B) {
            targetAngle = normalizeAngle(e.orientation() + 180.0);
        } else if (direction == Direction.L) {
            targetAngle = normalizeAngle(e.orientation() - 22.5);
        } else if (direction == Direction.R) {
            targetAngle = normalizeAngle(e.orientation() + 22.5);
        } else if (direction == Direction.H) {
            targetAngle = e.orientation();
        } else {
            targetAngle = direction.toAngle();
        }

        e.forceOrientation(targetAngle);
        return true;
    }

    private double normalizeAngle(double angle) {
        double normalized = angle % 360.0;
        if (normalized < 0.0) {
            normalized += 360.0;
        }
        return normalized;
    }
}