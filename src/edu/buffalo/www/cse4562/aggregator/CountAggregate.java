/**
 * 
 */
package edu.buffalo.www.cse4562.aggregator;

import java.util.List;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;

/**
 * @author Sneha Mehta
 *
 */
public class CountAggregate {

  private Function function;
  private LongValue count;

  public CountAggregate(Function function) {
    this.function = function;
    count = new LongValue(0);
  }

  public ColumnCell getAggregate(List<Tuple> tupleRecords,Integer tableId) {
    count.setValue(tupleRecords.size());

    //final Tuple tuple = tupleRecords.get(0);
    ColumnCell cCell = new ColumnCell(count);
    cCell.setTableId(tableId);
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(), function.toString()));

    //tuple.getColumnCells().add(cCell);
    count = new LongValue(0);
    return cCell;
  }

}
