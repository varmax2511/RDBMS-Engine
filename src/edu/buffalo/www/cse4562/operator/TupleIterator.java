package edu.buffalo.www.cse4562.operator;

import java.util.Collection;

import edu.buffalo.www.cse4562.model.Tuple;
/**
 * This interface provides method for classes which iterate on tuples from disk.
 * 
 * @author varunjai
 *
 */
public interface TupleIterator {

  void open() throws Throwable;
  Collection<Tuple> getNext() throws Throwable;
  void close() throws Throwable;
}
