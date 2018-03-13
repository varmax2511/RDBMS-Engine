package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;

public class CrossProductOperator implements BinaryOperator {

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {

    Collection<Tuple> outputTuples = new ArrayList<>();
    final Iterator<Collection<Tuple>> tupleCollItr = tupleCollection.iterator();

    if (!tupleCollItr.hasNext()) {
      return null;
    }

    outputTuples = tupleCollItr.next();
    /*
     * Merge collection of tuples one by one.
     * First merge the two collections and create a new collection containing their
     * join. Then join these with the next and so on.
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

        newOutputTuple.add(new Tuple(mergedColumnCells));
      } // for

      outputTuples = newOutputTuple;

    } // while

    return outputTuples;
  }

}
