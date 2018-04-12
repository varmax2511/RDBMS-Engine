/**
 * 
 */
package edu.buffalo.www.cse4562.aggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * @author Sneha Mehta
 *
 */
public class CountAggregate extends Node implements AggregateOperator {
  private Function function;
  private LongValue count;

  public CountAggregate(Function function) {
    this.function = function;
    count = new LongValue(0);
  }

  @Override
  public Tuple getAggregate(List<Tuple> tupleRecords) {
    count.setValue(tupleRecords.size());

    final Tuple tuple = tupleRecords.get(0);
    ColumnCell cCell = new ColumnCell(count);
    cCell.setTableId(builtSchema.get(0).getKey());
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(), function.toString()));

    tuple.getColumnCells().add(cCell);
    count = new LongValue(0);
    return tuple;
  }

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {
    final List<Tuple> aggregateOutputs = new ArrayList<>();

    // null check
    if (tupleCollection == null || tupleCollection.size() == 0) {
      return aggregateOutputs;
    }

    // unary operator, interested in only the first collection
    final List<Tuple> tupleRecords = (List<Tuple>) tupleCollection.iterator()
        .next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return aggregateOutputs;
    }

    aggregateOutputs.add(getAggregate(tupleRecords));

    return aggregateOutputs;

  }
  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    builtSchema = new ArrayList<>(getChildren().get(0).getBuiltSchema());
    String fullName = function.toString();
    addFunctionToSchema(fullName, builtSchema);
    return builtSchema;
  }

  private void addFunctionToSchema(String fullName,
      List<Pair<Integer, Integer>> builtSchema) {

    Integer tableId = builtSchema.get(0).getKey();
    // if not already registered
    if (SchemaManager.getColumnIdByTableId(tableId, fullName) == null) {
      final TableSchema tableSchema = SchemaManager.getTableSchemaById(tableId);
      final List<ColumnDefinition> columnDefinitions = tableSchema
          .getColumnDefinitions();
      final ColumnDefinition columnDefinition = new ColumnDefinition();
      columnDefinition.setColumnName(fullName);
      columnDefinitions.add(columnDefinition);
      tableSchema.setColumnDefinitions(columnDefinitions);

      // update schema
      SchemaManager.updateSchema(tableId, tableSchema);
    }

    builtSchema.add(new Pair<Integer, Integer>(tableId,
        SchemaManager.getColumnIdByTableId(tableId, fullName)));
    // return;
  }

  @Override
  public Function getFunction() {
    // TODO Auto-generated method stub
    return this.function;
  }

}
