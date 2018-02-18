package edu.buffalo.www.cse4562.util;

import java.util.Collection;

public class CollectionUtils {

  public static boolean isEmpty(Collection<?> collection){
    if(null == collection || collection.size() == 0){
      return true;
    }
    return false;
  }
}
