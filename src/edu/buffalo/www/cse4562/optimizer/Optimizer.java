/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import edu.buffalo.www.cse4562.model.Node;

/**
 * Optimize operations: -all selections split to unary operations -push down
 * select -push down project -convert cross products to join wherever possible
 *
 * 
 * @author Sneha Mehta
 *
 */
public class Optimizer {

  /**
   * @param root
   */
  public static Node optimizeTree(Node root) {
    // splitAllSelectConditions(root);
    root.getBuiltSchema();
    PushDownSelection.pushDownSelect(root);
    PushDownProjection.pushDownProject(root);
    CrossToJoin.convertCrossProductToJoin(root);
    return root;
  }

}
