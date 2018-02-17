package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.util.Collection;

import edu.buffalo.www.cse4562.model.Tuple;

/**
 * This interface marks all classes implementing them as an SQL operator
 * processor.
 *
 */
public interface Operator {
 /*public void open() throws Throwable;
 public Collection<Tuple> getNext() throws Throwable;
 public void close() throws Throwable;*/
 public Collection<Tuple> process(Collection<Tuple> tuples) throws Throwable; 
}
