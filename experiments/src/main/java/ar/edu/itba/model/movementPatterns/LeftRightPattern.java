package ar.edu.itba.model.movementPatterns;

import java.util.Random;

public class LeftRightPattern implements MovementPattern {

  private int direction;

  public LeftRightPattern() {
    final Random r = new Random();
    direction = r.nextBoolean() ? 1 : -1;
  }

  @Override
  public int[] getOffset(final int[] position, final int rows, final int cols) {
    if (cols == 1) {
      return new int[]{0, 0};
    }

    if (position[1] + direction < 0 || position[1] + direction > cols - 1) {
      direction *= -1;
    }

    return new int[]{0, direction};
  }
}
