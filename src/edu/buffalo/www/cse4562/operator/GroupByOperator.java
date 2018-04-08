/**
 * 
 */
package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class GroupByOperator extends Node implements BlockingOperator{
  
  private List<Column> groupByColumnReferences;
  
  public static final Map<Integer, List<Tuple>> groupByRows = new HashMap<>();
  
  public GroupByOperator(List<Column> groupByColumnReferences) {
    Validate.notNull(groupByColumnReferences);
    this.groupByColumnReferences = groupByColumnReferences;
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {
    builtSchema = getChildren().get(0).getBuiltSchema();
    return builtSchema;
  }

  @Override
  public Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable {
    
    final List<Tuple> groupByOutputs = new ArrayList<>();

    // null check
    if (tupleCollection == null || tupleCollection.size() == 0) {
      return groupByOutputs;
    }

    // unary operator, interested in only the first collection
    final List<Tuple> tupleRecords = (List<Tuple>) tupleCollection.iterator()
        .next();

    // empty check
    if (CollectionUtils.areTuplesEmpty(tupleRecords)) {
      return groupByOutputs;
    }
    
    int hashedKey=getHashCode();
    for(Tuple tuple:tupleRecords) {
      if(groupByRows.containsKey(hashedKey)) {
        updateRow(hashedKey,tuple);
      }
      else {
        List<Tuple> tuples=new ArrayList<>();  
        tuples.add(tuple);
        groupByRows.put(hashedKey, tuples);
      }
    }
    
    //Collections.sort(tupleRecords, new TupleComparator(builtSchema));
    // sort in reverse
    //if (!orderByElements.get(0).isAsc()) {
      //Collections.reverse(tupleRecords);
    //}

    return tupleRecords;
  }

  private void updateRow(int hashedKey,Tuple tuple) {
    List<Tuple> tuples=groupByRows.get(hashedKey);  
    tuples.add(tuple);
  }

  public int getHashCode() {
    final int prime = 31;
    int result = 1;
    for(Column column: groupByColumnReferences) {
      result = prime * result + ((column == null) ? 0 : column.hashCode());
    }
    return result;
  }
}
