package edu.buffalo.www.cse4562.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.operator.Operator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import net.sf.jsqlparser.expression.PrimitiveValue;

/**
 * This class represents a container for a single record returned by any
 * {@link Operator}. A tuple will hold several cell values pertaining to
 * different columns and data types.
 * 
 * Ideally I prefer a tuple consisting a collection of combination of values and
 * their associated data types. I also have a requirement of maintaining the
 * order of column cells as they appear in a row. An ordered collection like a
 * list should do that Q. How to handle null values? An array list can hold
 * multiple null values, this will be responsibility of the caller. example a
 * distinct operator may use a Set and selection operator may use an array list.
 * 
 * @author varunjai
 *
 */
public class Tuple implements Cloneable {

  private Collection<ColumnCell> columnCells;

  /**
   * 
   */
  public Tuple(){
    
  }
  /**
   * 
   * @param columnCells
   *          can be empty.
   */
  public Tuple(Collection<ColumnCell> columnCells) {
    // Validate.notEmpty(columnCells);
    this.columnCells = columnCells;
  }

  public Collection<ColumnCell> getColumnCells() {
    return columnCells;
  }

  /**
   * Check if tuple is empty or not.
   * 
   * @return
   */
  public boolean isEmpty() {
    if (CollectionUtils.isEmpty(columnCells)) {
      return true;
    }
    return false;
  }

  @Override
  public String toString() {

    return TuplePrinter.parseTuple(this);
  }
  
  public Tuple getCopy(){
    Tuple tuple = new Tuple();
    List<ColumnCell> columnCells = new ArrayList<>();
    for(ColumnCell columnCell : this.getColumnCells()){
      columnCells.add(columnCell.getCopy());
    }
    
    tuple.columnCells = columnCells;
    return tuple;
  }
  

  /**
   * Represents one cell value.
   * 
   * @author varunjai
   *
   */
  public static class ColumnCell implements Cloneable{
    private Integer tableId;
    private Integer columnId;
    private PrimitiveValue cellValue;
    
    /**
     * WARNING!!!! Don't use
     */
    public ColumnCell() {
      super();
    }

    /**
     * 
     * @param cellValue
     *          can be null.
     * @param colDataType
     *          can be null.
     */
    public ColumnCell(PrimitiveValue cellValue) {
      this.cellValue = cellValue;
    }

    public Integer getTableId() {
      return tableId;
    }

    public void setTableId(Integer tableId) {
      this.tableId = tableId;
    }

    public Integer getColumnId() {
      return columnId;
    }

    public void setColumnId(Integer columnId) {
      this.columnId = columnId;
    }

    public PrimitiveValue getCellValue() {
      return cellValue;
    }

    public void setCellValue(PrimitiveValue cellValue) {
      this.cellValue = cellValue;
    }

    @Override
    public String toString() {

      StringBuilder builder = new StringBuilder();
      builder.append(tableId == null ? "" : tableId.toString());
      builder.append("|");
      builder.append(columnId == null ? "" : columnId.toString());
      builder.append("|");
      builder.append(cellValue == null ? "" : cellValue.toString());

      return builder.toString();
    }

    public ColumnCell getCopy(){
        ColumnCell columnCell = new ColumnCell();
        columnCell.setColumnId(this.columnId.intValue());
        columnCell.setTableId(this.tableId.intValue());
        columnCell.setCellValue(this.cellValue);
        return columnCell;
    }
    
  }

}
