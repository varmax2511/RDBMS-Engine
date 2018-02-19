package edu.buffalo.www.cse4562;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;

/**
 * Print a tuple to the console.
 * 
 * @author varunjai
 *
 */
public class TuplePrinter {

  public static void printTuple(Tuple tuple) {
    StringBuilder stringBuidler = new StringBuilder();

    for (ColumnCell cell : tuple.getColumnCells()) {
      stringBuidler.append(cell.getCellValue());
      stringBuidler.append("|");
    }

    System.out.println(stringBuidler.substring(0, stringBuidler.length() - 1));
  }
}
