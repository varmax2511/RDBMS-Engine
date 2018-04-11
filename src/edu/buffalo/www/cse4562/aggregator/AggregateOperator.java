/**
 * 
 */
package edu.buffalo.www.cse4562.aggregator;

import java.util.List;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.operator.BlockingOperator;

/**
 * @author Sneha Mehta
 *
 */
public interface AggregateOperator extends BlockingOperator{

  public Tuple getAggregate(List<Tuple> tupleRecords);

}
