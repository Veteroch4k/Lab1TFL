package org.veteroch4k.laba1.Semantics;

public class ArgNode extends AstNode {
  public String type = "any";
  public String name = "<ошибка>";
  public String defaultValue = null;

  @Override
  public String printAst(String indent, boolean isLast) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append(isLast ? "└── " : "├── ").append("ArgNode").append("\n");
    String childIndent = indent + (isLast ? "    " : "│   ");

    boolean hasValue = (defaultValue != null);

    sb.append(childIndent).append("├── name: \"").append(name).append("\"\n");

    sb.append(childIndent).append(hasValue ? "├── " : "└── ").append("type: TypeNode").append("\n");
    sb.append(childIndent).append(hasValue ? "│   └── " : "    └── ").append("name: \"").append(type).append("\"\n");

    if (hasValue) {
      sb.append(childIndent).append("└── value: LiteralNode").append("\n");
      sb.append(childIndent).append("    └── value: ").append(defaultValue).append("\n");
    }

    return sb.toString();
  }
}