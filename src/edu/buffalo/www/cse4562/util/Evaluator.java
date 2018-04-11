package edu.buffalo.www.cse4562.util;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;

import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor.ColumnKey;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
/**
 * Evaluator class to evaluate expression operation.
 * 
 * @author varunjai
 *
 */
public class Evaluator extends Eval {

  private static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
  /**
   * This is a mapping of the column name to the {@link ColumnCell} value used
   * by {@link #eval(Column)} to lookup the corresponding {@link PrimitiveValue}
   * for the column.
   */
  private Map<ColumnKey, ColumnCell> column2ColumnCell = new TreeMap<>();
  final SimpleDateFormat dateFormat = new SimpleDateFormat(
      DATE_FORMAT_YYYY_MM_DD);

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
   * Observed that date value passed in format 'yyyy-MM-dd' is read as String by
   * expression visitor, so this a hack where I check each String value passed
   * in the evaluation expression for being a date.
   */
  @Override
  public PrimitiveValue eval(StringValue v) {

    try {
      dateFormat.parse(v.getValue());
      return new DateValue(v.getValue());
    } catch (ParseException e) {
      // no-op
    }

    return super.eval(v);
  }

//  @Override
//  public PrimitiveValue eval(Function function) throws SQLException {
//    // :TODO
//    List<Expression> expressions= function.getParameters().getExpressions();
//    for(Expression exp:expressions) {
//      exp.accept(null);
//    }
//    final ColumnCell columnCell = new ColumnCell();
//    columnCell.setCellValue(
//        Aggregator.getValue(function.getName(), function.getParameters()));
//    return columnCell.getCellValue();
//  }
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
