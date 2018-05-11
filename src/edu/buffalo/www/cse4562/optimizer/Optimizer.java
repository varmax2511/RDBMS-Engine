/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;

/**
 * Optimize operations: -all selections split to unary operations -push down
 * select -push down project -convert cross products to join wherever possible
 *
 * 
 * @author Sneha Mehta
 *
 */
public class Optimizer {

  final static List<Node> allSelectNodes = new ArrayList<>();
  final static List<ProjectionOperator> allProjectNodes = new ArrayList<>();
  /**
   * @param root
   */
  public static Node optimizeTree(Node root) {
    // build schema
    root.getBuiltSchema();
    populateProjectAndSelectNodes(root);
    PushDownSelection.pushDownSelect(root,allSelectNodes);
    // :TODO fix push down project for renaming
    // like SELECT A AS C FROM R WHERE A=4;
    root = PushDownProjection.pushDownProject(root,allProjectNodes);
    //IndexJoinOptimizer.generateIndexJoin(root);
    // re-build schema
    //root.getBuiltSchema();

    return root;
  }
  private static void populateProjectAndSelectNodes(Node root) {
    final List<ProjectionOperator> allProjectNodes = new ArrayList<ProjectionOperator>();
    if (root instanceof ProjectionOperator) {
      allProjectNodes.add((ProjectionOperator) root);
    }
    else if (root instanceof SelectionOperator) {
      allSelectNodes.add(root);
    }

    for (final Node node : root.getChildren()) {
      populateProjectAndSelectNodes(node);
    } // for

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
