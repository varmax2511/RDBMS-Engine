package edu.buffalo.www.cse4562.operator.visitor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.Evaluator;
import edu.buffalo.www.cse4562.util.StringUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * <pre>
 * Each operator will pass a tuple to the visitor with an
 * {@link SelectExpressionItem} that it processes and returns the value for that
 * expression.
 *
 * Here it will also have an instance of a Map of column name to
 * {@link ColumnCell}.
 *
 * An operator will invoke the visitor with expression and tuple, visitor will
 * process it be it single column expression or a binary expression, for binary
 * expression process it via the {@link Evaluator} and return result as a
 * {@link ColumnCell}.
 *
 * add them to map for expressions which need to be projected, not for
 * expressions in where clause, since A + B can be part of projection as well,
 * we are including it.
 *
 * TODO: Presently evaluator searches column values only by column name but we
 * need to make search based on both column name and table name. Example:
 * Queries like S.A == X.A Here column name is same but tables are different.
 *
 * Cases where no table name is available, e.g SELECT A, B FROM R;
 *
 *
 * Approach 1:
 * Create a map with Key - {@link ColumnKey} --> Value : ColumnCell
 * The Column key holds the table name and the column name passed. It overrides
 * the hashcode and equals method and generates same hashcode for other keys
 * with same table name and column name.
 *
 * E.g SELECT A + B from R;
 * Two column keys, one for A and one for B
 * Here column will contain only column name and tablename will be null
 *
 * SELECT R.A + R.B from R;
 * Two column keys, one for R.A and one for R.B
 * Here table name will not be null both columnKey
 *
 * SELECT * FROM R, S WHERE R.A = S.A
 * Two column keys, one for R.A and one for S.A
 * The keys will generate separate hash based and hence no overwrite will happen.
 *
 * TODO: identify mapping a String to date like '1994-01-01'
 * </pre>
 *
 *
 *
 * @author varunjai
 *
 */
