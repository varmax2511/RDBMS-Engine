/**
 * 
 */
package edu.buffalo.www.cse4562.aggregator;

import java.util.List;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;

/**
 * @author Sneha Mehta
 *
 */
public class MaxAggregate {

  private Function function;
  private PrimitiveValue max;

  public MaxAggregate(Function function) {
    this.function = function;
  }

  public ColumnCell getAggregate(List<Tuple> tupleRecords, Integer tableId) {
    OperatorVisitor opExpVisitor = new OperatorExpressionVisitor();
    for (Tuple tuple : tupleRecords) {
      final ColumnCell columnCell = opExpVisitor.getValue(tuple, function);
      try {

        if (max == null) {
          max = columnCell.getCellValue();
        } else if (columnCell.getCellValue() instanceof LongValue) {
          max = (max.toLong() < columnCell.getCellValue().toLong())
              ? columnCell.getCellValue()
              : max;
        } else if (columnCell.getCellValue() instanceof DoubleValue) {
          max = (max.toDouble() < columnCell.getCellValue().toDouble())
              ? columnCell.getCellValue()
              : max;
        } else if (columnCell.getCellValue() instanceof StringValue) {
          max = (max.toString()
              .compareTo(columnCell.getCellValue().toString()) < 0)
                  ? columnCell.getCellValue()
                  : max;
        } else {
          DateValue maxDate = (DateValue) max;
          DateValue resultDate = (DateValue) columnCell.getCellValue();
          max = (maxDate.getValue().getTime() < resultDate.getValue().getTime())
              ? resultDate
              : max;
        }
      } catch (InvalidPrimitive e) {
        System.err.println("Invalid primitive for MAX: " + max.getType());
      }
    }
    //final Tuple tuple = tupleRecords.get(0);
    ColumnCell cCell = new ColumnCell(max);
    cCell.setTableId(tableId);
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(), function.toString()));


    //tuple.getColumnCells().add(cCell);
    max=null;
    return cCell;
  }

}
