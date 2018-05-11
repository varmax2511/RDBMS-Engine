package edu.buffalo.www.cse4562.model;

import java.util.List;

import edu.buffalo.www.cse4562.preprocessor.TableStats;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
/**
 * This class models the table schema.
 * 
 */
public class TableSchema {
  private String tableName;
  private List<ColumnDefinition> columnDefinitions;
  private TableStats tableStats;

  /**
   * 
   * @param tableName
   *         !blank
   * @param columnDefinitions
   *         !empty, null
   */
  public TableSchema(String tableName,
      List<ColumnDefinition> columnDefinitions) {
    // null check
    Validate.notBlank(tableName);
    this.setTableName(tableName);
    this.setColumnDefinitions(columnDefinitions);
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<ColumnDefinition> getColumnDefinitions() {
    return columnDefinitions;
  }

  public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
    this.columnDefinitions = columnDefinitions;
  }

  public TableStats getTableStats() {
    return tableStats;
  }

  public void setTableStats(TableStats tableStats) {
    this.tableStats = tableStats;
  }

}
