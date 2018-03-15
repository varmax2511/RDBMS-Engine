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
    Collection<Tuple> outputTuples = new ArrayList<>();
    final Iterator<Collection<Tuple>> tupleCollItr = tupleCollection.iterator();

    if (!tupleCollItr.hasNext()) {
      return null;
    }

    outputTuples = tupleCollItr.next();
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
        final List<ColumnCell> mergedColumnCells = new ArrayList<>();
        mergedColumnCells.addAll(tuple.getColumnCells());

        // join one row of the outputTuple with every row of the next table
        for (final Tuple joinTuple : nextTable) {

          mergedColumnCells.addAll(joinTuple.getColumnCells());
        } // for

        final Tuple testTuple = new Tuple(mergedColumnCells);
        // process expressions
        final ColumnCell columnCell = opVisitor.getValue(testTuple,
            this.expression);

        // if operator returned a result and its value is true, then row can get
        // selected
        if (null != columnCell && columnCell.getCellValue().toBool()) {
          newOutputTuple.add(testTuple);
        }

      } // for

      outputTuples = newOutputTuple;

    } // while

    return outputTuples;
  }

}
