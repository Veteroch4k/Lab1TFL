package org.veteroch4k.laba1.Lexicon;

import java.util.ArrayList;
import java.util.List;

public class LexicalAnalyzer {
  private String text;
  private int pos;
  private int line;
  private int column;

  public List<Token> analyze(String text) {
    this.text = text;
    this.pos = 0;
    this.line = 1;
    this.column = 1;
    List<Token> tokens = new ArrayList<>();

    while (pos < text.length()) {
      char currentChar = text.charAt(pos);

      if (Character.isWhitespace(currentChar)) {
        handleWhitespace(currentChar);
        continue;
      }

      if (Character.isDigit(currentChar)) {
        tokens.add(parseNumber());
        continue;
      }

      if (currentChar == '$') {
        tokens.add(parseVariable());
        continue;
      }

      if (Character.isLetter(currentChar) || currentChar == '_') {
        tokens.add(parseWord());
        continue;
      }

      if (currentChar == '"' || currentChar == '\'') {
        tokens.add(parseString(currentChar));
        continue;
      }

      boolean matchedPunctuation = true;
      switch (currentChar) {
        case '(': tokens.add(new Token(TokenType.L_PAREN, "(", line, column)); break;
        case ')': tokens.add(new Token(TokenType.R_PAREN, ")", line, column)); break;
        case ',': tokens.add(new Token(TokenType.COMMA, ",", line, column)); break;
        case ':': tokens.add(new Token(TokenType.COLON, ":", line, column)); break;
        case '?': tokens.add(new Token(TokenType.QUESTION, "?", line, column)); break;
        case '=': tokens.add(new Token(TokenType.ASSIGN, "=", line, column)); break;
        case ';': tokens.add(new Token(TokenType.SEMICOLON, ";", line, column)); break;
        default: matchedPunctuation = false;
      }

      if (matchedPunctuation) {
        advance();
        continue;
      }

      tokens.add(new Token(TokenType.ERROR, String.valueOf(currentChar), line, column));
      advance();
    }

    tokens.add(new Token(TokenType.EOF, "", line, column));
    return tokens;
  }

  private void handleWhitespace(char c) {
    advance();
  }

  private void advance() {
    if (pos < text.length()) {
      if (text.charAt(pos) == '\n') {
        line++;
        column = 1;
      } else {
        column++;
      }
      pos++;
    }
  }

  private Token parseNumber() {
    int startColumn = column;
    StringBuilder sb = new StringBuilder();
    boolean isFloat = false;

    while (pos < text.length()) {
      char c = text.charAt(pos);

      if (Character.isDigit(c)) {
        sb.append(c);
        advance();
      } else if (c == '.' && !isFloat) {
        isFloat = true;
        sb.append(c);
        advance();
      } else {
        break;
      }
    }

    TokenType type = isFloat ? TokenType.FLOAT : TokenType.NUMBER;
    return new Token(type, sb.toString(), line, startColumn);
  }


  private Token parseVariable() {
    int startColumn = column;
    StringBuilder sb = new StringBuilder();

    sb.append(text.charAt(pos));
    advance();

    boolean hasName = false;

    while (pos < text.length()) {
      char c = text.charAt(pos);
      if (Character.isLetterOrDigit(c) || c == '_') {
        sb.append(c);
        advance();
        hasName = true;
      } else {
        break;
      }
    }

    if (!hasName) {
      return new Token(TokenType.ERROR, sb.toString(), line, startColumn);
    }

    return new Token(TokenType.VARIABLE, sb.toString(), line, startColumn);
  }

  private Token parseWord() {
    int startColumn = column;
    StringBuilder sb = new StringBuilder();

    while (pos < text.length()) {
      char c = text.charAt(pos);
      if (Character.isLetterOrDigit(c) || c == '_') {
        sb.append(c);
        advance();
      } else {
        break;
      }
    }

    String word = sb.toString();
    TokenType type = TokenType.IDENTIFIER;

    if (word.equals("function")) {
      type = TokenType.KW_FUNCTION;
    } else if (word.equals("int")) {
      type = TokenType.TYPE_INT;
    } else if (word.equals("string")) {
      type = TokenType.TYPE_STRING;
    } else if (word.equals("float")) {
      type = TokenType.TYPE_FLOAT;
    }


    return new Token(type, word, line, startColumn);
  }

  private Token parseString(char quote) {
    int startColumn = column;
    int startLine = line;
    StringBuilder sb = new StringBuilder();

    sb.append(quote);
    advance();

    boolean isClosed = false;

    while (pos < text.length()) {
      char c = text.charAt(pos);
      sb.append(c);
      advance();

      if (c == quote) {
        isClosed = true;
        break;
      }
    }

    if (!isClosed) {
      return new Token(TokenType.ERROR, "Незакрытая строка: " + sb.toString(), startLine, startColumn);
    }

    return new Token(TokenType.STRING, sb.toString(), startLine, startColumn);
  }

}