package org.veteroch4k.laba1;

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
        regex = "[a-zA-Z0-9а-яА-Я._%+-]+@[a-zA-Z0-9а-яА-Я.-]+\\.[a-zA-Zа-яА-Я]{2,}";
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
}