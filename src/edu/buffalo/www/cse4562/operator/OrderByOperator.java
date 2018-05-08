package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.SchemaUtils;
import edu.buffalo.www.cse4562.util.TupleComparator;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * Order By
 *
 * @author varunjai
 *
 */
public class OrderByOperator extends Node implements BlockingOperator {

  private final List<OrderByElement> orderByElements;
  private List<Pair<Integer, Integer>> orderByPairs = new ArrayList<>();
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

    // populate orderby pairs if not populated already
    if (CollectionUtils.isEmpty(this.orderByPairs)) {
      getOrderByPairs();
    }

    Collections.sort(tupleRecords, new TupleComparator(orderByPairs));
    // sort in reverse
    if (!orderByElements.get(0).isAsc()) {
      Collections.reverse(tupleRecords);
    }

    return tupleRecords;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    // if (CollectionUtils.isEmpty(builtSchema)) {
    builtSchema = getChildren().get(0).getBuiltSchema();
    // } // if

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

  /**
   * Get the {@link Pair} associated with {@link OrderByElement}
   */
  private void getOrderByPairs() {

    final Iterator<OrderByElement> orderByItr = orderByElements.iterator();

    while (orderByItr.hasNext()) {
      final Column column = (Column) orderByItr.next().getExpression();
      final boolean flag = SchemaUtils.isTableNameColNameAvailable(column);
      l1 : for (final Pair<Integer, Integer> pair : builtSchema) {
        // if table name and column name is present
        if (flag) {
          if (SchemaManager.getTableId(column.getTable().getName()) == pair
              .getKey()
              && SchemaManager.getColumnIdByTableId(pair.getKey(),
                  column.getColumnName()) == pair.getValue()) {
            this.orderByPairs.add(pair);
            break l1;
          }
        } else {
          // no table id present, checking using pair tableid
          if (SchemaManager.getColumnIdByTableId(pair.getKey(),
              column.getColumnName()) == pair.getValue()) {
            this.orderByPairs.add(pair);
            break l1;
          }
        } // else

      } // for
    } // while
  }
}
