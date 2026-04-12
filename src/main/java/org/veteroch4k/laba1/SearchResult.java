package org.veteroch4k.laba1;

public class SearchResult {
  private final String text;
  private final int line;
  private final int column;
  private final int length;
  private final int startOffset;
  private final int endOffset;

  public SearchResult(String text, int line, int column, int length, int startOffset, int endOffset) {
    this.text = text;
    this.line = line;
    this.column = column;
    this.length = length;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }

  public String getText() { return text; }
  public int getLine() { return line; }
  public int getColumn() { return column; }
  public int getLength() { return length; }
  public int getStartOffset() { return startOffset; }
  public int getEndOffset() { return endOffset; }
}