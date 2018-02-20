package edu.buffalo.www.cse4562.query;

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
public class QueryProcessor {

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

    // move iteratively
    while (root.hasNext()) {
      tuples = root.getNext();
      output.addAll(tuples);
    } // while

    return output;
  }

}
