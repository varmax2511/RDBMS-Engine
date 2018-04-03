/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;

/**
 * @author Sneha Mehta
 *
 */
public class PushDownProjection {

  /**
   * @param root
   */
  public static void pushDownProject(Node root) {
    final List<ProjectionOperator> allProjectNodes = getAllProjectNodes(root);

    for (final ProjectionOperator projectNode : allProjectNodes) {
      // No pointing pushing it down for select *
      if (!projectNode.isAllColFlag()) {
        verifyAndPushDownProject(projectNode);
      }
    }
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
  private static void verifyAndPushDownProject(Node projectNode) {
    final List<Pair<Integer, Integer>> projectSchema = projectNode
        .getBuiltSchema();
    final Node pushDownLevel = getPushDownLevel(projectNode, projectSchema);

    // if pushlevel is same as select node, no optimization can be done
    if (pushDownLevel == projectNode) {
      return;
    }

    // TODO push down should be inserted in case of aggregates like sum(c) and
    // not pushed down

    // push down select to the appropriate level
    Optimizer.pushDown(projectNode, pushDownLevel);

  }

  /**
   * @param node
   * @param projectSchema
   * @return
   */
  private static Node getPushDownLevel(Node node,
      List<Pair<Integer, Integer>> projectSchema) {
    Node pushDownLevel = node;

    if (CollectionUtils.isEmpty(projectSchema)) {
      return pushDownLevel;
    }

    for (final Node nextLevel : node.getChildren()) {

      final List<Pair<Integer, Integer>> builtSchema = nextLevel
          .getBuiltSchema();

      for (final Node nextLevel2 : nextLevel.getChildren()) {
        final List<Pair<Integer, Integer>> builtSchema2 = nextLevel2
            .getBuiltSchema();
        // If the projection schema contains the schema required for other
        // operators above then it can be pushed down
        // Don't go beyond Scanner
        if (projectSchema.containsAll(builtSchema)
            && builtSchema2.containsAll(projectSchema)) {
          pushDownLevel = getPushDownLevel(nextLevel, projectSchema);
        }
      } // for
    } // for
    return pushDownLevel;
  }

}
