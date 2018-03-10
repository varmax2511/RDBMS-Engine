package edu.buffalo.www.cse4562.util;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor.ColumnKey;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.schema.Column;
/**
 * Evaluator class to evaluate expression operation.
 * 
 * @author varunjai
 *
 */
public class Evaluator extends Eval {

  /**
   * This is a mapping of the column name to the {@link ColumnCell} value used
   * by {@link #eval(Column)} to lookup the corresponding {@link PrimitiveValue}
   * for the column.
   */
  private Map<ColumnKey, ColumnCell> column2ColumnCell = new TreeMap<>();

  @Override
  public PrimitiveValue eval(Column column) throws SQLException {
    // lookup
    // issue - here we don't have column id, for query w/o table name, we cannot
    // get column id
    String tableName = column.getTable() != null
        ? column.getTable().getName()
        : null;
        
    ColumnKey columnKey = new ColumnKey(column.getColumnName(), tableName);   
    final ColumnCell cell = this.column2ColumnCell.get(columnKey);
    return cell.getCellValue();
  }

  /**
   * {@link #column2ColumnCell}
   * 
   * @return
   */
  public Map<ColumnKey, ColumnCell> getColumn2ColumnCell() {
    return column2ColumnCell;
  }
  /**
   * {@link #column2ColumnCell}
   * 
   * @param column2ColumnCell
   */
  public void setColumn2ColumnCell(
      Map<ColumnKey, ColumnCell> column2ColumnCell) {
    this.column2ColumnCell = column2ColumnCell;
  }

}
