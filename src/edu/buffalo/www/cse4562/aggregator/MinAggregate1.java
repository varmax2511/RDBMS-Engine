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
 * @author Sneha
 *
 */
public class MinAggregate1 {

  private Function function;
  private PrimitiveValue min;

  public MinAggregate1(Function function) {
    this.function = function;
  }

  //@Override
  public ColumnCell getAggregate(List<Tuple> tupleRecords, Integer tableId) {
    OperatorVisitor opExpVisitor = new OperatorExpressionVisitor();
    for (Tuple tuple : tupleRecords) {
      final ColumnCell columnCell = opExpVisitor.getValue(tuple, function);
      try {

        if (min == null) {
          min = columnCell.getCellValue();
        } else if (columnCell.getCellValue() instanceof LongValue) {
          min = (min.toLong() > columnCell.getCellValue().toLong())
              ? columnCell.getCellValue()
              : min;
        } else if (columnCell.getCellValue() instanceof DoubleValue) {
          min = (min.toDouble() > columnCell.getCellValue().toDouble())
              ? columnCell.getCellValue()
              : min;
        } else if (columnCell.getCellValue() instanceof StringValue) {
          min = (min.toString()
              .compareTo(columnCell.getCellValue().toString()) > 0)
                  ? columnCell.getCellValue()
                  : min;
        } else {
          DateValue minDate = (DateValue) min;
          DateValue resultDate = (DateValue) columnCell.getCellValue();
          min = (minDate.getValue().getTime() > resultDate.getValue().getTime())
              ? resultDate
              : min;
        }
      } catch (InvalidPrimitive e) {
        System.err.println("Invalid primitive for MIN: " + min.getType());
      }
    }
    //final Tuple tuple = tupleRecords.get(0);
    ColumnCell cCell = new ColumnCell(min);
    cCell.setTableId(tableId);
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(), function.toString()));

    //tuple.getColumnCells().add(cCell);
    min=null;
    return cCell;
  }


}
