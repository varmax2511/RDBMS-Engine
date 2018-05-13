/**
 *
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.operator.AggregateOperator;
import edu.buffalo.www.cse4562.operator.BinaryOperator;
import edu.buffalo.www.cse4562.operator.GroupByOperator;
import edu.buffalo.www.cse4562.operator.JoinOperator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.ExpressionDecoder;
import edu.buffalo.www.cse4562.util.RequiredBuiltSchema;
import edu.buffalo.www.cse4562.util.SchemaUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

/**
 * <pre>
 * Approach:
 * 1. Identify what schema is required by each operator (required Schema) and
 * what output does it give (builtSchema)
 * 2. Check if the projection builtSchema contains all the tableids and column ids
 * required by the child of it.
 *
 * If its true then we can push it down.
 *
 * Now once this process is done, then check if the projection sits above a
 * Join or Cross-Product.
 * Find SET Dif of built schema of projection from the built schema of left child.
 * Using the set diff result spawn a new project operator with the set diff as
 * its built schema and having expressions satisfying the built schema and place
 * it over the right child.
 * Again using SET Dif of built schema of projection from the built schema of right child.
 * Using the set diff result spawn a new project operator with the set diff as
 * its built schema and having expressions satisfying the built schema and place
 * it over the left child.
 * Remove the projection opr.
 *
 * To separate expressions, when projection operators builds its schema, it
 * should also maintain mapping of pair to {@link SelectExpressionItem} which
 * tells which pair is generated from which expression
 *
 *
 *
 * </pre>
 *
 * @author Sneha Mehta
 *
 */
public class PushDownProjection {

  /**
   * @param root
   */
  public static Node pushDownProject(Node root) {
    final List<ProjectionOperator> allProjectNodes = getAllProjectNodes(root);

    for (final ProjectionOperator projectNode : allProjectNodes) {
      // No pointing pushing it down for select *
      if (!projectNode.isAllColFlag()) {
        root = verifyAndPushDownProject(root, projectNode);
      }
    }
    return root;
  }

  /**
   * @param root
   * @return
   */
  private static List<ProjectionOperator> getAllProjectNodes(Node root) {
    final List<ProjectionOperator> allProjectNodes = new ArrayList<ProjectionOperator>();
    if (root instanceof ProjectionOperator) {
      allProjectNodes.add((ProjectionOperator) root);
    }

    for (final Node node : root.getChildren()) {
      allProjectNodes.addAll(getAllProjectNodes(node));
    } // for

    return allProjectNodes;

  }

  /**
   * @param projectNode
   */
  public static Node verifyAndPushDownProject(Node root,
      ProjectionOperator projectNode) {

    Node orignalProject = projectNode.getDeepCopy();
    // remove expressions from projectNode as they will be covered by the
    // originalProject
    decodeExprNFunctions(projectNode);

    // get push down level
    Node pushDownLevel = getPushDownLevel(projectNode, projectNode);

    // if pushlevel is same as select node, no optimization can be done
    if (pushDownLevel == projectNode
        || pushDownLevel.getParent() == projectNode) {
      root = appendProject(root, projectNode, orignalProject);
      return removeNode(root, projectNode);
    }

    // TODO push down should be inserted in case of aggregates like sum(c) and
    // not pushed down

    root = appendProject(root, projectNode, orignalProject);

    // push down select to the appropriate level
    Optimizer.pushDown(root, projectNode, pushDownLevel);

    if (pushDownLevel instanceof BinaryOperator) {
      CrossToJoin.pushProjectDownJoin(root, projectNode, pushDownLevel);
    }

    return root;
  }

  private static Node appendProject(Node root, ProjectionOperator projectNode,
      Node orignalProject) {
    Node parentNode = projectNode.getParent();
    orignalProject.setChildren(null);
    final List<Node> children = new ArrayList<>();
    orignalProject.setChildren(children);
    orignalProject.addChild(projectNode);
    projectNode.setParent(orignalProject);
    // projectNode.setSelectExpressionItems(((ProjectionOperator)orignalProject).getSelectExpressionItems());

    if (root == projectNode) {
      root = orignalProject;
      return root;
    }

    int index = 0;
    for (Node child : parentNode.getChildren()) {

      if (child == projectNode) {
        parentNode.getChildren().set(index, orignalProject);
        break;
      }
      index++;
    } // for

    return root;
  }

  /**
   * If the project consists of any expression or function, then we want to
   * extract the {@link Column} required to compute that expression or function
   * and add it to the projection operator {@link SelectExpressionItem} list if
   * its not already present.
   * 
   * @param projectNode
   */
  private static void decodeExprNFunctions(ProjectionOperator projectNode) {
    final Set<SelectExpressionItem> projectExprs = new LinkedHashSet<>();
    for (SelectExpressionItem exprItem : projectNode
        .getSelectExpressionItems()) {

      for (Expression decodedExpr : new ExpressionDecoder(
          exprItem.getExpression()).getDecodedColumns()) {
        SelectExpressionItem selectionExprItem = new SelectExpressionItem();
        selectionExprItem.setExpression(decodedExpr);
        projectExprs.add(selectionExprItem);
      } // for
    } // for

    final List<SelectExpressionItem> exprs = new ArrayList<>();
    for (SelectExpressionItem item : projectExprs) {
      exprs.add(item);
    }
    projectNode.setSelectExpressionItems(exprs);
  }

