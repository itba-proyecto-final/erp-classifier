package ar.edu.itba.model;

import java.util.Arrays;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

public class LightsGridPane extends GridPane {

  private static final Color CURRENT_COLOR = Color.YELLOW;
  private static final Color GOAL_COLOR = Color.MEDIUMSEAGREEN;
  private static final Color EMPTY_COLOR = Color.DARKGRAY;
  private static final Color WIN_COLOR = Color.GREENYELLOW;

  private static final int GAP_SIZE = 12;
  private static final int SHAPE_SIZE = 40;

  private final int rows;
  private final int cols;
  private final Shape[][] shapes;

  private final int[] goalPosition;
  private final int[] currentPosition;

  public LightsGridPane(final int rows, final int cols, final int[] startingPosition,
      final int[] goalPosition) {
    this.rows = rows;
    this.cols = cols;
    this.shapes = new Shape[rows][cols];
    this.goalPosition = Arrays.copyOf(goalPosition, goalPosition.length);
    this.currentPosition = Arrays.copyOf(startingPosition, startingPosition.length);

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        final Shape rec = new Circle(SHAPE_SIZE, EMPTY_COLOR);
        this.shapes[row][col] = rec;
        this.add(rec, col, row);
      }
    }

    this.shapes[currentPosition[0]][currentPosition[1]].setFill(CURRENT_COLOR);
    this.shapes[goalPosition[0]][goalPosition[1]].setFill(GOAL_COLOR);

    this.setHgap(GAP_SIZE);
    this.setVgap(GAP_SIZE);
    this.setLayoutX(GAP_SIZE);
    this.setLayoutY(GAP_SIZE);
    this.setAlignment(Pos.CENTER);
  }

  public void moveLightWithOffset(final int[] offset) {
    shapes[currentPosition[0]][currentPosition[1]].setFill(EMPTY_COLOR);
    currentPosition[0] += offset[0];
    currentPosition[1] += offset[1];
    if (Arrays.equals(currentPosition, goalPosition)) {
      shapes[currentPosition[0]][currentPosition[1]].setFill(WIN_COLOR);
    } else {
      shapes[currentPosition[0]][currentPosition[1]].setFill(CURRENT_COLOR);
    }
  }

  public void moveLightWithOffset(final int rowOffset, final int colOffset) {
    shapes[currentPosition[0]][currentPosition[1]].setFill(EMPTY_COLOR);
    currentPosition[0] += rowOffset;
    currentPosition[1] += colOffset;
    shapes[currentPosition[0]][currentPosition[1]].setFill(CURRENT_COLOR);
  }

  public boolean isValidOffset(int[] movement) {
    int newRow = currentPosition[0] + movement[0];
    int newCol = currentPosition[1] + movement[1];
    return newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols;
  }
}
