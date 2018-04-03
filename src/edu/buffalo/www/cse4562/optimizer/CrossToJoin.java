/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.JoinOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;

/**
 * @author Sneha Mehta
 *
 */
public class CrossToJoin {

  /**
   * <pre>
   * This method makes a tree of the form
   * 
   *     ...                  ...
   *      |                    |
   *    Select      ->        Join 
   *      |                   /  \
   *    Cross   
   *    /   \
   * 
   * </pre>
   * 
   * @param selectNode
   * @param pushDownLevel
   */
  public static void convertCrossToJoin(SelectionOperator selectNode,
      Node pushDownLevel) {

    final JoinOperator joinNode = new JoinOperator(selectNode.getExpression());
    joinNode.setChildren(pushDownLevel.getChildren());
    joinNode.setParent(selectNode.getParent());
    joinNode.setSchema(pushDownLevel.getBuiltSchema());
    

    int index = 0;
    // removing selectNode from the tree and updating references
    for (final Node child : selectNode.getParent().getChildren()) {
      // if no match or invalid select node
      if (child != selectNode || selectNode.getChildren().isEmpty()) {
        index++;
        continue;
      }

      selectNode.getParent().getChildren().set(index, joinNode);
      // selectNode.getChildren().get(0).setParent(selectNode.getParent());

      index++;
    } // for

    // update references for pushdown or cross product children
    for (final Node child : pushDownLevel.getChildren()) {
      child.setParent(joinNode);
    }

  }

}
