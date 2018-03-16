package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
  private List<AllTableColumns> allTableColumns;
  /**
   * List of {@link SelectExpressionItem}
   */
  private List<SelectExpressionItem> selectExpressionItems = new ArrayList<>();

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
      for (final SelectExpressionItem expressionItem : selectExpressionItems) {

        // TODO later in the project if the renaming is required as output this
        // can be used by setting as a column name
        // for now we don't need to output the column name or the rename(alias)
        // String alias = expressionItem.getAlias();

        final ColumnCell columnCell = opVisitor.getValue(tuple,
            expressionItem.getExpression());
        // final Integer tableId = tuple.getColumnCells().iterator().next()
        // .getTableId();
        Integer tableId = columnCell.getTableId() == null
            ? tuple.getColumnCells().iterator().next().getTableId()
            : columnCell.getTableId();
        if (null != columnCell) {

          // if alias is present
          if (!StringUtils.isBlank(expressionItem.getAlias())) {
            /*
             * If the expression has an alias then we add a new column to the
             * table with the name of the alias. Any change in table schema
             * should be registered with the Schema Manager
             */

            // register with Schema Manager
            // addColumnAliasToSchema(expressionItem, tableId);

            // Update the column id of Column Cell
            columnCell.setColumnId(SchemaManager.getColumnIdByTableId(tableId,
                expressionItem.getAlias()));
            // Cell value returned from expressions like addition don't have
            // table id or column id set, so set both for them as well.
            columnCell.setTableId(tableId);
          }

          columnCells.add(columnCell);
        }
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

  /**
   * Get the alias in the {@link SelectExpressionItem} and add it as a new
   * column in the {@link TableSchema}. Register the changes in table schema
   * with the {@link SchemaManager}.
   *
   * @param expressionItem
   * @param tableId
   */
  private void addColumnAliasToSchema(final SelectExpressionItem expressionItem,
      Integer tableId) {
    final TableSchema tableSchema = SchemaManager.getTableSchemaById(tableId);
    final List<ColumnDefinition> columnDefinitions = tableSchema
        .getColumnDefinitions();
    final ColumnDefinition columnDefinition = new ColumnDefinition();
    columnDefinition.setColumnName(expressionItem.getAlias());
    columnDefinitions.add(columnDefinition);
    tableSchema.setColumnDefinitions(columnDefinitions);

    // update schema
    SchemaManager.updateSchema(tableId, tableSchema);
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
    // TODO: Rethink deep once doing projection pushdown
    if (!CollectionUtils.isEmpty(builtSchema)) {

      // expr like R.*
      findAllTableColumns();
    }

    // invoke child schema for schema manager updation
    final List<Pair<Integer, Integer>> childSchema = getChildren().get(0)
        .getBuiltSchema();

    // if expression is SELECT * or SELECT R.*, S.* FROM R,S
    if (this.allColFlag || CollectionUtils.isEmpty(selectExpressionItems)) {
      builtSchema = childSchema;
      return builtSchema;
    }

    // get schema based on expression items of project
    for (final SelectExpressionItem selectExprItem : this.selectExpressionItems) {

      if (selectExprItem.getExpression() instanceof Column) {
        final Column column = (Column) selectExprItem.getExpression();

        // if no table name, get table id from child schema where the
        // column name matches
        if (StringUtils.isBlank(column.getTable().getName())) {

          buildNoTableSchema(childSchema, selectExprItem,
              column.getColumnName());
          continue;
        } // if no column name

        buildWithCidTid(selectExprItem, column);
        continue;
      } else {
        if (!StringUtils.isBlank(selectExprItem.getAlias())) {
          buildExpression(childSchema, selectExprItem);
        }
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

    // if alias is present, add to schema
    if (!StringUtils.isBlank(selectExprItem.getAlias())) {

      addColumnAliasToSchema(selectExprItem, tableId);
      builtSchema.add(new Pair<Integer, Integer>(tableId, SchemaManager
          .getColumnIdByTableId(tableId, selectExprItem.getAlias())));
      return;
    }

    // no alias present
    builtSchema.add(new Pair<Integer, Integer>(tableId,
        SchemaManager.getColumnIdByTableId(tableId, column.getColumnName())));

  }

  /**
   *
   * @param childSchema
   * @param selectExprItem
   * @param column
   */
  private void buildNoTableSchema(List<Pair<Integer, Integer>> childSchema,
      final SelectExpressionItem selectExprItem, final String columnName) {
    for (final Pair<Integer, Integer> pair : childSchema) {
      // if matching
      if (!SchemaManager.getColumnNameById(pair.getKey(), pair.getValue())
          .equals(columnName)) {
        continue;
      }

      // alias
      if (!StringUtils.isBlank(selectExprItem.getAlias())) {

        addColumnAliasToSchema(selectExprItem, pair.getKey());
        builtSchema.add(new Pair<Integer, Integer>(pair.getKey(), SchemaManager
            .getColumnIdByTableId(pair.getKey(), selectExprItem.getAlias())));
        return;
      }

      // no alias present
      builtSchema.add(new Pair<Integer, Integer>(pair.getKey(),
          SchemaManager.getColumnIdByTableId(pair.getKey(), columnName)));

      return;
    } // for
  }

  /**
   *
   * @param childSchema
   * @param selectExprItem
   * @param column
   */
  private void buildExpression(List<Pair<Integer, Integer>> childSchema,
      final SelectExpressionItem selectExprItem) {
    // alias
    if (!StringUtils.isBlank(selectExprItem.getAlias())) {

      addColumnAliasToSchema(selectExprItem, childSchema.get(0).getKey());
      builtSchema.add(new Pair<Integer, Integer>(childSchema.get(0).getKey(),
          SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
              selectExprItem.getAlias())));
      return;
    }

    // no alias present
    builtSchema.add(new Pair<Integer, Integer>(childSchema.get(0).getKey(),
        SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
            selectExprItem.getExpression().toString())));

    return;

  }

}
