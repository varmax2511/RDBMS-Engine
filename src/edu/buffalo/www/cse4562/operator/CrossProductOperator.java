package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;

/**
 * To take cross product,
 * Tables R and S
 * We need tuple blocks, say 20 tuples from each child and get a result of 400 rows.
 * Now next call to getNext(), should check if S has reached the end of the iteration
 * or not. 
 * 
 * If S ! end
 *  - Use the same 20 tuples of R pulled in previous call and pull the next 20 tuples
 *    of S.
 * 
 * If S = end
 *  - Read the next 20 tuples from R if |R| > 40 
 *  - Re-open the S scanner to again read the S tables    
 *    
 * @author varunjai
 *
 */
public class CrossProductOperator extends Node implements BinaryOperator {

  
  
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

        newOutputTuple.add(new Tuple(mergedColumnCells));
      } // for

      outputTuples = newOutputTuple;

    } // while

    return outputTuples;
  }

  @Override
  public Collection<Tuple> getNext() throws Throwable {
    // check child count, should be 2
    if (this.getChildren() == null || this.getChildren().size() != 2) {
      throw new IllegalArgumentException(
          "Invalid cross product child configuration!");
    }

    final Collection<Collection<Tuple>> tuples = new ArrayList<>();
    final Iterator<Node> iterator = this.getChildren().iterator();

    // process each child node for this node
    // add all collection of tuples returned by each node in a collection
    // of collection
    while (iterator.hasNext()) {
      tuples.add(iterator.next().getNext());
    }

    return process(tuples);
  }
}
