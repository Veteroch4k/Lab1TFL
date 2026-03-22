package org.veteroch4k.laba1;

import java.util.ArrayList;
import java.util.List;

public class SyntaxAnalyzer {

  private final List<Token> tokens;
  private int pos;
  private final List<ErrorItem> errors;

  public SyntaxAnalyzer(List<Token> tokens) {
    this.tokens = tokens;
    this.pos = 0;
    this.errors = new ArrayList<>();
  }


  public List<ErrorItem> parse() {
    if(!tokens.isEmpty()) {
      parsePrototype();
    }
    return errors;
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

  private void require(TokenType expected, String errorMessage) {
    if (peek().type() == expected) {
      advance();
    } else {
      Token badToken = peek();

      errors.add(new ErrorItem("Новый документ", badToken.line(), badToken.column(),
          "Синтаксическая ошибка: " + errorMessage + ". Встречено: '" + badToken.value() + "'", badToken.value()));

      while (peek().type() != expected && peek().type() != TokenType.SEMICOLON && peek().type() != TokenType.EOF) {
        advance();
      }
      if (peek().type() == expected) {
        advance();
      }
    }
  }

  private void parsePrototype() {

    require(TokenType.KW_FUNCTION, "Ожидалось ключевое слово 'function'");
    require(TokenType.IDENTIFIER, "Ожидалось имя функции");
    require(TokenType.L_PAREN, "Ожидалась открывающая скобка '('");

    parseArgs();

    require(TokenType.R_PAREN, "Ожидалась закрывающая скобка ')'");

    parseRetType();

    require(TokenType.SEMICOLON, "Ожидалась точка с запятой ';' в конце прототипа");
  }


  private void parseArgs() {
    if (peek().type() != TokenType.R_PAREN && peek().type() != TokenType.EOF) {
      parseArg();
      while (peek().type() == TokenType.COMMA) {
        advance();
        parseArg();
      }
    }
  }

  private void parseArg() {
    parseType();
    require(TokenType.VARIABLE, "Ожидалось имя переменной (начинается с '$')");
    parseDefaultVal();
  }

  private void parseType() {
    if (match(TokenType.QUESTION)) {
      requireTypeToken();
    } else {
      if (isTypeToken(peek().type())) {
        advance();
      }
    }
  }

  private void parseDefaultVal() {
    if (match(TokenType.ASSIGN)) {
      Token t = peek();
      if (t.type() == TokenType.NUMBER || t.type() == TokenType.FLOAT || t.type() == TokenType.STRING) {
        advance();
      } else {
        errors.add(new ErrorItem("Новый документ", t.line(), t.column(),
            "Синтаксическая ошибка: Ожидалось значение (число или строка) после '='", t.value()));
        advance();
      }
    }
  }

  private void parseRetType() {
    if (match(TokenType.COLON)) {
      requireTypeToken();
    }
  }

  private boolean isTypeToken(TokenType type) {
    return type == TokenType.TYPE_INT || type == TokenType.TYPE_STRING || type == TokenType.IDENTIFIER;
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
