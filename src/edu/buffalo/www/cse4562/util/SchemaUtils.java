package edu.buffalo.www.cse4562.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

public class SchemaUtils {

  public static boolean expressionMatch(Expression expression, Integer tableId,
      Integer columnId) {

    // Like R.A * S.B AS C or A + B AS C
    if (!(expression instanceof Column)) {
      // if match is found
      if (SchemaManager.getColumnNameById(tableId, columnId)
          .equals(expression.toString())) {
        return true;
      } else {
        return false;
      }

    }

    // expression is like A AS C
    final Column column = (Column) expression;
    if (column.getTable() == null
        || StringUtils.isBlank(column.getTable().getName())) {

      if (SchemaManager.getColumnNameById(tableId, columnId)
          .equals(column.getColumnName())) {
        return true;
      } // if
    } // if

    // Exact Match R.A AS C
    if (column.getTable().getName().equals(SchemaManager.getTableName(tableId))
        && column.getColumnName()
            .equals(SchemaManager.getColumnNameById(tableId, columnId))) {
      return true;
    }

    return false;
  }

  public static void updateProjectNodeSchema(ProjectionOperator projectNode,
      final List<Pair<Integer, Integer>> projectBuiltSchema,
      final List<Pair<Integer, Integer>> requiredSchema) {
    final Set<Pair<Integer, Integer>> project = new HashSet<>();
    project.addAll(projectBuiltSchema);
    final Set<Pair<Integer, Integer>> child = new HashSet<>();
    child.addAll(requiredSchema);
  
    child.removeAll(project);
  
    for (final Pair<Integer, Integer> pair : child) {
      final Column expr = new Column();
      expr.setTable(new Table(SchemaManager.getTableName(pair.getKey())));
      expr.setColumnName(
          SchemaManager.getColumnNameById(pair.getKey(), pair.getValue()));
      final SelectExpressionItem exprItem = new SelectExpressionItem();
  
      exprItem.setExpression(expr);
      projectNode.addSelectExpressionItems(exprItem);
    } // for
  
    projectNode.getBuiltSchema();
  }

  public static boolean isTableNameColNameAvailable(Column column) {
  
    if (column == null) {
      throw new IllegalArgumentException("Null expression");
    }
  
    if (column.getTable() == null
        || StringUtils.isBlank(column.getTable().getName())) {
      return false;
    }
  
    return true;
  }

}
