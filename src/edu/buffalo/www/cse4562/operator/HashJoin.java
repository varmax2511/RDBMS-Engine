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
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.ExpressionDecoder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class HashJoin {
  
  
  public static Collection<Collection<Tuple>> getNextByHashJoin(Node firstChild, Node secondChild, Expression expression) throws Throwable{
    
    // for each tuple in first table 
    //    for each cell in the tuple
     //       for each Columncell in decoded columns
    //            if(cell corresponds to the column) get value and calculate hash
    //    store row in hash table with hashed value as key
    //populateHashTable(firstChild,expression);
    
    return null;


  }
  public static Map<Integer,List<Tuple>> populateHashTable(Node firstChild,Expression expression) throws Throwable{
    
    Map<Integer,List<Tuple>> hashTable= new HashMap<>();

    ExpressionDecoder expDecoder = new ExpressionDecoder(expression);
    List<Column> decodedColumns = expDecoder.getDecodedColumns();

    while(firstChild.hasNext()) {
      Collection<Tuple> firstTuples =  firstChild.getNext();
      
      for(Tuple tuple: firstTuples) {
        int hashedValue = 1;

        for(Column column: decodedColumns) {
          for(ColumnCell cell : tuple.getColumnCells()) {
              if(columnsMatch(cell,column)) {
                hashedValue = getHashedValue(cell.getCellValue(),hashedValue);
                break ;
              }
            }
        }
        if(hashTable.containsKey(hashedValue)) {
          hashTable.get(hashedValue).add(tuple);
        }
        else { 
          List<Tuple> tuples=new ArrayList<>();
          tuples.add(tuple);
          hashTable.put(hashedValue, tuples);
        }
      }
    }
    firstChild.close();
    //firstChild.open();
    return hashTable;
  }
  
  
  public static int getHashedValue(PrimitiveValue cellValue, int hashedValue) {
    final int prime = 31;
    hashedValue = prime * hashedValue + ((cellValue.toString() == null) ? 0 : cellValue.toString().hashCode());
    return hashedValue;
  }

  public static boolean columnsMatch(ColumnCell cell, Column column) {
    if(column.getColumnName().equals(SchemaManager.getColumnNameById(cell.getTableId(), cell.getColumnId())))
      return true;
    return false;
  }


}
