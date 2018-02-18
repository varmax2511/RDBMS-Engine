package edu.buffalo.www.cse4562.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * <pre>
 * Static map containing table name to {@link TableSchema} mapping.
 * This class will hold in memory:
 * 1. Schema of all tables.
 * 2. Table name to integer mapping. This will be used as table id.
 * 3. Column name to integer mapping. This will be used as column id.
 *
 * Why need table id and column id?
 * We can have queries where certain records from one table are projected and
 * certain records from another table.
 * Presently our {@link ColumnCell} contains information about its value, its
 * data type and its column name. It needs to hold its table name as well.
 * Storing so many Strings in memory is expensive, its better to store an Integer
 * of 4 bytes which can be looked upon when needed via the Schema Manager.
 *
 * <b>NOTE</b> : Not thread safe and not cloned.
 *
 * </pre>
 *
 * @author varunjai
 *
 */
public class SchemaManager {

  private static int tableCount = 1;
  private static Map<String, TableSchema> tableName2Schema = new HashMap<>();
  private static Map<String, Integer> tableName2Id = new HashMap<>();
  private static Map<Integer, Map<String, Integer>> tableId2ColName2Id = new HashMap<>();

  /**
   * Add table schema entry. Assign a table id to the table name and assign
   * column ids to columns in each table.
   * <p>
   * Though each table id is unique, column ids are unique within a table and
   * not across multiple tables.
   *
   * @param tableName
   * @param tableSchema
   */
  public static void addTableSchema(String tableName, TableSchema tableSchema) {
    tableName2Schema.put(tableName, tableSchema);

    tableName2Id.put(tableName, tableCount);
    tableCount++;

    final Map<String, Integer> colName2Id = new HashMap<>();
    int colCount = 1;
    final List<ColumnDefinition> colDefinitions = new ArrayList<>();

    for (int i = 0; i < colDefinitions.size(); i++) {
      colName2Id.put(colDefinitions.get(i).getColumnName(), colCount);
      colCount++;
    } // for
    tableId2ColName2Id.put(tableCount, colName2Id);
  }

  /**
   * Get the {@link TableSchema} by table name.
   *
   * @param tableName
   *          !blank.
   * @return
   */
  public static TableSchema getTableSchema(String tableName) {

    Validate.notBlank(tableName);
    return tableName2Schema.get(tableName);
  }

  /**
   * Get the table id for a table name.
   * 
   * @param tableName
   * @return
   */
  public static Integer getTableId(String tableName) {
    return tableName2Id.get(tableName);
  }

  /**
   * Get the column id by table id and column name.
   * 
   * @param tableId
   * @param columnName
   * @return
   */
  public static Integer getColumnIdByTableId(Integer tableId,
      String columnName) {
    return tableId2ColName2Id.get(tableId).get(columnName);
  }
}
