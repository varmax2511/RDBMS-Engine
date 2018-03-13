package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.operator.visitor.OperatorExpressionVisitor;
import edu.buffalo.www.cse4562.operator.visitor.OperatorVisitor;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;
/**
 * This {@link Operator} performs the selection from a relational point of view.
 * Its presently responsible for evaluating a WHERE clause which is not a
 * sub-select in itself.
 * 
 * @author varunjai
 *
 */
public class SelectionOperator extends Node implements UnaryOperator {

  private final Expression expression;

  /**
   * 
   * @param expression
   *          !null.
   */
  public SelectionOperator(Expression expression) {
    Validate.notNull(expression);
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples) throws Throwable {

    final List<Tuple> selectOutputs = new ArrayList<>();

    if (tuples == null || tuples.size() == 0) {
      return selectOutputs;
    }

    // unary operator, interested in only the first collection
    Collection<Tuple> tupleRecords = tuples.iterator().next();
    
    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return selectOutputs;
    }

    final OperatorVisitor opVisitor = new OperatorExpressionVisitor();
    // iterate tuples in the collection
    for (final Tuple tuple : tupleRecords) {
      // process expressions
      final ColumnCell columnCell = opVisitor.getValue(tuple, this.expression);

      // if operator returned a result and its value is true, then row can get
      // selected
      if (null != columnCell && columnCell.getCellValue().toBool()) {
        selectOutputs.add(tuple);
      }

    } // for

    return selectOutputs;
  }

}
