/**
 * 
 */
package edu.buffalo.www.cse4562.util;

import java.util.Map;

/**
 * @author Sneha Mehta
 *
 */
public class MapUtils {

  public static boolean isEmpty(Map<?,?> map) {
    if (null == map || map.size() == 0) {
      return true;
    }
    return false;
  }

}
