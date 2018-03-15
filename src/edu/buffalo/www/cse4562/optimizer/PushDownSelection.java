/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * * 
 * Approaches in pushDown Select:
 * -get all selects
 * -for each select 
 *    -verify if it can be pushed down by finding the level
 *      -recurse for each node if the columns in the select match with all columns reaching the level.
 *      For this we need the table schema at each node 
 *      Approaches - use a getter method which return the schema according to the operator being used.
 *                  More documentation about this on respective operators
 *    -if the level is not same as the select itself then push to that level 
 *    -as the nodes do not have a backward pointer, and we need the parent node below which 
 *     the selection has to be inserted, both parent and child should be returned to insert the selection in between
 *     this is not feasible as: to switch the select too, its parent needs to be known
 *     APPROACH: have a backward pointer
 *     
 * @author Sneha Mehta
 *
 */
public class PushDownSelection {
  
  /**
   * @param root
   * static method called from Optimizer to push all selects down wherever possible
   */
  public static void pushDownSelect(Node root) {
    List<Node> allSelectNodes = getAllSelectNodes(root);
    for (Node selectNode : allSelectNodes) {
      verifyAndPushDownSelect(selectNode);
    }
  }

  /**
   * @param selectNode
   * 
   * Given one select node, verifies if it can be pushed down and pushes it
   */
  private static void verifyAndPushDownSelect(Node selectNode) {
    //Schema for the required selection
    List<Pair<Integer, Integer>> selectSchema = new ArrayList<>();

    Expression selectExpression = ((SelectionOperator)selectNode).getExpression();

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

    Node pushDownLevel = getPushDownLevel(selectNode, selectSchema);
    
    if (pushDownLevel != selectNode) {
      pushDown(selectNode, pushDownLevel);
    }
  }

  /**
   * @param selectNode
   * @param pushLevelNode
   * 
   * once verified, this pushes the select node to the required level
   * THIS CAN BE IMPLEMENTED COMMONLY FOR EACH OPTIMIZATION TYPE
   */
  private static void pushDown(Node selectNode, Node pushLevelNode) {
    int index=0;
    for(Node child: selectNode.getParent().getChildren()) {
      if(child==selectNode && !selectNode.getChildren().isEmpty()) {
        //child=selectNode.getChildren().get(0);
        selectNode.getParent().getChildren().set(index, selectNode.getChildren().get(0));
      }
      index++;
    }
    
    Node parent = pushLevelNode.getParent();
    index=0;
    for(Node child: parent.getChildren()) {
      if(child==pushLevelNode && !pushLevelNode.getChildren().isEmpty()) {
        selectNode.setChildren(pushLevelNode.getChildren());
        parent.getChildren().set(index, selectNode);
      }
      index++;
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
    
    for (Node nextLevel : node.getChildren()) {
      List<Pair<Integer, Integer>> builtSchema = nextLevel.getBuiltSchema();
      if (builtSchema.containsAll(selectSchema)) {
        getPushDownLevel(nextLevel, selectSchema);
      }
    }
    return pushDownLevel;
  }

  /**
   * @param column
   * @param selectSchema
   * 
   * Adds all tableId,columnId pairs to the selectSchema
   */
  private static void addToSelectSchema(Column column,
      List<Pair<Integer, Integer>> selectSchema) {
    Integer tableId = SchemaManager
        .getTableId(column.getTable().getName());
    Integer columnId = SchemaManager.getColumnIdByTableId(tableId,
        column.getColumnName());
    selectSchema.add(new Pair<Integer, Integer>(tableId, columnId));

  }

  /**
   * @param root
   * @return all select nodes in the tree
   */
  private static List<Node> getAllSelectNodes(Node root) {
    List<Node> allSelectNodes = new ArrayList<Node>();
    if (root instanceof SelectionOperator) {
      allSelectNodes.add(root);
    }

    for (Node node : root.getChildren()) {
      return getAllSelectNodes(node);
    }
    return allSelectNodes;

  }
}
