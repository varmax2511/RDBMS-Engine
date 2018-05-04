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
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;

/**
 * @author Sneha Mehta
 *
 */
public class AverageAggregate {


  private DoubleValue avg;
  protected Boolean resultTypeLong;
  protected Function function;
  protected LongValue longSum;
  protected DoubleValue doubleSum;

  public AverageAggregate(Function function) {
    this.function = function;
    avg = new DoubleValue(0);
    longSum = new LongValue(0);
    doubleSum = new DoubleValue(0);
  }
  public ColumnCell getAggregate(List<Tuple> tupleRecords, Integer tableId) {
    OperatorVisitor opExpVisitor = new OperatorExpressionVisitor();
    for (Tuple tuple : tupleRecords) {
      final ColumnCell columnCell = opExpVisitor.getValue(tuple, function);
      resultTypeLong = (resultTypeLong == null)
          ? (columnCell.getCellValue() instanceof LongValue) ? true : false
          : resultTypeLong;
      try {

        if (resultTypeLong) {
          longSum.setValue(
              longSum.getValue() + columnCell.getCellValue().toLong());
        } else {
          doubleSum.setValue(
              doubleSum.getValue() + columnCell.getCellValue().toDouble());
        }
      } catch (InvalidPrimitive e) {
        System.err.println("Invalid primitive for avg: "
            + columnCell.getCellValue().getType());
      }
    }
    avg.setValue((resultTypeLong ? longSum.toDouble() : doubleSum.toDouble())
        / tupleRecords.size());

    ColumnCell cCell = new ColumnCell(avg);
    cCell.setTableId(tableId);
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(),
        function.toString()));

    avg = new DoubleValue(0);

    longSum = new LongValue(0);
    doubleSum = new DoubleValue(0);
    return cCell;
  }

}
