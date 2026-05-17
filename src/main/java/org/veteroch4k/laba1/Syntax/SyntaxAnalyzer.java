package org.veteroch4k.laba1.Syntax;

import java.util.ArrayList;
import java.util.List;
import org.veteroch4k.laba1.Lexicon.Token;
import org.veteroch4k.laba1.Lexicon.TokenType;
import org.veteroch4k.laba1.Semantics.ArgNode;
import org.veteroch4k.laba1.Semantics.PrototypeNode;
import org.veteroch4k.laba1.Semantics.SymbolTable;

public class SyntaxAnalyzer {

  private final List<Token> tokens;
  private int pos;
  private final List<ErrorItem> errors;

  private final SymbolTable symbolTable;
  private final List<String> declaredFunctions;
  private final List<PrototypeNode> rootNodes;

  public SyntaxAnalyzer(List<Token> tokens) {
    this.tokens = tokens;
    this.pos = 0;
    this.errors = new ArrayList<>();
    this.symbolTable = new SymbolTable();
    this.declaredFunctions = new ArrayList<>();
    this.rootNodes = new ArrayList<>();
  }

  public List<ErrorItem> parse() {
    if (tokens.isEmpty() || tokens.getFirst().type() == TokenType.EOF) {
      return errors;
    }

    while (peek().type() != TokenType.EOF) {
      symbolTable.clear();
      PrototypeNode node = parsePrototype();
      if (node != null) {
        rootNodes.add(node);
      }
    }
    return errors;
  }

  public String getAstTree() {
    if (!rootNodes.isEmpty() && errors.isEmpty()) {
      if (rootNodes.size() == 1) {
        return rootNodes.getFirst().printAst("", true);
      }

      StringBuilder sb = new StringBuilder();
      sb.append("ProgramNode\n");
      for (int i = 0; i < rootNodes.size(); i++) {
        sb.append(rootNodes.get(i).printAst("├── ", i == rootNodes.size() - 1));
      }
      return sb.toString();
    }
    return "AST Дерево не построено из-за наличия синтаксических или семантических ошибок.";
  }

  private Token peek() {
    if (pos >= tokens.size()) {
      return tokens.getLast();
    }
    return tokens.get(pos);
  }

  private Token advance() {
    Token current = peek();
    if(pos < tokens.size() - 1) pos++;
    return current;
  }

  private boolean match(TokenType expected) {
    if (peek().type() == expected) {
      advance();
      return true;
    }
    return false;
  }

  private void require(TokenType expected, String errorMessage, TokenType... additionalSync) {
    if (peek().type() == expected) {
      advance();
      return;
    }

    Token badToken = peek();
    errors.add(new ErrorItem("Новый документ", badToken.line(), badToken.column(),
        "Синтаксическая ошибка: " + errorMessage + ". Встречено: '" + badToken.value() + "'", badToken.value()));

    while (peek().type() != expected &&
        peek().type() != TokenType.SEMICOLON &&
        peek().type() != TokenType.EOF &&
        peek().type() != TokenType.R_PAREN &&
        peek().type() != TokenType.L_PAREN) {

      boolean isSync = false;
      for (TokenType t : additionalSync) {
        if (peek().type() == t) {
          isSync = true;
          break;
        }
      }
      if (isSync) break;

      advance();
    }

    if (peek().type() == expected) {
      advance();
    }
  }

