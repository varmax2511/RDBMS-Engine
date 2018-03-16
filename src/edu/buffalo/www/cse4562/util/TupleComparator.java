package edu.buffalo.www.cse4562.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import net.sf.jsqlparser.expression.PrimitiveValue;
import net.sf.jsqlparser.expression.PrimitiveValue.InvalidPrimitive;
import net.sf.jsqlparser.schema.PrimitiveType;

/**
 * {@link Comparator} based on the configured table id and column id. The
 * comparator will fetch the particular column cell which matches in with the
 * table id and column id and compare the same with the other row.
 *
 * Order by can be done by multiple columns ORDER BY column1, column2, ...
 * ASC|DESC;
 *
 * Ordering will be based on column1 and in case of tie, column2 will be used
 * and so on so forth.
 *
 *
 * @author varunjai
 *
 */
public class TupleComparator implements Comparator<Tuple> {

  private final List<Pair<Integer, Integer>> tableIdColIdpair;
  /**
   *
   * @param tableId
   *          !null
   * @param columnId
   *          !null
   */
  public TupleComparator(List<Pair<Integer, Integer>> tableIdColIdpair) {
    Validate.notNull(tableIdColIdpair);
    this.tableIdColIdpair = tableIdColIdpair;
  }

  @Override
  public int compare(Tuple o1, Tuple o2) {

    final List<CompareObject> compareObjects = new ArrayList<>();

    for (final Pair<Integer, Integer> pair : tableIdColIdpair) {
      final CompareObject compareObj = new CompareObject();

      // not sure if terms will be in order
      final Iterator<ColumnCell> colCellItr1 = o1.getColumnCells().iterator();
      final Iterator<ColumnCell> colCellItr2 = o2.getColumnCells().iterator();

      int cnt = 0;
      while (colCellItr1.hasNext()) {
        cnt++;
        final ColumnCell colCell = colCellItr1.next();

        if (colCell.getTableId() == pair.getKey()
            && colCell.getColumnId() == pair.getValue()) {
          compareObj.setPrimitiveValue1(colCell.getCellValue());
          compareObj.setPrimitiveType(colCell.getCellValue().getType());
          break;
        } // if

      } // while

      int cnt2 = 0;
      while (colCellItr2.hasNext()) {
        cnt2++;
        final ColumnCell colCell = colCellItr2.next();

        // check against tuple 1 count and break when found
        if (cnt == cnt2) {
          compareObj.setPrimitiveValue2(colCell.getCellValue());
          break;
        }

      } // while

      compareObjects.add(compareObj);

    } // for

    for (final CompareObject compareObj : compareObjects) {
      final int retVal = getCompareValue(compareObj);

      // else the loop will continue and use the next attribute for comparison
      // and breaking the tie and so on...
      if (retVal != 0) {
        return retVal;
      }
    } // for

    // if nothing works, return as equal
    return 0;
  }

  /**
   *
   * @param val1
   * @param val2
   * @param primitiveType
   * @return
   */
  private int getCompareValue(CompareObject compareObject) {
    if (compareObject.getPrimitiveValue1() == null
        && compareObject.getPrimitiveValue2() == null) {
      return 0;
    }

    if (compareObject.getPrimitiveValue1() != null
        && compareObject.getPrimitiveValue2() == null) {
      return -1;
    }

    if (compareObject.getPrimitiveValue1() == null
        && compareObject.getPrimitiveValue2() != null) {
      return 1;
    }

    // string
    if (compareObject.getPrimitiveType() == PrimitiveType.STRING) {
      final String strVal1 = compareObject.getPrimitiveValue1().toRawString();
      final String strVal2 = compareObject.getPrimitiveValue2().toRawString();
      return strVal1.compareTo(strVal2);
    }
    // boolean
    if (compareObject.getPrimitiveType() == PrimitiveType.BOOL) {

      try {
        final Boolean boolVal1 = compareObject.getPrimitiveValue1().toBool();
        final Boolean boolVal2 = compareObject.getPrimitiveValue2().toBool();
        return boolVal1.compareTo(boolVal2);
      } catch (final InvalidPrimitive e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
    // date
    if (compareObject.getPrimitiveType() == PrimitiveType.LONG) {
      try {
        final Long longVal1 = compareObject.getPrimitiveValue1().toLong();
        final Long longVal2 = compareObject.getPrimitiveValue2().toLong();
        return longVal1.compareTo(longVal2);
      } catch (final InvalidPrimitive e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
    // double
    if (compareObject.getPrimitiveType() == PrimitiveType.DOUBLE) {

      try {
        final Double dblVal1 = compareObject.getPrimitiveValue1().toDouble();
        final Double dblVal2 = compareObject.getPrimitiveValue2().toDouble();
        return dblVal1.compareTo(dblVal2);
      } catch (final InvalidPrimitive e) {
        e.printStackTrace();
      }

    }

    return 0;
  }

  /**
   * Container class for Comparison data.
   * 
   * @author varunjai
   *
   */
  public static class CompareObject {
    private PrimitiveType primitiveType;
    private PrimitiveValue primitiveValue1;
    private PrimitiveValue primitiveValue2;

    /**
     * Get the {@link PrimitiveType}
     * 
     * @return
     */
    public PrimitiveType getPrimitiveType() {
      return primitiveType;
    }
    /**
     * see {@link #getPrimitiveType()}
     * 
     * @param primitiveType
     */
    public void setPrimitiveType(PrimitiveType primitiveType) {
      this.primitiveType = primitiveType;
    }
    /**
     * Get the {@link PrimitiveValue} for first {@link Tuple}
     * 
     * @return
     */
    public PrimitiveValue getPrimitiveValue1() {
      return primitiveValue1;
    }
    /**
     * see {@link #getPrimitiveValue1()}
     * 
     * @param primitiveValue1
     */
    public void setPrimitiveValue1(PrimitiveValue primitiveValue1) {
      this.primitiveValue1 = primitiveValue1;
    }
    /**
     * Get the {@link PrimitiveValue} for second {@link Tuple}
     * 
     * @return
     */
    public PrimitiveValue getPrimitiveValue2() {
      return primitiveValue2;
    }
    /**
     * see {@link #getPrimitiveValue2()}
     * 
     * @param primitiveValue2
     */
    public void setPrimitiveValue2(PrimitiveValue primitiveValue2) {
      this.primitiveValue2 = primitiveValue2;
    }

  }

}
