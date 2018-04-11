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
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * @author Sneha Mehta
 *
 */
public class MinAggregate extends Node implements AggregateOperator {

  private Function function;
  private PrimitiveValue min;

  public MinAggregate(Function function) {
    this.function = function;
  }

  @Override
  public Tuple getAggregate(List<Tuple> tupleRecords) {
    OperatorVisitor opExpVisitor = new OperatorExpressionVisitor();
    for (Tuple tuple : tupleRecords) {
      final ColumnCell columnCell = opExpVisitor.getValue(tuple, function);
      try {

        if (min == null) {
          min = columnCell.getCellValue();
        } else if (columnCell.getCellValue() instanceof LongValue) {
          min = (min.toLong() > columnCell.getCellValue().toLong())
              ? columnCell.getCellValue()
              : min;
        } else if (columnCell.getCellValue() instanceof DoubleValue) {
          min = (min.toDouble() > columnCell.getCellValue().toDouble())
              ? columnCell.getCellValue()
              : min;
        } else if (columnCell.getCellValue() instanceof StringValue) {
          min = (min.toString()
              .compareTo(columnCell.getCellValue().toString()) > 0)
                  ? columnCell.getCellValue()
                  : min;
        } else {
          DateValue minDate = (DateValue) min;
          DateValue resultDate = (DateValue) columnCell.getCellValue();
          min = (minDate.getValue().getTime() > resultDate.getValue().getTime())
              ? resultDate
              : min;
        }
      } catch (InvalidPrimitive e) {
        System.err.println("Invalid primitive for MIN: " + min.getType());
      }
    }
    final Tuple tuple = tupleRecords.get(0);
    ColumnCell cCell = new ColumnCell(min);
    cCell.setTableId(builtSchema.get(0).getKey());
    cCell.setColumnId(SchemaManager.getColumnIdByTableId(cCell.getTableId(), function.toString()));

    tuple.getColumnCells().add(cCell);
    min=null;
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
    builtSchema = getChildren().get(0).getBuiltSchema();
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
}
