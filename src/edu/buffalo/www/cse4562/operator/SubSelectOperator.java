package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.StringUtils;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
/**
 * This operator is responsible for adding Alias information whenever its passed
 * with a subselect.
 *
 * @author varunjai
 *
 */
public class SubSelectOperator extends Node implements UnaryOperator {

  private final String alias;

  public SubSelectOperator(String alias) {
    this.alias = alias;
  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws Throwable {

    if (tuples == null || tuples.size() == 0) {
      return new ArrayList<>();
    }

    // unary operator, interested in only the first collection
    final Collection<Tuple> tupleRecords = tuples.iterator().next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return tupleRecords;
    }

    // add table entry in Schema Manager

    final Iterator<Tuple> tupleItr = tupleRecords.iterator();
    final List<ColumnDefinition> columnDefinitions = new ArrayList<>();
    final Tuple tuple = tupleItr.next();

    /*
     * for (final ColumnCell columnCell : tuple.getColumnCells()) { final
     * ColumnDefinition columnDefinition = new ColumnDefinition();
     * columnDefinition.setColumnName(SchemaManager.getColumnNameById(
     * columnCell.getTableId(), columnCell.getColumnId()));
     * columnDefinitions.add(columnDefinition); } // for
     * 
     * SchemaManager.addTableSchema(this.alias, new TableSchema(this.alias,
     * columnDefinitions));
     */

    // update table id to alias name in each column of the returned tuples
    
    Collection<Tuple> tupleCopies = TuplePrinter.getTupleCopy(tupleRecords);
    for (final Tuple tupleVal : tupleCopies) {
      
      for (final ColumnCell columnCell : tupleVal.getColumnCells()) {

        
        // update column's id and table id
        columnCell.setColumnId(SchemaManager.getColumnIdByTableId(
            SchemaManager.getTableId(this.alias),
            SchemaManager.getColumnNameById(columnCell.getTableId(),
                columnCell.getColumnId())));

        columnCell.setTableId(SchemaManager.getTableId(this.alias));
      }
    }

    return tupleCopies;
  }

  public String getAlias() {
    return alias;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
//    if (CollectionUtils.isEmpty(builtSchema)) {

      builtSchema = new ArrayList<>();
      final List<Pair<Integer, Integer>> childSchema = getChildren().get(0)
          .getBuiltSchema();
      if (StringUtils.isBlank(this.alias)) {
        builtSchema = childSchema;
        return builtSchema;
      }

      buildSchemaWithAlias(childSchema);

  //  }// not yet schema built

    return builtSchema;
  }

  private void buildSchemaWithAlias(
      final List<Pair<Integer, Integer>> childSchema) {
    final List<ColumnDefinition> columnDefinitions = new ArrayList<>();
    for (final Pair<Integer, Integer> pair : childSchema) {
      final ColumnDefinition columnDefinition = new ColumnDefinition();
      columnDefinition.setColumnName(
          SchemaManager.getColumnNameById(pair.getKey(), pair.getValue()));
      columnDefinitions.add(columnDefinition);
    } // for

    SchemaManager.addTableSchema(this.alias,
        new TableSchema(this.alias, columnDefinitions));

    final Integer tableId = SchemaManager.getTableId(this.alias);
    final TableSchema tableSchema = SchemaManager.getTableSchemaById(tableId);

    // build schema
    for (final ColumnDefinition colDefinition : tableSchema
        .getColumnDefinitions()) {

      builtSchema.add(new Pair<Integer, Integer>(tableId, SchemaManager
          .getColumnIdByTableId(tableId, colDefinition.getColumnName())));
    } // for
  }
}
