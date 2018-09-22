package ar.edu.itba.model;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class StartScreen extends BorderPane {

  private final Button startButton;

  public StartScreen() {
    super();

    this.startButton = new Button("Start");
    this.startButton.setFont(Font.font(24));
    this.startButton.setMinWidth(80);
    this.startButton.setMinHeight(60);
    this.setCenter(startButton);
  }

  public void setOnStart(final Runnable runnable) {
    startButton.setOnMouseClicked(event -> runnable.run());
  }
}
