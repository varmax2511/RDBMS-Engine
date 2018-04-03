/**
 * 
 */
package edu.buffalo.www.cse4562.util;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha
 *
 */
public class RequiredBuiltSchema {
  
  public static List<Pair<Integer, Integer>> getRequiredSchema(
      Expression expression) {
    final List<Pair<Integer, Integer>> schema = new ArrayList<>();

    // since all where conditions will be a binary expression
    if (expression instanceof BinaryExpression) {
      if (((BinaryExpression) expression)
          .getLeftExpression() instanceof Column) {
        addToSelectSchema(
            (Column) ((BinaryExpression) expression).getLeftExpression(),
            schema);
      }
      if (((BinaryExpression) expression)
          .getRightExpression() instanceof Column) {
        addToSelectSchema(
            (Column) ((BinaryExpression) expression).getRightExpression(),
            schema);
      }
    }

    return schema;
  }
  
  /**
   * @param column
   * @param selectSchema
   *
   *          Adds all tableId,columnId pairs to the selectSchema
   */
  private static void addToSelectSchema(Column column,
      List<Pair<Integer, Integer>> schema) {
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
    schema.add(new Pair<Integer, Integer>(tableId, columnId));

  }

}
