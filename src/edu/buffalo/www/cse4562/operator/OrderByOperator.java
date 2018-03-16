package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.TupleComparator;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * Order By
 *
 * @author varunjai
 *
 */
public class OrderByOperator extends Node implements BlockingOperator {

  private final List<OrderByElement> orderByElements;

  /**
   *
   * @param orderByElements
   *          !null
   */
  public OrderByOperator(List<OrderByElement> orderByElements) {
    Validate.notNull(orderByElements);
    this.orderByElements = orderByElements;
  }

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {

    final List<Tuple> selectOutputs = new ArrayList<>();

    // null check
    if (tupleCollection == null || tupleCollection.size() == 0) {
      return selectOutputs;
    }

    // unary operator, interested in only the first collection
    final List<Tuple> tupleRecords = (List<Tuple>) tupleCollection.iterator()
        .next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return selectOutputs;
    }

    Collections.sort(tupleRecords, new TupleComparator(builtSchema));
    // sort in reverse
    if (!orderByElements.get(0).isAsc()) {
      Collections.reverse(tupleRecords);
    }

    return tupleRecords;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    if (CollectionUtils.isEmpty(builtSchema)) {
      builtSchema = getChildren().get(0).getBuiltSchema();
    } // if

    return builtSchema;
  }

  @Override
  public Collection<Tuple> getNext() throws Throwable {
    // check child count, should be 2
    if (this.getChildren() == null || this.getChildren().size() != 1) {
      throw new IllegalArgumentException(
          "Invalid Order By child configuration!");
    }

    final Collection<Tuple> tuples = new ArrayList<>();
    while (getChildren().get(0).hasNext()) {
      tuples.addAll(getChildren().get(0).getNext());
    } // add all

    final Collection<Collection<Tuple>> tupleCollection = new ArrayList<>();
    tupleCollection.add(tuples);

    return process(tupleCollection);
  }
}