  private PrototypeNode parsePrototype() {
    PrototypeNode node = new PrototypeNode();

    require(TokenType.KW_FUNCTION, "Ожидалось ключевое слово 'function'", TokenType.IDENTIFIER);

    Token nameToken = peek();
    if (nameToken.type() == TokenType.IDENTIFIER) {
      node.functionName = nameToken.value();

      if (declaredFunctions.contains(node.functionName)) {
        errors.add(new ErrorItem("Новый документ", nameToken.line(), nameToken.column(),
            "Семантическая ошибка: Функция '" + node.functionName + "' уже объявлена ранее", nameToken.value()));
      } else {
        declaredFunctions.add(node.functionName);
      }
    }
    require(TokenType.IDENTIFIER, "Ожидалось имя функции");

    require(TokenType.L_PAREN, "Ожидалась открывающая скобка '('");

    node.arguments = parseArgs();

    require(TokenType.R_PAREN, "Ожидалась закрывающая скобка ')'");

    node.returnType = parseRetType();

    require(TokenType.SEMICOLON, "Ожидалась точка с запятой ';' в конце прототипа");

    return node;
  }

  private List<ArgNode> parseArgs() {
    List<ArgNode> args = new ArrayList<>();
    if (peek().type() != TokenType.R_PAREN && peek().type() != TokenType.EOF) {
      args.add(parseArg());
      while (peek().type() == TokenType.COMMA) {
        advance();
        args.add(parseArg());
      }
    }
    return args;
  }

  private ArgNode parseArg() {
    ArgNode arg = new ArgNode();
    arg.type = parseType();

    Token varToken = peek();
    if (varToken.type() == TokenType.VARIABLE) {
      arg.name = varToken.value();

      if (symbolTable.checkDuplicate(arg.name)) {
        errors.add(new ErrorItem("Новый документ", varToken.line(), varToken.column(),
            "Семантическая ошибка: Переменная '" + arg.name + "' уже объявлена ранее", varToken.value()));
      } else {
        symbolTable.declare(arg.name, arg.type);
      }
    }

    require(TokenType.VARIABLE, "Ожидалось имя переменной", TokenType.COMMA, TokenType.ASSIGN);

    arg.defaultValue = parseDefaultVal(arg.type);
    return arg;
  }

  private String parseType() {
    boolean isNullable = match(TokenType.QUESTION);
    Token typeToken = peek();
    requireTypeToken();
    return (isNullable ? "?" : "") + typeToken.value();
  }

  private String parseDefaultVal(String expectedType) {
    if (match(TokenType.ASSIGN)) {
      Token t = peek();
      if (t.type() == TokenType.NUMBER || t.type() == TokenType.FLOAT || t.type() == TokenType.STRING) {

        boolean typeError = false;
        String cleanType = expectedType != null ? expectedType.replace("?", "") : "";

        if (cleanType.equals("int") && t.type() != TokenType.NUMBER) typeError = true;
        if (cleanType.equals("float") && t.type() != TokenType.FLOAT && t.type() != TokenType.NUMBER) typeError = true;
        if (cleanType.equals("string") && t.type() != TokenType.STRING) typeError = true;

        if (typeError) {
          errors.add(new ErrorItem("Новый документ", t.line(), t.column(),
              "Семантическая ошибка: Значение по умолчанию '" + t.value() + "' не совместимо с типом '" + cleanType + "'", t.value()));
        }

        advance();
        return t.value();
      } else {
        errors.add(new ErrorItem("Новый документ", t.line(), t.column(),
            "Синтаксическая ошибка: Ожидалось значение (число или строка) после '='", t.value()));
        advance();
      }
    }
    return null;
  }

  private String parseRetType() {
    if (match(TokenType.COLON)) {
      Token t = peek();
      requireTypeToken();
      return t.value();
    }
    return null;
  }

  private boolean isTypeToken(TokenType type) {
    return type == TokenType.TYPE_INT || type == TokenType.TYPE_STRING || type == TokenType.TYPE_FLOAT || type == TokenType.IDENTIFIER;
  }

  private void requireTypeToken() {
    if (isTypeToken(peek().type())) {
      advance();
    } else {
      Token bad = peek();
      errors.add(new ErrorItem("Новый документ", bad.line(), bad.column(),
          "Синтаксическая ошибка: Ожидался тип данных (int, string и т.д.)", bad.value()));
    }
  }

}