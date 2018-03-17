package edu.buffalo.www.cse4562.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    System.out.println(parseTuple(tuple));
  }

  /**
   * 
   * @param tuple
   * @return
   */
  public static String parseTuple(Tuple tuple) {
    // null check
    if (null == tuple) {
      return null;
    }

    final StringBuilder stringBuidler = new StringBuilder();

    final Iterator<ColumnCell> columnCellItr = tuple.getColumnCells()
        .iterator();

    while (columnCellItr.hasNext()) {
      stringBuidler.append(columnCellItr.next().getCellValue());
      stringBuidler.append("|");
    }

    // if empty
    if (stringBuidler.length() == 0) {
      return null;
    }

    return stringBuidler.substring(0, stringBuidler.length() - 1);
  }

  public static Collection<Tuple> getTupleCopy(Collection<Tuple> tuples) {
    if (CollectionUtils.isEmpty(tuples)) {
      return tuples;
    }

    List<Tuple> output = new ArrayList<>();
    for (Tuple tuple : tuples) {
      if(tuple == null || CollectionUtils.isEmpty(tuple.getColumnCells())){
        continue;
      }
      output.add(tuple.getCopy());
    } // for

    return output;

  }
}
