package edu.buffalo.www.cse4562.operator;

import java.util.Collection;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;

public class SelectionOperator implements Operator {

  private final Expression expression;

  public SelectionOperator(Expression expression) {
    Validate.notNull(expression);
    this.expression = expression;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples) throws Throwable {
     return null;    
  }

 

}
