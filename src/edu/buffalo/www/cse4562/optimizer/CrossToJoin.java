/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.operator.BlockNestedJoinOperator;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.HashJoinOperator;
import edu.buffalo.www.cse4562.operator.JoinOperator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.RequiredBuiltSchema;
import edu.buffalo.www.cse4562.util.SchemaUtils;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;

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
     Node joinNode; 

    if(selectNode.getExpression() instanceof EqualsTo) {
      joinNode = new HashJoinOperator(selectNode.getExpression());
      joinNode.setChildren(pushDownLevel.getChildren());
      joinNode.setParent(selectNode.getParent());
      ((HashJoinOperator) joinNode).setSchema(pushDownLevel.getBuiltSchema());
    }
    else {
      joinNode = new BlockNestedJoinOperator(selectNode.getExpression());
      joinNode.setChildren(pushDownLevel.getChildren());
      joinNode.setParent(selectNode.getParent());
      ((BlockNestedJoinOperator) joinNode).setSchema(pushDownLevel.getBuiltSchema());
    }
    

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

  public static void pushProjectDownCross(Node root,
      ProjectionOperator projectNode, CrossProductOperator pushDownLevel) {

  }

  public static void pushProjectDownJoin(Node root,
      ProjectionOperator projectNode, Node pushDownLevel) {

    /*
     * Update projection schema
     */
    final List<Pair<Integer, Integer>> projectBuiltSchema = projectNode
        .getBuiltSchema();

    if (pushDownLevel instanceof JoinOperator) {
      // when selection see what is required by the operator is satisfied by
      // the projection schema or not
      RequiredBuiltSchema requiredBuiltSchema= new RequiredBuiltSchema();
      final List<Pair<Integer, Integer>> requiredSchema = requiredBuiltSchema
          .getRequiredSchema(((JoinOperator) pushDownLevel).getExpression(),
              pushDownLevel);

      if (!projectBuiltSchema.containsAll(requiredSchema)) {
        SchemaUtils.updateProjectNodeSchema(projectNode, projectBuiltSchema,
            requiredSchema);
      }

    }// if
    
    // create new project for left child
    final ProjectionOperator project1 = getDifProjection(projectNode,
        getDif(projectNode.getBuiltSchema(),
            pushDownLevel.getChildren().get(1).getBuiltSchema()));

    // create new project for right child
    final ProjectionOperator project2 = getDifProjection(projectNode,
        getDif(projectNode.getBuiltSchema(),
            pushDownLevel.getChildren().get(0).getBuiltSchema()));

    // append
    project1.addChild(pushDownLevel.getChildren().get(0));
    project2.addChild(pushDownLevel.getChildren().get(1));
    project1.setParent(pushDownLevel);
    project2.setParent(pushDownLevel);

    pushDownLevel.getChildren().get(0).setParent(project1);
    pushDownLevel.getChildren().get(1).setParent(project2);
    pushDownLevel.getChildren().set(0, project1);
    pushDownLevel.getChildren().set(1, project2);

    // remove projectnode
    pushDownLevel.setParent(projectNode.getParent());

    int idx = 0;
    for (Node child : projectNode.getParent().getChildren()) {
      if (child == projectNode) {
        projectNode.getParent().getChildren().set(idx,
            projectNode.getChildren().get(0));
      }
      idx++;
    }// for

    projectNode.setChildren(null);
    projectNode.setParent(null);
    projectNode = null;
    if (!project1.isAllColFlag()) {
      root = PushDownProjection.verifyAndPushDownProject(root, project1);
     }
     if (!project2.isAllColFlag()) {
       root =PushDownProjection.verifyAndPushDownProject(root, project2);
     }
    root.getBuiltSchema();
  }

  /**
   * 
   * @param parentProjection
   * @param pairs
   * @return
   */
  private static ProjectionOperator getDifProjection(
      ProjectionOperator parentProjection, List<Pair<Integer, Integer>> pairs) {

    final ProjectionOperator project = new ProjectionOperator();
    for (Pair<Integer, Integer> pair : pairs) {

      project.addSelectExpressionItems(
          parentProjection.getPair2SelectExprItem().get(pair));
    }
    return project;
  }

  /**
   * Get the output containing a list of list1 - list2.
   * 
   * @param list1
   * @param list2
   * @return
   */
  public static List<Pair<Integer, Integer>> getDif(
      List<Pair<Integer, Integer>> list1, List<Pair<Integer, Integer>> list2) {

    final List<Pair<Integer, Integer>> output = new ArrayList<>();

    for (Pair<Integer, Integer> pair : list1) {
      if (!list2.contains(pair)) {
        output.add(pair);
      }
    }
    return output;
  }

}
