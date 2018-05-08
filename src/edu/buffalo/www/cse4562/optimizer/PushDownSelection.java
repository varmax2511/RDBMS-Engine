/**
 *
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.RequiredBuiltSchema;
import net.sf.jsqlparser.expression.Expression;

/**
 * * Approaches in pushDown Select: -get all selects -for each select -verify if
 * it can be pushed down by finding the level -recurse for each node if the
 * columns in the select match with all columns reaching the level. For this we
 * need the table schema at each node Approaches - use a getter method which
 * return the schema according to the operator being used. More documentation
 * about this on respective operators -if the level is not same as the select
 * itself then push to that level -as the nodes do not have a backward pointer,
 * and we need the parent node below which the selection has to be inserted,
 * both parent and child should be returned to insert the selection in between
 * this is not feasible as: to switch the select too, its parent needs to be
 * known APPROACH: have a backward pointer
 *
 * @author Sneha Mehta
 *
 */
public class PushDownSelection {

  /**
   * @param root
   *          static method called from Optimizer to push all selects down
   *          wherever possible
   */
  public static void pushDownSelect(Node root) {
    final List<Node> allSelectNodes = getAllSelectNodes(root);
    for (final Node selectNode : allSelectNodes) {
      verifyAndPushDownSelect(root,selectNode);
    }
  }

  /**
   * @param selectNode
   *
   *          Given one select node, verifies if it can be pushed down and
   *          pushes it
   */
  private static void verifyAndPushDownSelect(Node root,Node selectNode) {
    final Expression selectExpression = ((SelectionOperator) selectNode)
        .getExpression();

    // Schema for the required selection
    RequiredBuiltSchema requiredBuiltSchema= new RequiredBuiltSchema();
    final List<Pair<Integer, Integer>> selectSchema = requiredBuiltSchema.getRequiredSchema(selectExpression,selectNode);
    // level on top of which Select node will be setup
    final Node pushDownLevel = getPushDownLevel(selectNode, selectSchema);

    // if pushlevel is same as select node, no optimization can be done
    if (pushDownLevel == selectNode) {
      return;
    }

    // push down select to the appropriate level
    Optimizer.pushDown(root, selectNode, pushDownLevel);

    // if push level is a cross product, now select is above cross product
    // convert to join
    if (pushDownLevel instanceof CrossProductOperator) {
      CrossToJoin.convertCrossToJoin((SelectionOperator) selectNode, pushDownLevel);
      return;
    }

  }

 

  /**
   * @param node
   * @param selectSchema
   * @return the level to which a select node can be pushed
   */
  private static Node getPushDownLevel(Node node,
      List<Pair<Integer, Integer>> selectSchema) {
    Node pushDownLevel = node;

    // no-op
    if (CollectionUtils.isEmpty(selectSchema)) {
      return pushDownLevel;
    }

    for (final Node nextLevel : node.getChildren()) {

      // we can't push below leaf
      /*
       * if(nextLevel.isLeaf()){ continue; }
       */

      final List<Pair<Integer, Integer>> builtSchema = nextLevel
          .getBuiltSchema();
      if (builtSchema.containsAll(selectSchema)) {
        pushDownLevel = getPushDownLevel(nextLevel, selectSchema);
      }
    } // for
    return pushDownLevel;
  }

  /**
   * @param root
   * @return all select nodes in the tree
   */
  private static List<Node> getAllSelectNodes(Node root) {
    final List<Node> allSelectNodes = new ArrayList<Node>();
    if (root instanceof SelectionOperator) {
      allSelectNodes.add(root);
    }

    for (final Node node : root.getChildren()) {
      allSelectNodes.addAll(getAllSelectNodes(node));
    } // for

    return allSelectNodes;

  }
}
