package ar.edu.itba;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Hello world!
 *
 */
public class App extends Application {
    public static void main( String[] args ){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println( "Hello World!" );
        GridPane grid = new GridPane();
        Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED};
        int rowNum = 5;
        int colNum = 4;
        Random rand = new Random();
        for (int row = 0; row < rowNum; row++) {
            for (int col = 0; col < colNum; col++) {
                int n = rand.nextInt(4);
                Rectangle rec = new Rectangle();
                rec.setWidth(20);
                rec.setHeight(20);
                rec.setFill(colors[n]);
                GridPane.setRowIndex(rec, row);
                GridPane.setColumnIndex(rec, col);
                grid.getChildren().addAll(rec);
            }
        }
        Scene scene = new Scene(grid, colNum * 20, rowNum * 20);

        primaryStage.setTitle("Grid");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
