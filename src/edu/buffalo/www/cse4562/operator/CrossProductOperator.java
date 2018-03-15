package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;

/**
 * To take cross product, Tables R and S We need tuple blocks, say 20 tuples
 * from each child and get a result of 400 rows. Now next call to getNext(),
 * should check if S has reached the end of the iteration or not.
 * 
 * If S ! end - Use the same 20 tuples of R pulled in previous call and pull the
 * next 20 tuples of S.
 * 
 * If S = end - Read the next 20 tuples from R if |R| > 40 - Re-open the S
 * scanner to again read the S tables
 * 
 * @author varunjai
 *
 */
public class CrossProductOperator extends Node implements BinaryOperator {

  private Collection<Tuple> holdingList = null;

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

        // join one row of the outputTuple with every row of the next table
        for (final Tuple joinTuple : nextTable) {

          final List<ColumnCell> mergedColumnCells = new ArrayList<>();
          mergedColumnCells.addAll(tuple.getColumnCells());
          mergedColumnCells.addAll(joinTuple.getColumnCells());
          newOutputTuple.add(new Tuple(mergedColumnCells));
        } // for

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

    Node firstChild = this.getChildren().get(0);
    Node secondChild = this.getChildren().get(1);

    // update relation 1 tuples
    if (CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()) {
      holdingList = firstChild.getNext();
    }
    // if first child has rows and the second child has reached end,
    // then re-open the second child iterator and update the holding list
    // with the next values from first child.
    if (firstChild.hasNext() && !secondChild.hasNext()) {
      holdingList = firstChild.getNext();
      // update if result obtained is empty, possible if a select is below
      while(CollectionUtils.isEmpty(holdingList) && firstChild.hasNext()){
        holdingList = firstChild.getNext();  
      }
      secondChild.close();
      secondChild.open();
    }// if

    final Collection<Collection<Tuple>> tuples = new ArrayList<>();
    tuples.add(holdingList);
    // add second child tuples
    tuples.add(secondChild.getNext());
    return process(tuples);
  }

  @Override
  public void close() throws Throwable {
    this.holdingList = null;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {

    // if no schema populated already
    if (CollectionUtils.isEmpty(builtSchema)) {
      final Iterator<Node> childItr = getChildren().iterator();
      // iterate over all children and add their schema
      while (childItr.hasNext()) {
        builtSchema.addAll(childItr.next().getBuiltSchema());
      } // while
    } // if
    return builtSchema;
  }
}
