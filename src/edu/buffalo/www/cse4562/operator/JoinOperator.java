package edu.buffalo.www.cse4562.operator;

import java.util.List;

import edu.buffalo.www.cse4562.model.Pair;
import net.sf.jsqlparser.expression.Expression;

public interface JoinOperator {


  void setSchema(List<Pair<Integer, Integer>> builtSchema);

  Expression getExpression();
}
