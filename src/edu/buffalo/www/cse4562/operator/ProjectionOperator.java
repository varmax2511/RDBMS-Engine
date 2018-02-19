package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

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
 * The {@link ProjectionOperator} invokes a {@link OperatorExpressionVisitor}
 * and pass a tuple and an expression to it for processing. 
 *
 * TODO: Support for Alias
 * TODO: Support for processing of AllTableColumns
 *
 * </pre>
 *
 * @author varunjai
 *
 */
public class ProjectionOperator implements Operator {

  /**
   * flag to indicate that columns have been requested, hence no projection
   */
  private boolean allColFlag = false;
  /**
   * List of {@link AllTableColumns}
   */
  private List<AllTableColumns> allTableColumns;
  /**
   * List of {@link SelectExpressionItem}
   */
  private List<SelectExpressionItem> selectExpressionItems;

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

    final OperatorVisitor opVisitor = new OperatorExpressionVisitor();
    // iterate tuples in the collection
    for (final Tuple tuple : tuples) {
      // process expressions
      final List<ColumnCell> columnCells = new ArrayList<>();
      for (final SelectExpressionItem expressionItem : selectExpressionItems) {
        final ColumnCell columnCell = opVisitor.getValue(tuple,
            expressionItem.getExpression());
        if (null != columnCell) {
          columnCells.add(columnCell);
        }
      } // for

      projectOutput.add(new Tuple(columnCells));
    } // for

    return projectOutput;
  }

  public boolean isAllColFlag() {
    return this.allColFlag;
  }

  public void setAllColFlag(boolean allColFlag) {
    this.allColFlag = allColFlag;
  }

  public List<AllTableColumns> getAllTableColumns() {
    return this.allTableColumns;
  }

  public void setAllTableColumns(List<AllTableColumns> allTableColumns) {
    this.allTableColumns = allTableColumns;
  }

  public List<SelectExpressionItem> getSelectExpressionItems() {
    return this.selectExpressionItems;
  }

  public void setSelectExpressionItems(
      List<SelectExpressionItem> selectExpressionItems) {
    this.selectExpressionItems = selectExpressionItems;
  }

  public void addSelectExpressionItems(
      SelectExpressionItem selectExpressionItems) {
    if (this.selectExpressionItems == null) {
      this.selectExpressionItems = new ArrayList<>();
    }

    this.selectExpressionItems.add(selectExpressionItems);
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
