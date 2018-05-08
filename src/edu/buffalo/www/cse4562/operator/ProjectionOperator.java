package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.StringUtils;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
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
 *
 * TODO: Support for processing of AllTableColumns
 *
 * </pre>
 *
 * @author varunjai
 *
 */
public class ProjectionOperator extends Node implements UnaryOperator {

  /**
   * flag to indicate that columns have been requested, hence no projection
   */
  private boolean allColFlag = false;
  /**
   * List of {@link AllTableColumns}
   */
  private List<AllTableColumns> allTableColumns = new ArrayList<>();
  /**
   * List of {@link SelectExpressionItem}
   */
  private List<SelectExpressionItem> selectExpressionItems = new ArrayList<>();

  private Map<Pair<Integer, Integer>, SelectExpressionItem> pair2SelectExprItem = new HashMap<>();

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws Throwable {

    final List<Tuple> projectOutput = new ArrayList<>();
    if (tuples == null || tuples.size() == 0) {
      return projectOutput;
    }

    // unary operator, interested in only the first collection
    final Collection<Tuple> tupleRecords = tuples.iterator().next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return projectOutput;
    }

    // if user requested all columns for all tables
    if (this.allColFlag) {
      return tupleRecords;
    }

    final OperatorVisitor opVisitor = new OperatorExpressionVisitor();
    // iterate tuples in the collection
    for (final Tuple tuple : tupleRecords) {
      // process expressions
      final List<ColumnCell> columnCells = new ArrayList<>();
      int cnt = 0;
      for (final SelectExpressionItem expressionItem : selectExpressionItems) {

        if (expressionItem.getExpression() instanceof Function) {
          // search for a match in the tuple column cells
          for (final ColumnCell columnCell : tuple.getColumnCells()) {

            // if not matching
            if (columnCell == null || !SchemaManager
                .getColumnNameById(columnCell.getTableId(),
                    columnCell.getColumnId())
                .equals(expressionItem.getExpression().toString())) {

              continue;
            }

            // found match
            final ColumnCell cCell = new ColumnCell(columnCell.getCellValue());
            cCell.setColumnId(builtSchema.get(cnt).getValue());
            cCell.setTableId(builtSchema.get(cnt).getKey());

            // add to list, move over to next expression
            columnCells.add(cCell);
            break;

          } // for

          // increment count at all times
          cnt++;
          continue;
        } // for

        // TODO later in the project if the renaming is required as output this
        // can be used by setting as a column name
        // for now we don't need to output the column name or the rename(alias)
        // String alias = expressionItem.getAlias();

        final ColumnCell columnCell = opVisitor.getValue(tuple,
            expressionItem.getExpression());

        if (null != columnCell) {
          ColumnCell cCell = new ColumnCell(columnCell.getCellValue());
          cCell.setColumnId(builtSchema.get(cnt).getValue());
          cCell.setTableId(builtSchema.get(cnt).getKey());

          columnCells.add(cCell);
        }
        cnt++;
      } // for

      projectOutput.add(new Tuple(columnCells));
    } // for

