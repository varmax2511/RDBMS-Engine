/**
 * 
 */
package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.ExpressionDecoder;
import edu.buffalo.www.cse4562.util.MapUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class HashJoinOperator extends Node implements BinaryOperator,JoinOperator{

  private final Expression expression;
  Map<Integer,List<Tuple>> hashTable= new HashMap<>();


  public HashJoinOperator(Expression expression) {
    Validate.notNull(expression);

    this.expression = expression;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    
     builtSchema = new ArrayList<>();
      final Iterator<Node> childItr = getChildren().iterator();

      // iterate over all children and add their schema
      while (childItr.hasNext()) {
        builtSchema.addAll(childItr.next().getBuiltSchema());
      } // while

    return builtSchema;
  }

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {

    final Iterator<Collection<Tuple>> tupleCollItr = tupleCollection.iterator();

    if (!tupleCollItr.hasNext()) {
      return null;
    }

    Collection<Tuple> outputTuples = tupleCollItr.next();
    final OperatorVisitor opVisitor = new OperatorExpressionVisitor();
    /*
     * Merge collection of tuples one by one. First merge the two collections
     * and create a new collection containing their join. Then join these with
     * the next and so on.
     */
    while (tupleCollItr.hasNext()) {
      final Collection<Tuple> nextTable = tupleCollItr.next();

      final List<Tuple> newOutputTuple = new ArrayList<>();

      for (final Tuple tuple : outputTuples) {

        // final List<ColumnCell> mergedColumnCells = new ArrayList<>();
        // mergedColumnCells.addAll(tuple.getColumnCells());

        // join one row of the outputTuple with one row of the next table per
        // iteration
        for (final Tuple joinTuple : nextTable) {

          if (joinTuple == null
              || CollectionUtils.isEmpty(joinTuple.getColumnCells())) {
            continue;
          }

          final List<ColumnCell> mergedColumnCells = new ArrayList<>();
          mergedColumnCells.addAll(tuple.getColumnCells());
          mergedColumnCells.addAll(joinTuple.getColumnCells());

          final Tuple testTuple = new Tuple(mergedColumnCells);
          // process expressions

          ColumnCell columnCell = null;
          columnCell = opVisitor.getValue(testTuple, this.expression);
         

          // if operator returned a result and its value is true, then row can
          // get
          // selected
          if (null != columnCell && columnCell.getCellValue().toBool()) {
            newOutputTuple.add(testTuple);
          } // if

        } // for

      } // for

      outputTuples = newOutputTuple;

    } // while

    return outputTuples;
  }

  @Override
  public Collection<Tuple> getNext() throws Throwable {
    // check child count, should be 2
    if (this.getChildren() == null || this.getChildren().size() != 2) {
      throw new IllegalArgumentException(
          "Invalid cross product child configuration!");
    }
    ExpressionDecoder expDecoder = new ExpressionDecoder(expression);
    List<Column> decodedColumns = expDecoder.getDecodedColumns();


    Node firstChild = this.getChildren().get(0);
    Node secondChild = this.getChildren().get(1);

    //if hashTable is empty then populate it
    if(MapUtils.isEmpty(hashTable)) {
      hashTable = HashJoin.populateHashTable(firstChild, expression);
    }
    
    if(MapUtils.isEmpty(hashTable) && !secondChild.hasNext()) {
      return new ArrayList<Tuple>();
    }
      
      Collection<Tuple> tuples = new ArrayList<>();
      Collection<Tuple> secondChildTuples = secondChild.getNext();
      
      for(final Tuple tuple:secondChildTuples) {
        
        Collection<Collection<Tuple>> tuplesForProcess = new ArrayList<>();
        int hashedValue=1;
        for(Column column: decodedColumns) {

        for(ColumnCell cell : tuple.getColumnCells()) {
            if(HashJoin.columnsMatch(cell,column)) {
              hashedValue = HashJoin.getHashedValue(cell.getCellValue(),hashedValue);
              break ;
            }
          }
      }
        
        if(hashTable.containsKey(hashedValue)) {
          tuplesForProcess.add(hashTable.get(hashedValue));
          List<Tuple> tuple1= new ArrayList<>();
          tuple1.add(tuple);
          tuplesForProcess.add(tuple1);
          tuples.addAll(process(tuplesForProcess));
        }
      }
      
      return tuples;
  }

  @Override
  public boolean hasNext() throws IOException {
    return super.hasNext() || this.getChildren().get(1).hasNext();
  }

  @Override
  public void close() throws Throwable {
    this.hashTable = null;
  }

  public void setSchema(List<Pair<Integer, Integer>> builtSchema) {
    if (builtSchema == null) {
      return;
    }
    this.builtSchema = builtSchema;
  }

  public Expression getExpression() {
    return expression;
  }
  
  
}
