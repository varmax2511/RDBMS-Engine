package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.CollectionUtils;
/**
 * Limit the number of records by count. The operator will track the number of
 * records tracked so far and set its {@link #hasNext()} to return null once the
 * target count is complete.
 *
 * @author varunjai
 *
 */
public class LimitOperator extends Node implements UnaryOperator {

  private long rowCount;

  /**
   *
   * @param rowCount
   *          non-negative
   */
  public LimitOperator(long rowCount) {
    // validate
    if (rowCount < 1) {
      throw new IllegalArgumentException("Invalid Limit row count!!!");
    }
    this.rowCount = rowCount;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    if (CollectionUtils.isEmpty(builtSchema)) {
      builtSchema = getChildren().get(0).getBuiltSchema();
    } // if

    return builtSchema;
  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws Throwable {

    final List<Tuple> limitOutput = new ArrayList<>();

    if (tuples == null || tuples.size() == 0) {
      return limitOutput;
    }

    // unary operator, interested in only the first collection
    final Collection<Tuple> tupleRecords = tuples.iterator().next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return limitOutput;
    }

    // if number of records is less than row count
    if (tupleRecords.size() <= getRowCount()) {
      this.rowCount = this.rowCount - tupleRecords.size();
      return tupleRecords;
    }

    for (final Tuple tuple : tupleRecords) {
      if (getRowCount() == 0) {
        break;
      }

      limitOutput.add(tuple);
      this.rowCount--;
    } // for

    return limitOutput;
  }

  @Override
  public boolean hasNext() throws IOException {
    // once row count drops, set hasnext to false
    if (getRowCount() == 0) {
      return false;
    }
    return super.hasNext();
  }

  /**
   * Get the present value of the row count.
   * 
   * @return
   */
  public long getRowCount() {
    return rowCount;
  }

}
