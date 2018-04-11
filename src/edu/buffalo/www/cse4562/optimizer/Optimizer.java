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

    // re-build schema
    root.getBuiltSchema();
    return root;
  }

  /**
   * @param node
   * @param pushLevelNode
   */
  public static Node pushDown1(Node root, Node node, Node pushLevelNode) {
    if (node == root)
      root = node.getChildren().get(0);
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
    else {
      node.getChildren().get(0).setParent(null);
      final List<Node> children = new ArrayList<>();
      children.add(node);
      node.getChildren().get(0).setChildren(children);
    }
    final Node parent = pushLevelNode.getParent();
    index = 0;
    if (parent != null) {
      for (final Node child : parent.getChildren()) {
        // if no match, ignore
        if (child != pushLevelNode) {
          index++;
          continue;
        }

        // set the pushLevelNode as child of given node
        final List<Node> children = new ArrayList<>();
        children.add(pushLevelNode);
        node.setChildren(children);
        pushLevelNode.setParent(node);

        // adding given node as pushLevelNode's child at the position index of
        // pushLevelNode
        parent.getChildren().set(index, node);
        node.setParent(parent);

        index++;
      } // for
    } else {
      final List<Node> projectChildren = new ArrayList<>();
      projectChildren.add(pushLevelNode);
      node.setChildren(projectChildren);
      pushLevelNode.setParent(node);
    }
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
    return root;
  }

  public static Node pushDownProject(Node root, Node node, Node addNode,
      Node pushLevelNode) {
    if (node == root) {
      root = addNode;
      addNode.addChild(node.getChildren().get(0));
    } else {
      Node father = node.getParent();
      father.addChild(addNode);
      addNode.setParent(father);
    }

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
    return root;
  }
  
}
