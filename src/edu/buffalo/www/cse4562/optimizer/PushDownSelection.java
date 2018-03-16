/**
 *
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.JoinOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

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
      verifyAndPushDownSelect(selectNode);
    }
  }

  /**
   * @param selectNode
   *
   *          Given one select node, verifies if it can be pushed down and
   *          pushes it
   */
  private static void verifyAndPushDownSelect(Node selectNode) {
    // Schema for the required selection
    final List<Pair<Integer, Integer>> selectSchema = new ArrayList<>();

    final Expression selectExpression = ((SelectionOperator) selectNode)
        .getExpression();

    // since all where conditions will be a binary expression
    if (selectExpression instanceof BinaryExpression) {
      if (((BinaryExpression) selectExpression)
          .getLeftExpression() instanceof Column) {
        addToSelectSchema(
            (Column) ((BinaryExpression) selectExpression).getLeftExpression(),
            selectSchema);
      }
      if (((BinaryExpression) selectExpression)
          .getRightExpression() instanceof Column) {
        addToSelectSchema(
            (Column) ((BinaryExpression) selectExpression).getRightExpression(),
            selectSchema);
      }
    }

    // level on top of which Select node will be setup
    final Node pushDownLevel = getPushDownLevel(selectNode, selectSchema);

    // if pushlevel is same as select node, no optimization can be done
    if (pushDownLevel == selectNode) {
      return;
    }

    // push down select to the appropriate level
    pushDown(selectNode, pushDownLevel);

    // if push level is a cross product, now select is above cross product
    // convert to join
    if (pushDownLevel instanceof CrossProductOperator) {
      convertCrossToJoin((SelectionOperator) selectNode, pushDownLevel);
      return;
    }

  }

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

  /**
   * @param selectNode
   * @param pushLevelNode
   *
   *          once verified, this pushes the select node to the required level
   *          THIS CAN BE IMPLEMENTED COMMONLY FOR EACH OPTIMIZATION TYPE
   */
  private static void pushDown(Node selectNode, Node pushLevelNode) {
    int index = 0;
    // removing selectNode from the tree and updating references
    for (final Node child : selectNode.getParent().getChildren()) {
      // if no match or invalid select node
      if (child != selectNode || selectNode.getChildren().isEmpty()) {
        index++;
        continue;
      }

      selectNode.getParent().getChildren().set(index,
          selectNode.getChildren().get(0));
      selectNode.getChildren().get(0).setParent(selectNode.getParent());

      index++;
    } // for

    final Node parent = pushLevelNode.getParent();
    index = 0;
    for (final Node child : parent.getChildren()) {
      // if no match, ignore
      if (child != pushLevelNode) {
        index++;
        continue;
      }

      // set the pushLevelNode as child of select node
      final List<Node> selectChildren = new ArrayList<>();
      selectChildren.add(pushLevelNode);
      selectNode.setChildren(selectChildren);
      pushLevelNode.setParent(selectNode);

      // adding select node as pushLevelNode's child at the position index of
      // pushLevelNode
      parent.getChildren().set(index, selectNode);
      selectNode.setParent(parent);

      index++;
    } // for

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
   * @param column
   * @param selectSchema
   *
   *          Adds all tableId,columnId pairs to the selectSchema
   */
  private static void addToSelectSchema(Column column,
      List<Pair<Integer, Integer>> selectSchema) {
    final Integer tableId = SchemaManager
        .getTableId(column.getTable().getName());

    // if no table id found, it means that its a simple expression
    // like SELECT A,B FROM R WHERE C < 3;
    // this doesn't need optimization, rather an attempt to optimize it
    // by looking into the Select node children, getting the schema info
    // and guessing the table is costly.
    if (tableId == null) {
      return;
    }

    final Integer columnId = SchemaManager.getColumnIdByTableId(tableId,
        column.getColumnName());
    selectSchema.add(new Pair<Integer, Integer>(tableId, columnId));

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
