package ar.edu.itba.experiments;

import static java.lang.Math.abs;

import ar.edu.itba.model.CounterPane;
import ar.edu.itba.model.LightsGridPane;
import ar.edu.itba.model.StartScreen;
import ar.edu.itba.senders.StimulusSender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GridLightsExperiment extends Application {

  private static final Random RANDOM = ThreadLocalRandom.current();
  private static final List<int[]> movements = new ArrayList<>(4);
  private static final int MAX_ITERATION = 10;
  private static final int ROWS = 1;
  private static final int COLS = 6;
  private static final Duration STEP_DURATION = Duration.seconds(3);

  static {
    movements.add(new int[]{-1, 0});
    movements.add(new int[]{1, 0});
    movements.add(new int[]{0, 1});
    movements.add(new int[]{0, -1});
  }

  private int iteration = 1;
  private int[] currentPosition;

  private BorderPane pane;
  private CounterPane counterPane;
  private LightsGridPane currentGrid;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    final StartScreen startScreen = new StartScreen();
    startScreen.setOnStart(() -> {
      pane.setCenter(counterPane);
      counterPane.startTimer();
    });

    counterPane = new CounterPane();
    counterPane.setOnTimerFinished(this::startExperiment);

    pane = new BorderPane(startScreen);
    pane.setBackground(
        new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

    primaryStage.setTitle("Square Lights");
    primaryStage.setScene(new Scene(pane));
    primaryStage.setResizable(false);
    primaryStage.setMaximized(true);
    setMaxSize(primaryStage);
    primaryStage.setFullScreen(true);
    primaryStage.show();
  }

  private static void setMaxSize(final Stage primaryStage) {
    final Screen screen = Screen.getPrimary();
    final Rectangle2D bounds = screen.getVisualBounds();

    primaryStage.setX(bounds.getMinX());
    primaryStage.setY(bounds.getMinY());
    primaryStage.setWidth(bounds.getWidth());
    primaryStage.setHeight(bounds.getHeight());
  }

  private void startExperiment() {
    final int[] startingPosition = newStartingPosition();
    currentPosition = Arrays.copyOf(startingPosition, startingPosition.length);
    final int[] goalPosition = newGoalPosition();
    currentGrid = new LightsGridPane(ROWS, COLS, currentPosition, goalPosition);
    pane.setCenter(currentGrid);

    final StimulusSender sender = new StimulusSender();
    try {
      sender.open("10.17.2.185", 15361);
      sender.send(3L, 0L);
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }

    final Timeline timeline = new Timeline();
    final KeyFrame keyFrame = new KeyFrame(STEP_DURATION, e -> {
      final int prevDistance = distanceToGoal(goalPosition);
      final List<int[]> validMovements = movements.stream()
          .filter(currentGrid::isValidOffset)
          .collect(Collectors.toList());
      final int[] movement = validMovements.get(RANDOM.nextInt(validMovements.size()));
      moveLightWithOffset(movement);

      if (distanceToGoal(goalPosition) == 0) {
        timeline.stop();
        try {
          sender.send(4L, 0L);
          sender.close();
        } catch (Exception e1) {
          e1.printStackTrace();
        }
        if (iteration < MAX_ITERATION) {
          iteration++;
          final Timeline tl = new Timeline(new KeyFrame(STEP_DURATION, oe -> {
            pane.setCenter(counterPane);
            counterPane.startTimer();
          }));
          tl.play();
        }
      } else {
        final int currentDistance = distanceToGoal(goalPosition);
        final long distanceDifference = prevDistance - currentDistance;
        try {
          sender.send(distanceDifference > 0 ? 1 : 2, 0L);
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      }
    });
    timeline.getKeyFrames().add(keyFrame);
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  private void moveLightWithOffset(final int[] offset) {
    currentPosition[0] += offset[0];
    currentPosition[1] += offset[1];
    currentGrid.moveLightWithOffset(offset);
  }

  private int[] newStartingPosition() {
    return new int[]{RANDOM.nextInt(ROWS), RANDOM.nextInt(COLS)};
  }

  private int[] newGoalPosition() {
    int[] goalPosition;

    do {
      goalPosition = new int[]{RANDOM.nextInt(ROWS), RANDOM.nextInt(COLS)};
    } while (distanceToGoal(goalPosition) <= 1);

    return goalPosition;
  }

  private int distanceToGoal(final int[] goalPosition) {
    return abs(currentPosition[0] - goalPosition[0]) + abs(currentPosition[1] - goalPosition[1]);
  }
}
