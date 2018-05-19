package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Container;
import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.ScannerContainer;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.ExpressionDecoder;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class IndexNestedJoinOperator extends Node implements BinaryOperator,JoinOperator {

  private final Expression expression;
  private Collection<Tuple> holdingList = new ArrayList<>();
  private ScannerContainer scannerContainer;
  private Integer leftTableId;
  private Integer leftColumnId;
  private Integer rightTableId;
  private Integer rightColumnId;

  public IndexNestedJoinOperator(Expression expression,
      ScannerContainer scannerContainer, Integer leftTableId, Integer leftColumnId,Integer rightTableId, Integer rightColumnId) {
    Validate.notNull(expression);

    this.expression = expression;
    this.scannerContainer = scannerContainer;
    this.leftTableId = leftTableId;
    this.leftColumnId = leftColumnId;
    this.rightTableId = rightTableId;
    this.rightColumnId = rightColumnId;
  }

  @Override
  public void setSchema(List<Pair<Integer, Integer>> builtSchema) {
    if (builtSchema == null) {
      return;
    }
    this.builtSchema = builtSchema;
  }

  @Override
  public Expression getExpression() {
    return expression;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    // if (CollectionUtils.isEmpty(builtSchema)) {
    builtSchema = new ArrayList<>();
    final Iterator<Node> childItr = getChildren().iterator();

    // iterate over all children and add their schema
    while (childItr.hasNext()) {
      builtSchema.addAll(childItr.next().getBuiltSchema());
    } // while

    // } // if
    return builtSchema;
  }

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {
    //holdingList = new ArrayList<>();
    
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
        holdingList.remove(tuple);
        //System.out.println(holdingList.size());
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
  public Collection<Tuple> getNext(Container container) throws Throwable {
    
    if (this.getChildren() == null || this.getChildren().size() != 2) {
      throw new IllegalArgumentException(
          "Invalid cross product child configuration!");
    }

    Node firstChild = this.getChildren().get(0);
    Node secondChild = this.getChildren().get(1);

    // update relation 1 tuples
    while (CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()) {
      holdingList = TuplePrinter.getTupleCopy(firstChild.getNext(null));
    }

    if (CollectionUtils.isEmpty(holdingList)) {
      return new ArrayList<>();
    }

    // if first child has rows and the second child has reached end,
    // then re-open the second child iterator and update the holding list
    // with the next values from first child.
//    if (!holdingList.isEmpty() && !secondChild.hasNext()) {
//
//      ((ArrayList)holdingList).remove(0);
//
//      while (CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()) {
//        holdingList = TuplePrinter.getTupleCopy(firstChild.getNext(null));
//      }
//
//      if (CollectionUtils.isEmpty(holdingList)) {
//        return new ArrayList<>();
//      }
//
//      secondChild.close();
//      secondChild.open();
//
//    } // if

    final Collection<Collection<Tuple>> tuples = new ArrayList<>();

    final Collection<Tuple> heldTuple = new ArrayList<>();
    Tuple tuple = (Tuple)((ArrayList)holdingList).get(0);
    heldTuple.add(tuple);
    tuples.add(heldTuple);
    tuples.add(secondChild.getNext(getContainerWithValue(tuple)));
    Collection<Tuple> t=process(tuples);

    //System.out.println(t.size());
   

    return  t;
    }

  private Container getContainerWithValue(Tuple tuple) throws InvalidPrimitive {
    for (ColumnCell columnCell : tuple.getColumnCells()) {
      if (columnCell.getTableId() == leftTableId
          && columnCell.getColumnId() == leftColumnId) {
        scannerContainer.setValue((int)SchemaManager.getRandomIndexOffset(rightTableId, rightColumnId, (int)columnCell.getCellValue().toLong()));
        return scannerContainer;
      }
    }
    throw new IllegalArgumentException("Column cell not found!!");
  }

  @Override
  public boolean hasNext() throws IOException {
    return !holdingList.isEmpty() || this.getChildren().get(0).hasNext();
  }
}
