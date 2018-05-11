/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

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
    // build schema
    root.getBuiltSchema();

    PushDownSelection.pushDownSelect(root);
    // :TODO fix push down project for renaming
    // like SELECT A AS C FROM R WHERE A=4;
    root = PushDownProjection.pushDownProject(root);
    IndexJoinOptimizer.generateIndexJoin(root);
    // re-build schema
    //root.getBuiltSchema();

    return root;
  }

  public static Node pushDown(Node root, Node node, Node pushLevelNode) {
    if (node == root)
      root = node.getChildren().get(0);
    // pop
    Node parent = node.getParent();
    for (Node child : node.getChildren()) {
      child.setParent(parent);
    }
    if (parent != null) {
      parent.setChildren(node.getChildren());
    }

    // push
    Node parent1 = pushLevelNode.getParent();
    node.setParent(parent1);
    pushLevelNode.setParent(node);
    int index = 0;
    for (Node child : parent1.getChildren()) {
      if (child == pushLevelNode) {
        parent1.getChildren().set(index, node);
        break;
      }
      index++;
    }
    final List<Node> children = new ArrayList<>();
    children.add(pushLevelNode);
    node.setChildren(children);
    root.getBuiltSchema();
    return root;
  }

}
