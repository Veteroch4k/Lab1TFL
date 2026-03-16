package org.veteroch4k.laba1;

public record ErrorItem(String filePath, int line, int column, String message, String errorValue) {

}
