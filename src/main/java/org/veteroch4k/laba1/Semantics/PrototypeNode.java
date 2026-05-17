package org.veteroch4k.laba1.Semantics;

import java.util.ArrayList;
import java.util.List;

public class PrototypeNode extends AstNode {
  public String functionName = "<ошибка>";
  public List<ArgNode> arguments = new ArrayList<>();
  public String returnType = "void";

  @Override
  public String printAst(String indent, boolean isLast) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent).append(isLast ? "└── " : "├── ").append("PrototypeNode").append("\n");
    String childIndent = indent + (isLast ? "    " : "│   ");

    boolean hasArgs = !arguments.isEmpty();
    boolean hasRet = (returnType != null);

    sb.append(childIndent).append(hasArgs || hasRet ? "├── " : "└── ").append("name: \"").append(functionName).append("\"\n");

    if (hasArgs) {
      sb.append(childIndent).append(hasRet ? "├── " : "└── ").append("arguments").append("\n");
      String argIndent = childIndent + (hasRet ? "│   " : "    ");
      for (int i = 0; i < arguments.size(); i++) {
        sb.append(arguments.get(i).printAst(argIndent, i == arguments.size() - 1));
      }
    }

    if (hasRet) {
      sb.append(childIndent).append("└── returnType: TypeNode").append("\n");
      sb.append(childIndent).append("    └── name: \"").append(returnType).append("\"\n");
    }
    return sb.toString();
  }
}