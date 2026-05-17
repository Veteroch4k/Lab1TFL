package org.veteroch4k.laba1.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexSearcher {

  public static List<SearchResult> search(String text, String regexType) {
    String regex = "";

    switch (regexType) {
      case "Числа":
        regex = "[+-]?(?:(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?|\\.[0-9]+)";
        break;
      case "Пароли":
        regex = "[A-Za-zА-Яа-я0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{10,}";
        break;
      case "Email":
        regex = "[a-zA-Z0-9а-яА-Я][a-zA-Z0-9а-яА-Я._%+-]*@[a-zA-Z0-9а-яА-Я-]+(?:\\.[a-zA-Zа-яА-Я]{2,})+";
        break;
    }

    List<SearchResult> results = new ArrayList<>();
    if (text == null || text.isEmpty() || regex.isEmpty()) return results;

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);

    while (matcher.find()) {
      String foundText = matcher.group();
      int start = matcher.start();
      int end = matcher.end();
      int length = end - start;

      int[] lineAndCol = calculateLineAndColumn(text, start);

      results.add(new SearchResult(foundText, lineAndCol[0], lineAndCol[1], length, start, end));
    }
    return results;
  }

  private static int[] calculateLineAndColumn(String fullText, int index) {
    int line = 1;
    int column = 1;
    for (int i = 0; i < index; i++) {
      if (fullText.charAt(i) == '\n') {
        line++;
        column = 1;
      } else {
        column++;
      }
    }
    return new int[]{line, column};
  }


  public static List<SearchResult> searchEmailWithAutomaton(String text) {
    List<SearchResult> results = new ArrayList<>();
    int state = 0;
    int startIdx = -1;

    String paddedText = text + " ";

    for (int i = 0; i < paddedText.length(); i++) {
      char c = paddedText.charAt(i);

      switch (state) {
        case 0:
          if (isLocalChar(c)) {
            state = 1;
            startIdx = i;
          }
          break;

        case 1:
          if (c == '@') {
            state = 2;
          } else if (!isLocalChar(c)) {
            state = 0;
          }
          break;

        case 2:
          if (isDomainChar(c)) {
            state = 3;
          } else {
            state = 0;
          }
          break;

        case 3:
          if (c == '.') {
            state = 4;
          } else if (!isDomainChar(c)) {
            state = 0;
          }
          break;

        case 4:
          if (isZoneChar(c)) {
            state = 5;
          } else {
            state = 0; // Сброс
          }
          break;

        case 5:
          if (isZoneChar(c)) {
            state = 6;
          } else if (c == '.') {
            state = 4;
          } else {

            state = 0;
          }
          break;

        case 6:
          if (isZoneChar(c)) {
            state = 6;
          } else if (c == '.') {
            state = 4;
          } else {

            int endIdx = i;
            String foundEmail = paddedText.substring(startIdx, endIdx);
            int[] lineAndCol = calculateLineAndColumn(text, startIdx);

            results.add(new SearchResult(foundEmail, lineAndCol[0], lineAndCol[1], endIdx - startIdx, startIdx, endIdx));

            state = 0;
            i--;
          }
          break;
      }
    }
    return results;
  }

  private static boolean isLocalChar(char c) {
    return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '%' || c == '+' || c == '-';
  }
  private static boolean isDomainChar(char c) {
    return Character.isLetterOrDigit(c) || c == '-';
  }
  private static boolean isZoneChar(char c) {
    return Character.isLetter(c);
  }


}