public class OperatorExpressionVisitor
    implements
      ExpressionVisitor,
      OperatorVisitor {

  public Evaluator evaluator;
  public Tuple currentTuple;
  private Map<ColumnKey, ColumnCell> column2ColumnCell = new HashMap<>();
  private ColumnCell outputColumnCell;

  /**
   *
   */
  public OperatorExpressionVisitor() {
    this.evaluator = new Evaluator();
  }

  @Override
  public void visit(NullValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Function function) {
    // null check
    if (null == function) {
      return;
    }
    //:TODO handle for * which means no parameter
    if (function.getParameters() != null) {
      if (function.getParameters().getExpressions() != null) {
        for (Expression expression : function.getParameters().getExpressions()) {
          expression.accept(this);
        }
      }
    }
  }

  @Override
  public void visit(InverseExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(JdbcParameter arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(DoubleValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(LongValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(DateValue arg0) {
    //System.out.println("date value");

  }

  @Override
  public void visit(TimeValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(TimestampValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BooleanValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(StringValue input) {
   
  }

  @Override
  public void visit(Addition addition) {

    // null check
    if (null == addition) {
      return;
    }
    // evaluate LHS and RHS
    addition.getLeftExpression().accept(this);
    addition.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(addition);
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(addition.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);
  }

  @Override
  public void visit(Division arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Multiplication multiplication) {
 // null check
    if (null == multiplication) {
      return;
    }
    // evaluate LHS and RHS
    multiplication.getLeftExpression().accept(this);
    multiplication.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(multiplication);
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    this.outputColumnCell = new ColumnCell(cellValue);
  }

  @Override
  public void visit(Subtraction arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(AndExpression andExpression) {
    andExpression.getLeftExpression().accept(this);
    andExpression.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(andExpression);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(andExpression.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(OrExpression orExpression) {

    orExpression.getLeftExpression().accept(this);
    orExpression.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(orExpression);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(orExpression.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);
  }

  @Override
  public void visit(Between arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(EqualsTo equalsTo) {

    equalsTo.getLeftExpression().accept(this);
    equalsTo.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(equalsTo);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(equalsTo.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);
  }

  @Override
  public void visit(GreaterThan greaterThan) {
    greaterThan.getLeftExpression().accept(this);
    greaterThan.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(greaterThan);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(greaterThan.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(GreaterThanEquals greaterThanEquals) {
    greaterThanEquals.getLeftExpression().accept(this);
    greaterThanEquals.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(greaterThanEquals);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    // this.column2ColumnCell.put(greaterThanEquals.getStringExpression(),
    // new ColumnCell(cellValue));
    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(InExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(IsNullExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(LikeExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(MinorThan minorThan) {

    minorThan.getLeftExpression().accept(this);
    minorThan.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(minorThan);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(MinorThanEquals minorThanEquals) {
    minorThanEquals.getLeftExpression().accept(this);
    minorThanEquals.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(minorThanEquals);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(NotEqualsTo notEqualsTo) {

    notEqualsTo.getLeftExpression().accept(this);
    notEqualsTo.getRightExpression().accept(this);

    PrimitiveValue cellValue = null;
    // pass column values map to evaluator for processing.
    evaluator.setColumn2ColumnCell(this.column2ColumnCell);
    try {
      cellValue = evaluator.eval(notEqualsTo);
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // creating new instance, as we will be destroying map and wasn't sure if
    // it will destroy the object as well
    this.outputColumnCell = new ColumnCell(cellValue);

  }

  @Override
  public void visit(Column column) {

    // to flush out any previous entry, and set null if not record is found for
    // a column.
    // e.g columns B and C are required for a table which only has entries of
    // columns A and B
    this.outputColumnCell = null;

    // return null
    if (this.currentTuple.isEmpty()) {
      return;
    }

    for (final ColumnCell columnCell : this.currentTuple.getColumnCells()) {

      /*if(columnCell == null){
        System.out.println("i am null");
      }*/
      final String tableName = column.getTable() != null
          ? column.getTable().getName()
          : null;

      Integer tableId = SchemaManager.getTableId(tableName);

      /*
       * Check all negative cases.
       */
      if (tableId == null) {

        /*
         * If table id is null, then check only column id. To get column id, we
         * will use the column cell table id itself. E.g. SELECT A, B FROM R;
         * Here no table id will be present
         */
        tableId = columnCell.getTableId();

        if (SchemaManager.getColumnIdByTableId(tableId,
            column.getColumnName()) != columnCell.getColumnId()) {
          continue;
        } // if

      } else {

        // If table id is not null, check with column
        // cell table id and column id.
        if (tableId != columnCell.getTableId()) {
          continue;
        }

        if (SchemaManager.getColumnIdByTableId(tableId,
            column.getColumnName()) != columnCell.getColumnId()) {
          continue;
        }

      } // if

      // create a key based on data in column received, if table id is null
      // then
      // store as is. This is because later the column key will be matched
      // with
      // column key hash generated in Evaluator

      final ColumnKey columnKey = new ColumnKey(column.getColumnName(),
          tableName);
      this.column2ColumnCell.put(columnKey, columnCell);
      this.outputColumnCell = columnCell;
      // since we got the column we are looking for, break
      break;
    } // for
  }

  @Override
  public void visit(SubSelect arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(CaseExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(WhenClause arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(ExistsExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(AllComparisonExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(AnyComparisonExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Concat arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Matches arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BitwiseAnd arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BitwiseOr arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(BitwiseXor arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public ColumnCell getValue(Tuple tuple, Expression expression) {
    this.currentTuple = tuple;

    expression.accept(this);
    return this.outputColumnCell;
  }

  /**
   *
   * @author varunjai
   *
   */
  public static class ColumnKey implements Comparable<ColumnKey> {
    private final String columnName;
    private final String tableName;

    /**
     *
     * @param columnName
     * @param tableName
     */
    public ColumnKey(String columnName, String tableName) {
      Validate.notBlank(columnName);
      this.columnName = columnName;
      this.tableName = tableName;
    }

    @Override
    public int compareTo(ColumnKey other) {
      // if we have no table name for any key
      // we cannot get the column id
      // make stupid string length comparison
      if (StringUtils.isBlank(this.tableName)
          && StringUtils.isBlank(other.tableName)) {
        if (this.columnName.equals(other.columnName)) {
          return 0;
        }

        if (this.columnName.length() > other.columnName.length()) {
          return 1;
        } else {
          return -1;
        }

      } // if

      // if we have table name for both
      if (!StringUtils.isBlank(this.tableName)
          && !StringUtils.isBlank(other.tableName)) {

        if (this.tableName.equals(other.tableName)
            && this.columnName.equals(other.columnName)) {
          
          return 0;
        }

        if (this.tableName.equals(other.tableName)) {
          if (SchemaManager.getColumnIdByTableId(
              SchemaManager.getTableId(this.tableName),
              this.columnName) > SchemaManager.getColumnIdByTableId(
                  SchemaManager.getTableId(other.tableName),
                  other.columnName)) {
            return 1;
          } else {
            return -1;
          }

        } // if
      } // if

      // if we have table name for one
      if (StringUtils.isBlank(this.tableName)) {
        return -1;
      } else {
        return 1;
      }

    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + columnName.hashCode();
      result = prime * result
          + ((StringUtils.isBlank(tableName)) ? 0 : tableName.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final ColumnKey other = (ColumnKey) obj;
      if (StringUtils.isBlank(columnName)) {
        if (!StringUtils.isBlank(other.columnName)) {
          return false;
        }
      } else if (!columnName.equals(other.columnName)) {
        return false;
      }
      if (StringUtils.isBlank(tableName)) {
        if (!StringUtils.isBlank(other.tableName)) {
          return false;
        }
      } else if (!tableName.equals(other.tableName)) {
        return false;
      }
      return true;
    }

    public String getColumnName() {
      return columnName;
    }

    public String getTableName() {
      return tableName;
    }
  }
}
