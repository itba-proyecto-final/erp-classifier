package ar.edu.itba.experiments;

import ar.edu.itba.model.LightsGridPane;
import ar.edu.itba.senders.StimulusSender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GridLightsExperiment extends Application {

  private static final Color CURRENT = Color.MEDIUMSEAGREEN;
  private static final Color GOAL = Color.BLUE;
  private static final Color NORMAL = Color.GRAY;
  private static final Color CURRENT_AND_GOAL = Color.YELLOW;
  private static final Random RANDOM = ThreadLocalRandom.current();
  private final int gapSize = 5;
  private final int squareSize = 50;
  private final int rows = 1;
  private final int cols = 10;
  private final Button startButton = new Button("Start");
  private final int[] goalPosition = new int[]{0, 3};
  private LightsGridPane currentGrid;
  private int[] currentPosition = new int[]{0, 0};
  private List<int[]> movements = new ArrayList();

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    movements.add(new int[]{-1, 0});
    movements.add(new int[]{1, 0});
    movements.add(new int[]{0, 1});
    movements.add(new int[]{0, -1});

    startButton.setOnMouseClicked(event -> startExperiment());

    currentGrid = new LightsGridPane(rows, cols, currentPosition, goalPosition);

    final VBox vbox = new VBox(gapSize, startButton, currentGrid);
    vbox.setBackground(
        new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
    vbox.setAlignment(Pos.CENTER);
    vbox.setPadding(new Insets(gapSize, gapSize, gapSize, gapSize));

    final Scene scene = new Scene(vbox, cols * squareSize + gapSize * (cols + 1),
        30 + rows * squareSize + gapSize * (rows + 1));

    primaryStage.setTitle("Square Lights");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.setMaximized(true);
    primaryStage.setFullScreen(true);
    primaryStage.show();
  }

  private void startExperiment() {
    startButton.setDisable(true);

    final StimulusSender sender = new StimulusSender();
    try {
      sender.open("localhost", 15361);
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }

    final Timeline timeline = new Timeline();
    final KeyFrame keyFrame = new KeyFrame(Duration.seconds(3), e -> {
      final int prevDistance = distanceToGoal();
      final List<int[]> validMovements = movements.stream()
          .filter(currentGrid::isValidOffset)
          .collect(Collectors.toList());
      final int[] movement = validMovements.get(RANDOM.nextInt(validMovements.size()));
      moveLightWithOffset(movement);

      final int currentDistance = distanceToGoal();
      if (distanceToGoal() == 0) {
        timeline.stop();
        try {
          sender.close();
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
      final long distanceDifference = prevDistance - currentDistance;
      try {
        System.out.println(String.format("%d sent", distanceDifference));
        sender.send(distanceDifference, 0L);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    });
    timeline.getKeyFrames().add(keyFrame);
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private int distanceToGoal() {
    return Math.abs(currentPosition[0] - goalPosition[0]) + Math
        .abs(currentPosition[1] - goalPosition[1]);
  }

  private void moveLightWithOffset(final int[] offset) {
    currentPosition[0] += offset[0];
    currentPosition[1] += offset[1];
    currentGrid.moveLightWithOffset(offset);
  }
}
