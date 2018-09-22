package ar.edu.itba.experiments;

import ar.edu.itba.model.LightsGridPane;
import ar.edu.itba.model.StartScreen;
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
  private static final List<int[]> movements = new ArrayList(4);
  private static final int ROWS = 1;
  private static final int COLS = 10;
  private static final int[] GOAL_POSITION = new int[]{0, 3};

  static {
    movements.add(new int[]{-1, 0});
    movements.add(new int[]{1, 0});
    movements.add(new int[]{0, 1});
    movements.add(new int[]{0, -1});
  }
  private int[] currentPosition = new int[]{0, 0};

  private BorderPane pane;
  private LightsGridPane currentGrid;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    final StartScreen startScreen = new StartScreen();
    startScreen.setOnStart(this::startExperiment);

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
    currentGrid = new LightsGridPane(ROWS, COLS, currentPosition, GOAL_POSITION);
    pane.setCenter(currentGrid);

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

  private void moveLightWithOffset(final int[] offset) {
    currentPosition[0] += offset[0];
    currentPosition[1] += offset[1];
    currentGrid.moveLightWithOffset(offset);
  }

  private int distanceToGoal() {
    return Math.abs(currentPosition[0] - GOAL_POSITION[0]) + Math
        .abs(currentPosition[1] - GOAL_POSITION[1]);
  }
}
