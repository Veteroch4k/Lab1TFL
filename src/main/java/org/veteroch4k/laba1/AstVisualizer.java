package org.veteroch4k.laba1;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class AstVisualizer {

  private static final double NODE_W = 140;
  private static final double NODE_H = 60;
  private static final double V_GAP = 100;
  private static final double H_GAP = 160;

  public static void showWindow(Object rootNodeObj) {
    UINode root = (UINode) rootNodeObj;

    Stage stage = new Stage();
    stage.setTitle("Визуализация AST");
    Pane pane = new Pane();
    pane.setStyle("-fx-background-color: #f4f4f4;");

    double totalWidth = getSubtreeWidth(root);
    drawNode(pane, root, Math.max(totalWidth / 2 + 50, 400), 50);

    ScrollPane scroll = new ScrollPane(pane);
    scroll.setPannable(true);
    scroll.setFitToWidth(true);

    Scene scene = new Scene(scroll, 900, 600);
    stage.setScene(scene);
    stage.show();
  }

  private static double getSubtreeWidth(UINode node) {
    if (node.children.isEmpty()) return H_GAP;
    double w = 0;
    for (UINode c : node.children) {
      w += getSubtreeWidth(c);
    }
    return w;
  }

  private static void drawNode(Pane pane, UINode node, double x, double y) {
    double currentX = x - getSubtreeWidth(node) / 2;

    for (UINode child : node.children) {
      double childWidth = getSubtreeWidth(child);
      double childCenter = currentX + childWidth / 2;

      Line line = new Line(x, y + NODE_H, childCenter, y + V_GAP);
      line.setStroke(Color.GRAY);
      line.setStrokeWidth(2);
      pane.getChildren().add(line);

      drawNode(pane, child, childCenter, y + V_GAP);
      currentX += childWidth;
    }

    StackPane sp = new StackPane();
    Rectangle rect = new Rectangle(NODE_W, NODE_H);
    rect.setFill(Color.web("#e3f2fd"));
    rect.setStroke(Color.web("#1565c0"));
    rect.setStrokeWidth(2);
    rect.setArcWidth(10);
    rect.setArcHeight(10);

    Text text = new Text(node.label);
    text.setStyle("-fx-font-family: Arial; -fx-font-size: 13px;");
    text.setTextAlignment(TextAlignment.CENTER);

    sp.getChildren().addAll(rect, text);
    sp.setLayoutX(x - NODE_W / 2);
    sp.setLayoutY(y);

    pane.getChildren().add(sp);
  }
}