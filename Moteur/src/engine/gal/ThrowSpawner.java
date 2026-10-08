package engine.gal;

import engine.entity.Entity;
import engine.gal.arguments.Direction;

public interface ThrowSpawner {

	Entity spawnFrom(Entity source, Direction direction, double intensity);
}