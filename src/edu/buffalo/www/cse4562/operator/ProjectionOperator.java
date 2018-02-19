package edu.buffalo.www.cse4562.operator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.eval.Eval;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
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
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;

/**
 * <pre>
 * This {@link Operator} does the projection of selected columns.
 *
 * A projection operator is instantiated for a query in such a way, that its
 * recorded 
 * 1. All columns need to be displayed 
 * 2. All columns for some tables need to be displayed 
 * 3. The list of expressions that need to be displayed.
 * 
 * A projection is also an {@link ExpressionVisitor} and visits all the
 * expressions passed to it. This helps in processing expressions related to
 * binary operations as well.
 * 
 * column2ColumnCell It maintains internal variables such as a mapping of every
 * {@link Column} to the {@link ColumnCell} that was observed on visiting an
 * expression. The columns which are visited only for doing binary operation are
 * removed by the {@link #eval(Addition)}, provided they were not required by expression.
 * 
 * The {@link ProjectionOperator} is also an {@link Eval} and implements {@link #eval(Column)}
 * where it returns the {@link PrimitiveValue} for the {@link Column} from the
 * {@link #column2ColumnCell} mapping.
 * 
 * Once all the expressions, alltableColumns entries have been visited the
 * operator creates a tuple from the values in {@link #column2ColumnCell}.
 * {@link #column2ColumnCell} is a {@link TreeMap} to retain the natural order
 * of the occurrence of columns, since the number of columns is not expected to
 * be large (Gokhan), re-balancing cost shouldn't be high.
 * 
 * TODO: Prevent removal of columns during evaluation which are also part of selection.
 * TODO: Support for Alias
 * TODO: Support for processing of AllTableColumns
 * 
 * </pre>
 * 
 * @author varunjai
 *
 */
public class ProjectionOperator extends Eval
    implements
      Operator,
      ExpressionVisitor {

  private Map<String, ColumnCell> column2ColumnCell = new TreeMap<>();
  /**
   * flag to indicate that columns have been requested, hence no projection
   */
  private boolean allColFlag = false;
  private List<AllTableColumns> allTableColumns;
  private List<SelectExpressionItem> expressions;
  private Tuple currentTuple = null;
  /**
   * To be used only when the query involves only one expression and that is
   * display all columns SELECT * FROM R;
   */
  public ProjectionOperator() {

  }

  public ProjectionOperator(List<AllTableColumns> allTableColumns,
      List<SelectExpressionItem> expressions) {

    this.allTableColumns = allTableColumns;
    this.expressions = expressions;
  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples) throws Throwable {

    final List<Tuple> projectOutput = new ArrayList<>();
    // empty check
    if (CollectionUtils.areTuplesEmpty(tuples)) {
      return projectOutput;
    }

    // if user requested all columns for all tables
    if (this.allColFlag) {
      return tuples;
    }

    // iterate tuples in the collection
    for (final Tuple tuple : tuples) {
      this.currentTuple = tuple;
      // process expressions
      for (final SelectExpressionItem expressionItem : expressions) {
        expressionItem.getExpression().accept(this);
      }

      projectOutput.add(new Tuple(column2ColumnCell.values()));
    } // for

    // destroy
    this.currentTuple = null;
    this.column2ColumnCell = new TreeMap<>();
    return projectOutput;
  }

  public boolean isAllColFlag() {
    return this.allColFlag;
  }

  public void setAllColFlag(boolean allColFlag) {
    this.allColFlag = allColFlag;
  }

  @Override
  public void visit(NullValue arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Function arg0) {
    // TODO Auto-generated method stub

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
    // TODO Auto-generated method stub

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
  public void visit(StringValue arg0) {
    // TODO Auto-generated method stub

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
    try {
      cellValue = eval(addition);
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // if null, no-op
    if (null == cellValue) {
      return;
    }

    // TODO: how to set aliases???
    this.column2ColumnCell.put(addition.getStringExpression(),
        new ColumnCell(cellValue));
  }

  @Override
  public void visit(Division arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Multiplication arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Subtraction arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(AndExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(OrExpression arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Between arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(EqualsTo arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(GreaterThan arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(GreaterThanEquals arg0) {
    // TODO Auto-generated method stub

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
  public void visit(MinorThan arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(MinorThanEquals arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(NotEqualsTo arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Column column) {

    // return null
    if (this.currentTuple.isEmpty()) {
      return;
    }

    for (final ColumnCell columnCell : this.currentTuple.getColumnCells()) {

      Integer tableId = SchemaManager.getTableId(column.getColumnName());
      tableId = tableId == null ? columnCell.getTableId() : tableId;

      if (SchemaManager.getColumnIdByTableId(tableId,
          column.getColumnName()) == columnCell.getColumnId()) {
        this.column2ColumnCell.put(column.getWholeColumnName(), columnCell);
      } // if
    }
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
  public PrimitiveValue eval(Column column) throws SQLException {

    final ColumnCell cell = this.column2ColumnCell.get(column.getColumnName());
    // TODO: remove column only if not in selection
    this.column2ColumnCell.remove(column.getColumnName());
    return cell.getCellValue();
  }

  public List<AllTableColumns> getAllTableColumns() {
    return this.allTableColumns;
  }

  public void setAllTableColumns(List<AllTableColumns> allTableColumns) {
    this.allTableColumns = allTableColumns;
  }

  public List<SelectExpressionItem> getExpressions() {
    return this.expressions;
  }

  public void setExpressions(List<SelectExpressionItem> expressions) {
    this.expressions = expressions;
  }

  public void addExpression(SelectExpressionItem expression) {
    if (this.expressions == null) {
      this.expressions = new ArrayList<>();
    }

    this.expressions.add(expression);
  }

  /**
   * 
   * @param allTableColumns
   */
  public void addAllTableColumns(AllTableColumns allTableColumns) {
    if (this.allTableColumns == null) {
      this.allTableColumns = new ArrayList<>();
    }

    this.allTableColumns.add(allTableColumns);
  }
}
