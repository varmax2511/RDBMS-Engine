package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Node implements BinaryOperator {

  private final Expression expression;
  private Collection<Tuple> holdingList = null;

  public JoinOperator(Expression expression) {
    Validate.notNull(expression);

    this.expression = expression;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    if (CollectionUtils.isEmpty(builtSchema)) {
      final Iterator<Node> childItr = getChildren().iterator();

      // iterate over all children and add their schema
      while (childItr.hasNext()) {
        builtSchema.addAll(childItr.next().getBuiltSchema());
      } // while

    } // if
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
          // try{
          columnCell = opVisitor.getValue(testTuple, this.expression);
          /*
           * }catch(Throwable t){
           * 
           * System.err.println(cnt); for(ColumnCell colCell :
           * testTuple.getColumnCells()){
           * System.err.println(colCell.getTableId() + "|" +
           * colCell.getColumnId()); }
           * 
           * System.err.println("Holding Tuple"); for(ColumnCell colCell :
           * tuple.getColumnCells()){ System.err.println(colCell.getTableId() +
           * "|" + colCell.getColumnId()); }
           * 
           * System.err.println("BuiltSchema"); for(Pair<Integer, Integer> pair
           * : builtSchema){ System.err.println(pair.getKey() + "|" +
           * pair.getValue()); }
           * 
           * System.err.println("------------------"); }
           */

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

    Node firstChild = this.getChildren().get(0);
    Node secondChild = this.getChildren().get(1);

    // update relation 1 tuples
    while (CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()) {
      holdingList = TuplePrinter.getTupleCopy(firstChild.getNext());
    }

    if (CollectionUtils.isEmpty(holdingList)) {
      return new ArrayList<>();
    }

    // if first child has rows and the second child has reached end,
    // then re-open the second child iterator and update the holding list
    // with the next values from first child.
    if (firstChild.hasNext() && !secondChild.hasNext()) {

      holdingList = TuplePrinter.getTupleCopy(firstChild.getNext());

      while (CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()) {
        holdingList = TuplePrinter.getTupleCopy(firstChild.getNext());
      }

      if (CollectionUtils.isEmpty(holdingList)) {
        return new ArrayList<>();
      }

      secondChild.close();
      secondChild.open();

    } // if

    final Collection<Collection<Tuple>> tuples = new ArrayList<>();
    tuples.add(holdingList);
    // add second child tuples
    tuples.add(secondChild.getNext());

    return process(tuples);
  }

  @Override
  public void close() throws Throwable {
    this.holdingList = null;
  }

  public void setSchema(List<Pair<Integer, Integer>> builtSchema) {
    if (builtSchema == null) {
      return;
    }
    this.builtSchema = builtSchema;
  }
}
