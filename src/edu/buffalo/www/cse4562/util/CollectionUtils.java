package edu.buffalo.www.cse4562.util;

import java.util.Collection;

import edu.buffalo.www.cse4562.model.Tuple;

public class CollectionUtils {

  public static boolean isEmpty(Collection<?> collection) {
    if (null == collection || collection.size() == 0) {
      return true;
    }
    return false;
  }

  /**
   * check if collection of {@link Tuple} is empty.
   * 
   * @param collection
   * @return
   */
  public static boolean areTuplesEmpty(Collection<Tuple> collection) {
    if (null == collection || collection.size() == 0) {
      return true;
    }

    return !collection.stream()
        .anyMatch(tuple -> tuple != null && !tuple.isEmpty());
  }

}