  /**
   * @param node
   * @param projectSchema
   * @return
   */
  private static Node getPushDownLevel(Node node,
      ProjectionOperator projectNode) {
    Node pushDownLevel = node;

    if (projectNode == null) {
      return pushDownLevel;
    }

    for (final Node nextLevel : node.getChildren()) {

      final List<Pair<Integer, Integer>> projectBuiltSchema = projectNode
          .getBuiltSchema();
      // Don't go below scanner or join, cross
      /*
       * if (nextLevel instanceof ScannerOperator || nextLevel instanceof
       * BinaryOperator) { pushDownLevel = nextLevel; break; }
       */
      // if not the below list, break
      if (!(nextLevel instanceof SelectionOperator)
         // && !(nextLevel instanceof JoinOperator)
          && !(nextLevel instanceof AggregateOperator)
          && !(nextLevel instanceof GroupByOperator)) {
        pushDownLevel = nextLevel;
        break;
      }

      if (nextLevel instanceof SelectionOperator) {
        // when selection see what is required by the operator is satisfied by
        // the projection schema or not
        RequiredBuiltSchema requiredBuiltSchema = new RequiredBuiltSchema();
        final List<Pair<Integer, Integer>> requiredSchema = requiredBuiltSchema
            .getRequiredSchema(((SelectionOperator) nextLevel).getExpression(),
                nextLevel);

        if (!projectBuiltSchema.containsAll(requiredSchema)) {
          SchemaUtils.updateProjectNodeSchema(projectNode, projectBuiltSchema,
              requiredSchema);
        }

        pushDownLevel = getPushDownLevel(nextLevel, projectNode);

      } else if (nextLevel instanceof JoinOperator) {
        // when selection see what is required by the operator is satisfied by
        // the projection schema or not
        RequiredBuiltSchema requiredBuiltSchema = new RequiredBuiltSchema();
        final List<Pair<Integer, Integer>> requiredSchema = requiredBuiltSchema
            .getRequiredSchema(((JoinOperator) nextLevel).getExpression(),
                nextLevel);

        if (!projectBuiltSchema.containsAll(requiredSchema)) {
          SchemaUtils.updateProjectNodeSchema(projectNode, projectBuiltSchema,
              requiredSchema);
        }

        pushDownLevel = getPushDownLevel(nextLevel, projectNode);
      } else if (nextLevel instanceof AggregateOperator) {

        final AggregateOperator nextOpr = (AggregateOperator) nextLevel;
        // expr like count(*)
        List<Expression> expressions = new ArrayList<>();
        
        for(Function function: ((AggregateOperator) nextLevel).getFunctions()) {
          if(function.getParameters() == null)
            continue;
          expressions.addAll(function.getParameters().getExpressions());
        }
        
        
        if(CollectionUtils.isEmpty(expressions)) {
          pushDownLevel = getPushDownLevel(nextLevel, projectNode);
          continue;
        }
        
        final List<Pair<Integer, Integer>> requiredSchema = new ArrayList<>();
        for (final Expression expr : expressions) {
          RequiredBuiltSchema requiredBuiltSchema = new RequiredBuiltSchema();
          requiredSchema
              .addAll(requiredBuiltSchema.getRequiredSchema(expr, nextLevel));
        } // for

        // update
        if (!projectBuiltSchema.containsAll(requiredSchema)) {
          SchemaUtils.updateProjectNodeSchema(projectNode, projectBuiltSchema,
              requiredSchema);
        }

        pushDownLevel = getPushDownLevel(nextLevel, projectNode);

      } else if (nextLevel instanceof GroupByOperator) {

        // when selection see what is required by the operator is satisfied by
        // the projection schema or not
        final List<Pair<Integer, Integer>> requiredSchema = new ArrayList<>();
        for (final Expression expr : ((GroupByOperator) nextLevel)
            .getGroupByColumnReferences()) {
          RequiredBuiltSchema requiredBuiltSchema = new RequiredBuiltSchema();
          requiredSchema
              .addAll(requiredBuiltSchema.getRequiredSchema(expr, nextLevel));
        } // for

        if (!projectBuiltSchema.containsAll(requiredSchema)) {
          SchemaUtils.updateProjectNodeSchema(projectNode, projectBuiltSchema,
              requiredSchema);
        }
        pushDownLevel = getPushDownLevel(nextLevel, projectNode);

      } else if (projectBuiltSchema.containsAll(nextLevel.getBuiltSchema())) {
        // for any other node match the schemas

        pushDownLevel = getPushDownLevel(nextLevel, projectNode);
      } else {
        pushDownLevel = nextLevel;
        break;
      }
    }
    return pushDownLevel;
  }

  public static Node removeNode(Node root, Node remove) {
    // no-op
    if (root == remove && remove instanceof BinaryOperator) {
      return root;
    }

    // replace root
    if (root == remove) {
      Node child = remove.getChildren().get(0);
      child.setParent(null);
      root = child;
      return root;
    }

    // find and replace
    Node parent = remove.getParent();
    List<Node> children = remove.getChildren();
    int idx = 0;
    for (Node child : parent.getChildren()) {
      if (child == remove) {
        break;
      }
      idx++;
    }

    parent.getChildren().remove(idx);
    parent.getChildren().addAll(children);

    for (Node child : children) {
      child.setParent(parent);
    }

    remove.setChildren(null);
    remove.setParent(null);
    remove = null;

    return root;
  }

}
