/**
 * 
 */
package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.aggregator.AverageAggregate;
import edu.buffalo.www.cse4562.aggregator.AverageAggregate1;
import edu.buffalo.www.cse4562.aggregator.CountAggregate;
import edu.buffalo.www.cse4562.aggregator.CountAggregate1;
import edu.buffalo.www.cse4562.aggregator.MaxAggregate;
import edu.buffalo.www.cse4562.aggregator.MaxAggregate1;
import edu.buffalo.www.cse4562.aggregator.MinAggregate;
import edu.buffalo.www.cse4562.aggregator.MinAggregate1;
import edu.buffalo.www.cse4562.aggregator.SumAggregate;
import edu.buffalo.www.cse4562.aggregator.SumAggregate1;
import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * Implement each function on the same set of data received in getNext()
 * 
 * Contain a list of function which will be processed in order
 * 
 * built schema will add a new pair for each function in order
 * 
 * get next will get the same chunk for each function
 * 
 * 
 * @author Sneha Mehta
 *
 */
public class AggregateOperator1 extends Node{
  
  private List<Function> functions = new ArrayList<>();
  
  public AggregateOperator1(List<Function> functions) {
    this.functions = functions;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    builtSchema = new ArrayList<>(getChildren().get(0).getBuiltSchema());
    for(Function function: functions) {
    String fullName = function.toString();
    addFunctionToSchema(fullName, builtSchema);
    }
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

    Integer tableId = builtSchema.get(0).getKey();
    
    Tuple tuple = tupleRecords.get(0);
    for(Function function : functions) {
    switch (function.getName().toUpperCase()) {
      case "SUM" :
        SumAggregate1 sumAggregate = new SumAggregate1(function);
        tuple.getColumnCells().add(sumAggregate.getAggregate(tupleRecords,tableId));
        break;
      case "COUNT" :
        CountAggregate1 countAggregate = new CountAggregate1(function);
        tuple.getColumnCells().add(countAggregate.getAggregate(tupleRecords,tableId));
        break;
      case "AVG":
        AverageAggregate1 avgAggregate = new AverageAggregate1(function);
        tuple.getColumnCells().add(avgAggregate.getAggregate(tupleRecords,tableId));
        break;
      case "MIN":
        MinAggregate1 minAggregate = new MinAggregate1(function);
        tuple.getColumnCells().add(minAggregate.getAggregate(tupleRecords,tableId));
        break;
      case "MAX":
        MaxAggregate1 maxAggregate = new MaxAggregate1(function);
        tuple.getColumnCells().add(maxAggregate.getAggregate(tupleRecords,tableId));
        break;
      default: System.err.println("This function is not handled: "+function.getName());
      
    }
    }
    aggregateOutputs.add(tuple);

    return aggregateOutputs;
  }

  
  @Override
  public Collection<Tuple> getNext() throws Throwable {
    // check child count, should be 1
    if (this.getChildren() == null || this.getChildren().size() != 1) {
      throw new IllegalArgumentException(
          "Invalid Aggregation child configuration!");
    }
    final Collection<Tuple> tuples = new ArrayList<>();
    if (getChildren().get(0) instanceof GroupByOperator && getChildren().get(0).hasNext()) {
      tuples.addAll(getChildren().get(0).getNext());
    }
    else {
      while(getChildren().get(0).hasNext()) {
        tuples.addAll(getChildren().get(0).getNext());
      }
    }

    final Collection<Collection<Tuple>> tupleCollection = new ArrayList<>();
    tupleCollection.add(tuples);

    return process(tupleCollection);
  }

  public List<Function> getFunctions() {
    return functions;
  }

  public void setFunctions(List<Function> functions) {
    this.functions = functions;
  }
  
  
}
