package edu.buffalo.www.cse4562.util;

import edu.buffalo.www.cse4562.model.SchemaManager;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

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

}
