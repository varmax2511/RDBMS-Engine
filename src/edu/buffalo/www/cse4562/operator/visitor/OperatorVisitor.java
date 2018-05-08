package edu.buffalo.www.cse4562.operator.visitor;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import net.sf.jsqlparser.expression.Expression;
/**
 * An {@link OperatorVisitor} is presently expected to visit a {@link Tuple} and
 * return the corresponding {@link ColumnCell} matching that expression or null
 * indicating no match.
 * 
 * @author varunjai
 *
 */
public interface OperatorVisitor {

  /**
   * Return the {@link ColumnCell}
   * 
   * @return
   */
  public ColumnCell getValue(Tuple tuple, Expression expression);
}
