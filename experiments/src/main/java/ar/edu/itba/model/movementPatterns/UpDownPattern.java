package ar.edu.itba.model.movementPatterns;

import java.util.Random;

public class UpDownPattern implements MovementPattern {

  private int direction;

  public UpDownPattern() {
    final Random r = new Random();
    direction = r.nextBoolean() ? 1 : -1;
  }

  @Override
  public int[] getOffset(final int[] position, final int rows, final int cols) {
    if (rows == 1) {
      return new int[]{0, 0};
    }

    if (position[0] + direction < 0 || position[0] + direction > rows - 1) {
      direction *= -1;
    }

    return new int[]{direction, 0};
  }
}
