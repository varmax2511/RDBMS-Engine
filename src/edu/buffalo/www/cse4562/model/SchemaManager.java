package edu.buffalo.www.cse4562.model;

import java.util.HashMap;
import java.util.Map;

import edu.buffalo.www.cse4562.util.Validate;

/**
 * Static map containing table name to {@link TableSchema} mapping.
 * <p>
 * <b>NOTE</b> : Not thread safe and not cloned.
 * 
 * @author varunjai
 *
 */
public class SchemaManager {

  private static Map<String, TableSchema> tableName2Schema = new HashMap<>();

  /**
   * Add table schema entry.
   * 
   * @param tableName
   * @param tableSchema
   */
  public static void addTableSchema(String tableName, TableSchema tableSchema) {
    tableName2Schema.put(tableName, tableSchema);
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

}