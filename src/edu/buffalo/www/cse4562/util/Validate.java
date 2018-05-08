package edu.buffalo.www.cse4562.util;

import java.util.Collection;

/**
 * Representation of Validate class of Apache Commons.
 * 
 * @author varunjai
 *
 */
public class Validate {

  /**
   * Validate that the specified argument is not null; otherwise throwing an
   * {@link IllegalArgumentException}.
   * 
   * @param object
   * @throws IllegalArgumentException
   */
  public static void notNull(Object object) {
    notNull(object, "Object cannot be null");
  }

  /**
   * Validate that the specified argument is not null; otherwise throwing an
   * {@link IllegalArgumentException} with the specified message.
   * 
   * @param object
   * @param message
   * @throws IllegalArgumentException
   */
  public static void notNull(Object object, String message) {
    if (null == object) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * 
   * @param input
   * @throws IllegalArgumentException
   */
  public static void notBlank(String input) {
    if (null == input || input.trim().length() == 0) {
      throw new IllegalArgumentException("Input cannot be blank");
    }
  }
  
  
  public static void notEmpty(Collection<?> collection){
    if (null == collection || collection.size() == 0) {
      throw new IllegalArgumentException("Collection is not valid");
    }
  }
}
