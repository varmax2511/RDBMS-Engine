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
    //PushDownProjection.pushDownProject(root);

    // re-build schema
    root.getBuiltSchema();
    return root;
  }

  /**
   * @param node
   * @param pushLevelNode
   */
  public static void pushDown(Node node, Node pushLevelNode) {
    int index = 0;
    // removing node from the tree and updating references
    if (node.getParent() != null) {
      for (final Node child : node.getParent().getChildren()) {
        // if no match or invalid node
        if (child != node || node.getChildren().isEmpty()) {
          index++;
          continue;
        }

        node.getParent().getChildren().set(index, node.getChildren().get(0));
        node.getChildren().get(0).setParent(node.getParent());

        index++;
      } // for
    } // if
    final Node parent = pushLevelNode.getParent();
    index = 0;
    for (final Node child : parent.getChildren()) {
      // if no match, ignore
      if (child != pushLevelNode) {
        index++;
        continue;
      }

      // set the pushLevelNode as child of given node
      final List<Node> projectChildren = new ArrayList<>();
      projectChildren.add(pushLevelNode);
      node.setChildren(projectChildren);
      pushLevelNode.setParent(node);

      // adding given node as pushLevelNode's child at the position index of
      // pushLevelNode
      parent.getChildren().set(index, node);
      node.setParent(parent);

      index++;
    } // for

  }

}
