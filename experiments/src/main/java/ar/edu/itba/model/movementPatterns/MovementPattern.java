package ar.edu.itba.model.movementPatterns;

public interface MovementPattern {

  int[] getOffset(final int[] position, final int rows, final int cols);

}
