package org.veteroch4k.laba1.Semantics;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
  private final Map<String, String> symbols = new HashMap<>();

  public boolean checkDuplicate(String name) {
    return symbols.containsKey(name);
  }

  public void declare(String name, String type) {
    symbols.put(name, type);
  }

  public void clear() {
    symbols.clear();
  }
}