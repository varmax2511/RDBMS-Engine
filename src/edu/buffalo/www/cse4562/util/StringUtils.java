package edu.buffalo.www.cse4562.util;

public class StringUtils {

  public static boolean isBlank(String input) {
    if (null == input || input.trim().length() == 0) {
      return true;
    }
    return false;
  }
}
