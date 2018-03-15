package edu.buffalo.www.cse4562.util;

import java.util.ArrayList;
import java.util.Collection;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Tuple;

/**
 * Process each type of query.
 *
 * @author varunjai
 *
 */
public class TreeProcessor {

  /**
   * Process the Tree.
   *
   * @param root
   *          !null.
   * @return
   * @throws Throwable
   */
  public static Collection<Tuple> processTree(Node root) throws Throwable {
    Collection<Tuple> tuples = new ArrayList<>();
    final Collection<Tuple> output = new ArrayList<>();

    root.getBuiltSchema();
    root.open();
    // move iteratively
    while (root.hasNext()) {
      tuples = root.getNext();
      
      if(tuples.isEmpty()){
        continue;
      }
      output.addAll(tuples);
    } // while

    root.close();
    return output;
  }

}
