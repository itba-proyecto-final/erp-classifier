package ar.edu.itba.experiments;

import ar.edu.itba.senders.StimulusSender;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SquareLights extends Application {

    private static final Color CURRENT = Color.MEDIUMSEAGREEN;
    private static final Color GOAL = Color.BLUE;
    private static final Color NORMAL = Color.GRAY;
    private static final Color CURRENT_AND_GOAL = Color.YELLOW;

    private final int squareSize = 50;
    private final int rows = 1;
    private final int cols = 10;

    private final Button startButton = new Button("Start");
    private final GridPane grid = new GridPane();
    private final Rectangle[][] matrix = new Rectangle[rows][cols];
    private final int gapSize = 5;

    private final int[] goalPosition = new int[]{0, 9};
    private int[] currentPosition = new int[]{0, 0};
    private List<int[]> movements = new ArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox vbox = new VBox();
        vbox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(gapSize);
        vbox.setPadding(new Insets(gapSize, gapSize, gapSize, gapSize));

        startButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                startExperiment();
            }
        });

        vbox.getChildren().add(startButton);

        grid.setHgap(gapSize);
        grid.setVgap(gapSize);
        grid.setLayoutX(gapSize);
        grid.setLayoutY(gapSize);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle rec = new Rectangle(squareSize, squareSize);
                rec.setFill(calculateSquareColor(row, col));
                grid.add(rec, col, row);
                matrix[row][col] = rec;
            }
        }

        vbox.getChildren().add(grid);

        Scene scene = new Scene(vbox, cols * squareSize + gapSize * (cols + 1),
                30 + rows * squareSize + gapSize * (rows + 1));

        primaryStage.setTitle("Square Lights");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        movements.add(new int[]{-1, 0});
        movements.add(new int[]{1, 0});
        movements.add(new int[]{0, 1});
        movements.add(new int[]{0, -1});
    }

    private void startExperiment() {
        startButton.setDisable(true);
        Random r = new Random();

        StimulusSender sender = new StimulusSender();

        try {
            sender.open("localhost", 15361);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Timeline timeline = new Timeline();
        final KeyFrame keyFrame = new KeyFrame(Duration.seconds(3), e -> {
            final int prevDistance = distanceToGoal();
            final Rectangle currentRec = matrix[currentPosition[0]][currentPosition[1]];
            currentRec.setFill(NORMAL);

            List<int[]> validMovements = movements.stream().filter(this::isValidMovement).collect(Collectors.toList());
            int[] movement = validMovements.get(r.nextInt(validMovements.size()));
            currentPosition[0] += movement[0];
            currentPosition[1] += movement[1];

            final Rectangle nextRec = matrix[currentPosition[0]][currentPosition[1]];
            nextRec.setFill(calculateSquareColor(currentPosition[0], currentPosition[1]));

            final int currentDistance = distanceToGoal();
            if (distanceToGoal() == 0) {
                timeline.stop();
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

    private final Color calculateSquareColor(final int row, final int col) {
        if (currentPosition[0] == row && currentPosition[1] == col) {
            if (goalPosition[0] == row && goalPosition[1] == col) {
                return CURRENT_AND_GOAL;
            }
            return CURRENT;
        }

        if (goalPosition[0] == row && goalPosition[1] == col) {
            return GOAL;
        }

        return NORMAL;
    }

    private boolean isValidMovement(int[] movement) {
        int newRow = currentPosition[0] + movement[0];
        int newCol = currentPosition[1] + movement[1];
        return newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols;
    }

    private int distanceToGoal() {
        return Math.abs(currentPosition[0] - goalPosition[0]) + Math.abs(currentPosition[1] - goalPosition[1]);
    }
}
