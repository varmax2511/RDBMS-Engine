package edu.buffalo.www.cse4562.operator;

import java.util.Collection;

import edu.buffalo.www.cse4562.model.Tuple;

/**
 * This interface marks all classes implementing them as an SQL operator
 * processor.
 *
 */
public interface Operator {
  /**
   * Each operator accepts a Collection of collection of tuples. This is to
   * cater operators which can be binary or more, like Croos-Product which
   * accept two collection of tuples, one from each table.
   * 
   * @param tupleCollection
   * @return
   * @throws Throwable
   */
  public Collection<Tuple> process(Collection<Collection<Tuple>> tupleCollection)
      throws Throwable;
}
