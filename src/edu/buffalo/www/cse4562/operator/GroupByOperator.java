/**
 * 
 */
package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.MapUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class GroupByOperator extends Node implements BlockingOperator{
  
  private List<Column> groupByColumnReferences;

  private final Map<Integer, List<Tuple>> groupByRows = new LinkedHashMap<>();
  @SuppressWarnings("rawtypes")
  private Iterator it;
  
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

    for(Tuple tuple:tupleRecords) {
      int hashedKey=getHashCode(tuple);
      if(groupByRows.containsKey(hashedKey)) {
        updateRow(hashedKey,tuple);
      }
      else {
        
        List<Tuple> tuples=new ArrayList<>();  
        tuples.add(tuple);
        groupByRows.put(hashedKey, tuples);
      }
    }
    
    return tupleRecords;
  }


  private void updateRow(int hashedKey,Tuple tuple) {
    List<Tuple> tuples=groupByRows.get(hashedKey);  
    tuples.add(tuple);
  }

  public int getHashCode(Tuple tuple) {
    final int prime = 31;
    int result = 1;
    List<ColumnCell> cells = (List<ColumnCell>) tuple.getColumnCells();
    for(Column column: groupByColumnReferences) {
      for(final ColumnCell cell:cells) {
        if(!column.getColumnName().equals(SchemaManager.getColumnNameById(cell.getTableId(), cell.getColumnId())))
          continue;
        result = prime * result + ((cell.getCellValue().toString() == null) ? 0 : cell.getCellValue().toString().hashCode());
        break;
      }
    }
    //System.out.println(result);

    return result;
  }

  public List<Column> getGroupByColumnReferences() {
    return groupByColumnReferences;
  }

  public void setGroupByColumnReferences(List<Column> groupByColumnReferences) {
    this.groupByColumnReferences = groupByColumnReferences;
  }

  public Map<Integer, List<Tuple>> getGroupByRows() {
    return groupByRows;
  }
  
  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Collection<Tuple> getNext() throws Throwable {
    // check child count, should be 1
    if(MapUtils.isEmpty(groupByRows)) {
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

    process(tupleCollection);
    it = groupByRows.entrySet().iterator();
    }
    
    final Collection<Tuple> tupleBlock = new ArrayList<>();
    if (it.hasNext()) {

        Map.Entry pair = (Map.Entry)it.next();
        tupleBlock.addAll((List<Tuple>) pair.getValue());
        it.remove();
    } 
    return tupleBlock;
  }
  
  @Override
  public boolean hasNext() throws IOException {
    return super.hasNext() || this.it.hasNext();
  }
}
