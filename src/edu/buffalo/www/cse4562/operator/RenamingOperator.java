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

    builtSchema = new ArrayList<>();
    // invoke child schema for schema manager updation
    /*
     * Renaming operator will receive the schema prepared from below It is
     * responsible for only replacing the value of the schema pair where
     * renaming AS is required as per its expression
     */
    final List<Pair<Integer, Integer>> childSchema = getChildren().get(0)
        .getBuiltSchema();

    // iterate each pair
    for (Pair<Integer, Integer> pair : childSchema) {
      int schemaSize = builtSchema.size();
      
      // iterate each expression and see if this pair is a match
      for (final SelectExpressionItem selectExprItem : this.selectExpressionItems) {

        // if the expression is not an alias by error
        if (StringUtils.isBlank(selectExprItem.getAlias())) {
          continue;
        }

        if (selectExprItem.getExpression() instanceof Column) {
          handleColumnExpr(selectExprItem,
              (Column) selectExprItem.getExpression(), pair);
          continue;
        }

        handleExpr(selectExprItem, pair);
      } // for

      // not an renaming pair, so add directly
      // all renamed pair are added by the above methods
      if (schemaSize == builtSchema.size()) {
        builtSchema.add(pair);
      }
    } // for

    return builtSchema;
  }

  /**
   * 
   * @param selectExprItem
   * @param pair
   */
  private void handleExpr(SelectExpressionItem selectExprItem,
      Pair<Integer, Integer> pair) {

    Pair<Integer, Integer> matchPair = null;

    if (SchemaManager.getColumnNameById(pair.getKey(), pair.getValue())
        .equals(selectExprItem.getExpression().toString())) {
      matchPair = pair;
    }

    // no match
    if (matchPair == null) {
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
   * @param selectExprItem
   * @param column
   * @param pair
   */
  private void handleColumnExpr(SelectExpressionItem selectExprItem,
      Column column, Pair<Integer, Integer> pair) {

    Pair<Integer, Integer> matchPair = null;
    if (column.getTable() == null
        || StringUtils.isBlank(column.getTable().getName())) {

      if (SchemaManager.getColumnNameById(pair.getKey(), pair.getValue())
          .equals(column.getColumnName())) {
        matchPair = pair;
      }

    } else {

      if (SchemaManager.getTableName(pair.getKey())
          .equals(column.getTable().getName())
          && SchemaManager.getColumnNameById(pair.getKey(), pair.getValue())
              .equals(column.getColumnName())) {

        matchPair = pair;

      } // if

    } // else

    // no match
    if (matchPair == null) {
      return;
    }

    if (SchemaManager.getColumnIdByTableId(matchPair.getKey(),
        selectExprItem.getAlias()) == null) {
      SchemaManager.addColumnAliasToSchema(selectExprItem.getAlias(),
          matchPair.getKey());
    }

    builtSchema.add(new Pair<Integer, Integer>(matchPair.getKey(), SchemaManager
        .getColumnIdByTableId(matchPair.getKey(), selectExprItem.getAlias())));

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
        if (!cellMatch) {
          columnCells.add(columnCell);
          cnt++;
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
