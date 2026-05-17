package org.veteroch4k.laba1;

import java.util.ArrayList;
import java.util.List;

public class UINode {
  public String label;
  public List<UINode> children = new ArrayList<>();

  public UINode(String label) {
    this.label = label;
  }
  public void addChild(UINode child) {
    children.add(child);
  }
}