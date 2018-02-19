package edu.buffalo.www.cse4562.util;

import java.util.Iterator;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;

/**
 * Print a tuple to the console.
 *
 * @author varunjai
 *
 */
public class TuplePrinter {

  /**
   * 
   * @param tuple
   *          !null
   */
  public static void printTuple(Tuple tuple) {
    // null check
    if(null == tuple){
      return;
    }
    
    final StringBuilder stringBuidler = new StringBuilder();

    final Iterator<ColumnCell> columnCellItr = tuple.getColumnCells()
        .iterator();

    while (columnCellItr.hasNext()) {
      stringBuidler.append(columnCellItr.next().getCellValue());
      stringBuidler.append("|");
    }

    System.out.println(stringBuidler.substring(0, stringBuidler.length() - 1));
  }
}
