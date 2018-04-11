package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.SchemaUtils;
import edu.buffalo.www.cse4562.util.StringUtils;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * <pre>
 * Renaming operator are tasked with handling all the renaming tasks in an query
 * and not aliases.
 * SELECT A AS C FROM RADIO R;
 *
 * Here A AS C is renaming and RADIO R is aliasing. This operator handles only
 * renaming.
 *
 * Renaming happens only in {@link ProjectionOperator}. Presently projection
 * handles renaming, which gets dirty and prevents projection pushdown within a
 * query as its built schema changes
 *
 *
 * During tree generation, we will parse the {@link SelectItem} present in the
 * {@link ProjectionOperator} and find all the renaming to be performed. This
 * will be handled by the {@link RenamingOperator} which will sit on top of the
 * Projection.
 *
 * SELECT A AS C, D AS E FROM R ;
 *
 *                RENAMING (A -> C, D -> E)
 *                  |
 *                PROJECT  ( A, D)
 *                  |
 *                SCANNER   R
 *
 *
 * SELECT P1.ID AS P1_ID, P2.ID AS P2_ID FROM PLAYERS P1, PLAYERS P2
 * WHERE P1.ID = P2.ID;
 *
 *                     Rename
 *                       |
 *                     Project
 *                       |
 *                      Join
 *                      /  \
 *                    P1    P2
 *
 *
 * Requirement:
 * 1. List of Aliasing expressions to be performed.
 * 2. Handling cases where we have the table name and column name
 * 3. Handling cases where we don't have table name, only column name
 * 4. Handling expressions renaming.
 * 5. Handling aggregate renaming.
 *
 * </pre>
 *
 * @author varunjai
 *
 */
public class RenamingOperator extends Node implements UnaryOperator {

  private List<SelectExpressionItem> selectExpressionItems = new ArrayList<>();

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {

    // if already set
    if (!CollectionUtils.isEmpty(builtSchema)) {
      return builtSchema;
    }

    // invoke child schema for schema manager updation
    final List<Pair<Integer, Integer>> childSchema = getChildren().get(0)
        .getBuiltSchema();

    // get schema based on expression items of project
    for (final SelectExpressionItem selectExprItem : this.selectExpressionItems) {

      // if the expression is not an alias by error
      if (StringUtils.isBlank(selectExprItem.getAlias())) {
        continue;
      }

      // if column instance
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

    if (SchemaManager.getColumnIdByTableId(tableId,
        selectExprItem.getAlias()) == null) {
      SchemaManager.addColumnAliasToSchema(selectExprItem.getAlias(), tableId);
    }

    builtSchema.add(new Pair<Integer, Integer>(tableId, SchemaManager
        .getColumnIdByTableId(tableId, selectExprItem.getAlias())));
    return;

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

    // if not already registered
    if (SchemaManager.getColumnIdByTableId(matchPair.getKey(),
        selectExprItem.getAlias()) == null) {
      SchemaManager.addColumnAliasToSchema(selectExprItem.getAlias(),
          matchPair.getKey());
    }

    builtSchema.add(new Pair<Integer, Integer>(matchPair.getKey(), SchemaManager
        .getColumnIdByTableId(matchPair.getKey(), selectExprItem.getAlias())));
  }

  /**
   *
   * @param childSchema
   * @param selectExprItem
   * @param column
   */
  private void buildExpression(List<Pair<Integer, Integer>> childSchema,
      final SelectExpressionItem selectExprItem) {

    // if not already registered
    if (SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
        selectExprItem.getAlias()) == null) {
      SchemaManager.addColumnAliasToSchema(selectExprItem.getAlias(),
          childSchema.get(0).getKey());
    }

    builtSchema.add(new Pair<Integer, Integer>(childSchema.get(0).getKey(),
        SchemaManager.getColumnIdByTableId(childSchema.get(0).getKey(),
            selectExprItem.getAlias())));
    return;
  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws Throwable {

    final List<Tuple> renamedOutput = new ArrayList<>();
    if (tuples == null || tuples.size() == 0) {
      return renamedOutput;
    }

    // unary operator, interested in only the first collection
    final Collection<Tuple> tupleRecords = tuples.iterator().next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return renamedOutput;
    }

    // iterate tuples in the collection
    for (final Tuple tuple : tupleRecords) {
      // process expressions
      final List<ColumnCell> columnCells = new ArrayList<>();
      int cnt = 0;
      // search for a match in the tuple column cells
      for (final ColumnCell columnCell : tuple.getColumnCells()) {
        boolean cellMatch = false;
        for (final SelectExpressionItem expressionItem : selectExpressionItems) {

          // if not matching
          if (columnCell == null
              || !SchemaUtils.expressionMatch(expressionItem.getExpression(),
                  columnCell.getTableId(), columnCell.getColumnId())) {

            continue;
          }

          // found match
          final ColumnCell cCell = new ColumnCell(columnCell.getCellValue());
          cCell.setColumnId(builtSchema.get(cnt).getValue());
          cCell.setTableId(builtSchema.get(cnt++).getKey());

          // add to list, move over to next expression
          columnCells.add(cCell);
          cellMatch = true;
          break;
        } // for
        
        // add cell even if it didn't match any expression as renaming doesnt'
        // trim data it just updates ids for renamed column cells
        if(!cellMatch){
          columnCells.add(columnCell);
        }

      } // for

      renamedOutput.add(new Tuple(columnCells));
    } // for

    return renamedOutput;
  }

  public void addSelectExpressionItems(
      SelectExpressionItem selectExpressionItems) {
    if (this.selectExpressionItems == null) {
      return;
    }

    this.selectExpressionItems.add(selectExpressionItems);
  }

  public List<SelectExpressionItem> getSelectExpressionItems() {
    return selectExpressionItems;
  }

  public void setSelectExpressionItems(
      List<SelectExpressionItem> selectExpressionItems) {
    this.selectExpressionItems = selectExpressionItems;
  }
}