    return projectOutput;
  }

  /**
   * Find the {@link AllTableColumns} and add their expanded form i.e.
   * table.Column in the {@link #selectExpressionItems}
   */
  private void findAllTableColumns() {
    // null check
    if (CollectionUtils.isEmpty(allTableColumns)) {
      return;
    }

    int cnt = 0;
    for (final AllTableColumns tableColumns : allTableColumns) {

      final TableSchema tableSchema = SchemaManager
          .getTableSchema(tableColumns.getTable().getName());

      for (final ColumnDefinition colDef : tableSchema.getColumnDefinitions()) {
        final SelectExpressionItem selectExprItem = new SelectExpressionItem();
        final Column column = new Column();
        column.setColumnName(colDef.getColumnName());
        column.setTable(tableColumns.getTable());
        selectExprItem.setExpression(column);
        // TODO: hack assuming Table.* is at beginning
        this.selectExpressionItems.add(cnt++, selectExprItem);
      } // for
    } // for

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

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {

    // if already set
    if (CollectionUtils.isEmpty(builtSchema)) {
      findAllTableColumns();
    }

    builtSchema = new ArrayList<>();
    // invoke child schema for schema manager updation
    final List<Pair<Integer, Integer>> childSchema = getChildren().get(0).getBuiltSchema();

    // expr like R.*
    // TODO: Rethink deep once doing projection pushdown
   // findAllTableColumns();

    // if expression is SELECT * FROM R,S
    if (this.allColFlag || CollectionUtils.isEmpty(selectExpressionItems)) {
      builtSchema = childSchema;
      return builtSchema;
    }

    // get schema based on expression items of project
    for (final SelectExpressionItem selectExprItem : this.selectExpressionItems) {

      // if not a column instance, process as expression
      if (!(selectExprItem.getExpression() instanceof Column)) {
        buildExpression(childSchema, selectExprItem);
        continue;
      }

      final Column column = (Column) selectExprItem.getExpression();

      // if no table name, get table id from child schema where the
      // column name matches
      if (StringUtils.isBlank(column.getTable().getName())) {

        buildNoTableSchema(childSchema, selectExprItem, column.getColumnName());
      } else {
        // if no column name
        buildWithCidTid(selectExprItem, column);
      }

    } // for

    return builtSchema;
  }

  /**
   *
   * @param selectExprItem
   * @param column
   * @return
   */
  private void buildWithCidTid(final SelectExpressionItem selectExprItem,
      final Column column) {
    final Integer tableId = SchemaManager
        .getTableId(column.getTable().getName());

    final Pair<Integer, Integer> pair = new Pair<Integer, Integer>(tableId,
        SchemaManager.getColumnIdByTableId(tableId, column.getColumnName()));
    builtSchema.add(pair);
    pair2SelectExprItem.put(pair, selectExprItem);

  }

  /**
   *
   * @param childSchema
   * @param selectExprItem
   * @param column
   */
  private void buildNoTableSchema(List<Pair<Integer, Integer>> childSchema,
      final SelectExpressionItem selectExprItem, final String columnName) {

    Pair<Integer, Integer> matchPair = null;
    for (final Pair<Integer, Integer> pair : childSchema) {
      // if matching
      if (!SchemaManager.getColumnNameById(pair.getKey(), pair.getValue())
          .equals(columnName)) {
        continue;
      }
      matchPair = pair;
    } // for

    // no matching pair - no-op
    if (null == matchPair) {
      return;
    }

    final Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
        matchPair.getKey(),
        SchemaManager.getColumnIdByTableId(matchPair.getKey(), columnName));
    builtSchema.add(pair);
    pair2SelectExprItem.put(pair, selectExprItem);

  }

  /**
   *
   * @param childSchema
   * @param selectExprItem
   * @param column
   */
  private void buildExpression(List<Pair<Integer, Integer>> childSchema,
      final SelectExpressionItem selectExprItem) {

    // no alias present
    // register expression as column
    // if not already registered
    if (SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
        selectExprItem.getExpression().toString()) == null) {
      SchemaManager.addColumnAliasToSchema(
          selectExprItem.getExpression().toString(),
          childSchema.get(0).getKey());
    }

    final Pair<Integer, Integer> pair = new Pair<Integer, Integer>(
        childSchema.get(0).getKey(),
        SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
            selectExprItem.getExpression().toString()));
    builtSchema.add(pair);
    pair2SelectExprItem.put(pair, selectExprItem);

    return;

  }
  
  public Map<Pair<Integer, Integer>, SelectExpressionItem> getPair2SelectExprItem() {
    return pair2SelectExprItem;
  }

  public void setPair2SelectExprItem(
      Map<Pair<Integer, Integer>, SelectExpressionItem> pair2SelectExprItem) {
    this.pair2SelectExprItem = pair2SelectExprItem;
  }

  public Node getDeepCopy() {
    ProjectionOperator node = new ProjectionOperator();

    List<Node> children = new ArrayList<>();
    node.setChildren(children);
    for (Node child : this.getChildren()) {
      // node.addChild(child);
    }

    // node.setParent(this.getParent());
    node.builtSchema = new ArrayList<>(this.builtSchema);
    node.pair2SelectExprItem = new HashMap<>(this.getPair2SelectExprItem());
    node.setAllColFlag(this.allColFlag);

    for (AllTableColumns allTableColumns : this.getAllTableColumns()) {
      node.addAllTableColumns(allTableColumns);
    }

    for (SelectExpressionItem selectExprItem : this
        .getSelectExpressionItems()) {
      node.addSelectExpressionItems(selectExprItem);
    } // for

    return node;
  }

}
