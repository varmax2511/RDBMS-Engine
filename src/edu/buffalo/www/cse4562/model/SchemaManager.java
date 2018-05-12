package edu.buffalo.www.cse4562.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

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
 *
 *  TODO: We store all tableName -> table schema and tableAlias -> table schema
 *  in the mamager which has application scope, but tableAlias -> table schema
 *  has query scope.
 * </pre>
 *
 * @author varunjai
 *
 */
public class SchemaManager {

  private static int tableCount = 1;
  private static final Map<String, TableSchema> tableName2Schema = new HashMap<>();
  private static final Map<String, Integer> tableName2Id = new HashMap<>();
  private static final Map<Integer, Map<String, Integer>> tableId2ColName2Id = new HashMap<>();
  private static final Map<Integer, Map<Integer, Integer>> tableId2ColId2Index = new HashMap<>();


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
    
    if(tableName2Schema.containsKey(tableName)){
      tableName2Schema.put(tableName, tableSchema);
      addColumnIds(tableName2Id.get(tableName), tableSchema);
      return;
    }
    
    tableName2Schema.put(tableName, tableSchema);
    tableName2Id.put(tableName, tableCount);
    tableCount++;

    addColumnIds(tableCount - 1, tableSchema);
  }

  /**
   * Add new columns to existing tables.
   * 
   * @param tableId
   * @param tableSchema
   */
  private static void addColumnIds(Integer tableId, TableSchema tableSchema) {
    final Map<String, Integer> colName2Id = new HashMap<>();
    final List<ColumnDefinition> colDefinitions = tableSchema
        .getColumnDefinitions();

    for (int i = 0; i < colDefinitions.size(); i++) {
      colName2Id.put(colDefinitions.get(i).getColumnName(), i + 1);
    } // for
    tableId2ColName2Id.put(tableId, colName2Id);
  }

  public static void updateSchema(Integer tableId, TableSchema tableSchema) {

    tableName2Schema.put(getTableName(tableId), tableSchema);
    addColumnIds(tableId, tableSchema);
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

  public static TableSchema getTableSchemaById(Integer tableId) {

    return tableName2Schema.get(getTableName(tableId));
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

  /**
   * Returns the column name against a given table id and column id. This is an
   * O(n) call where n is the number of columns in the table.
   * 
   * @param tableId
   * @param columnId
   * @return null when no match found
   */
  public static String getColumnNameById(Integer tableId, Integer columnId) {

    if (!tableId2ColName2Id.containsKey(tableId)) {
      return null;
    }

    for (Map.Entry<String, Integer> entry : tableId2ColName2Id.get(tableId)
        .entrySet()) {
      if (entry.getValue() == columnId) {
        return entry.getKey();
      }
    } // for

    return null;
  }

  /**
   * Get the table name corresponding to a tableid. This is O(n) call where n is
   * the number of tables in the system.
   * 
   * @param tableId
   * @return null when no match found
   */
  public static String getTableName(Integer tableId) {

    if (!tableId2ColName2Id.containsKey(tableId)) {
      return null;
    }

    for (Map.Entry<String, Integer> entry : tableName2Id.entrySet()) {
      if (entry.getValue() == tableId) {
        return entry.getKey();
      }
    } // for

    return null;
  }

  /**
   * Get the alias in the {@link SelectExpressionItem} and add it as a new
   * column in the {@link TableSchema}. Register the changes in table schema
   * with the {@link SchemaManager}.
   *
   * @param strVal
   *          TODO
   * @param tableId
   */
  public static void addColumnAliasToSchema(String strVal, Integer tableId) {
    final TableSchema tableSchema = getTableSchemaById(tableId);
    final List<ColumnDefinition> columnDefinitions = tableSchema
        .getColumnDefinitions();
    final ColumnDefinition columnDefinition = new ColumnDefinition();
    columnDefinition.setColumnName(strVal);
    columnDefinitions.add(columnDefinition);
    tableSchema.setColumnDefinitions(columnDefinitions);
  
    // update schema
    updateSchema(tableId, tableSchema);
  }
  
  public static void addIndexToColumn(String tableName,String columnName ) {
    if(tableId2ColId2Index.containsKey(getTableId(tableName))) {
      Integer indexKey = tableId2ColId2Index.get(getTableId(tableName)).size();
      tableId2ColId2Index.get(getTableId(tableName)).put(getColumnIdByTableId(getTableId(tableName), columnName), indexKey);
      return;
    }
    Map<Integer,Integer> col2Index = new HashMap<>();
    col2Index.put(getColumnIdByTableId(getTableId(tableName), columnName), 0);
    tableId2ColId2Index.put(getTableId(tableName), col2Index);
  }
  
  public static Integer getIndexValueForColumnId(Integer tableId, Integer columnId) {
    if(tableId2ColId2Index.containsKey(tableId) && tableId2ColId2Index.get(tableId).containsKey(columnId))
    return tableId2ColId2Index.get(tableId).get(columnId);
    return null;
  }
  public static Integer getIndexValueForColumnName(String tableName, String columnName) {
    int tableId=getTableId(tableName);
    int columnId = getColumnIdByTableId(tableId, columnName);
    if(tableId2ColId2Index.containsKey(tableId) && tableId2ColId2Index.get(tableId).containsKey(columnId))
    return tableId2ColId2Index.get(tableId).get(columnId);
    return null;
  }
  
  public static Map<Integer,Integer> getIndexedColumnsMap(Integer tableId) {
    if(tableId2ColId2Index.containsKey(tableId)) return tableId2ColId2Index.get(tableId);
    return null;
  }
  
  public static boolean isColumnIndexed(Integer tableId, Integer columnId) {
    if(tableId2ColId2Index.containsKey(tableId) && tableId2ColId2Index.get(tableId).containsKey(columnId))
      return true;
    return false;
  }
  
}
