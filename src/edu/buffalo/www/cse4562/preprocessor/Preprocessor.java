/**
 *
 */
package edu.buffalo.www.cse4562.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.MapUtils;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;

/**
 * Pre-computes certain details of the table
 *
 * - cardinality - primary & candidate keys - min and max of indexable columns -
 * kind of indexing required
 *
 * For now just creating indices based on range assuming equal distribution
 *
 * h(x) = x/1000
 *
 * @author Sneha Mehta
 *
 */
public class Preprocessor {

  private static final String PRIMARY_KEY = "PRIMARY KEY";
  private static final String REFERENCES = "REFERENCES";
  private static final String PRIMARY = "PRIMARY";
  // configurable bucket count
  public static int BUCKETS = 30;

  /**
   * Perform pre-processing.
   * 
   * @param tableSchema
   * @param indices
   * @throws IOException
   */
  public static void preprocess(final TableSchema tableSchema,
      final List<Index> indices) throws IOException {

    /*
     * Get the index mappings
     */

    // This nonsense works only when there is a composite primary key of form
    // PRIMARY KEY (L_ORDERKEY, L_LINENUMBER)
    if (!CollectionUtils.isEmpty(indices)) {
      for (final Index index : indices) {
        if (index.getType().equals(PRIMARY_KEY)) {
          // add column details to the map

          // :TODO ignoring composite key for now
          index.getColumnsNames();
        }
      }
    } // if

    // Checking for simple primary key if not composite
    for (final ColumnDefinition colDef : tableSchema.getColumnDefinitions()) {

      if (CollectionUtils.isEmpty(colDef.getColumnSpecStrings())) {
        continue;
      }

      if ((colDef.getColumnSpecStrings().get(0).equals(PRIMARY))){
        //  || colDef.getColumnSpecStrings().get(0).equals(REFERENCES))) {
        // add column details to the map
        SchemaManager.addIndexToColumn(tableSchema.getTableName(),
            colDef.getColumnName());
        // System.out.println(tableSchema.getTableName()+""+colDef.getColumnName());
        // System.out.println("sgd " + colDef.getColumnName());
      }
    } // for

    final Map<Integer, Integer> col2Index = SchemaManager.getIndexedColumnsMap(
        SchemaManager.getTableId(tableSchema.getTableName()));

    /*
     * phase 1 : Scan for table statistics
     */
    final TableStats stats = collectTableStats(
        ApplicationConstants.DATA_DIR_PATH + tableSchema.getTableName()
            + ApplicationConstants.SUPPORTED_FILE_EXTENSION,
        col2Index);

    // update table schema with stats
    tableSchema.setTableStats(stats);

    
    if(MapUtils.isEmpty(col2Index)) {
      return;
    }
    
    /*
     * phase 2 : Index data
     */
    SchemaManager.addRandomIndex(RandomIndex.indexData(
        ApplicationConstants.DATA_DIR_PATH + tableSchema.getTableName()
            + ApplicationConstants.SUPPORTED_FILE_EXTENSION,
        col2Index, stats.getColumnStats(), tableSchema.getTableName()));
   
    // invoke GC
    System.gc();
  }

  /**
   * Write the tuple to file. If file already exists then append.
   * 
   * @param tableName
   * @param indexValue
   * @param bucket
   * @param line
   * @throws IOException
   */
  private static void addToBucket(String tableName, int indexValue, int bucket,
      String line) throws IOException {

    FileWriter fileWriter = null;
    BufferedWriter bufferedWriter = null;
    try {
      final File file = new File(getDestination(tableName, indexValue, bucket));
      // create file
      if (!file.exists()) {
        file.createNewFile();
      }
      // append = true
      fileWriter = new FileWriter(file, true);
      bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write(line + "\n");

    } catch (final Throwable t) {
      // non-fatal
      System.err.println(
          "NON-FATAL Exception while writing index file: " + t.getMessage());
    } finally {
      /*
       * close all writers
       */
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
      if (fileWriter != null) {
        fileWriter.close();
      }

    } // finally
  }

  /**
   * Get the indexed file destination.
   * 
   * @param tableName
   * @param indexNumber
   * @param bucketNumber
   * @return
   */
  private static String getDestination(String tableName, int indexNumber,
      int bucketNumber) {
    return ApplicationConstants.INDEX_DIR_PATH + indexNumber + "_"
        + bucketNumber + "_" + tableName
        + ApplicationConstants.SUPPORTED_FILE_EXTENSION;
  }

  /**
   * Compute the bucket size for the index using the minimum and maximum value
   * of all items in a column and the cell value.
   * 
   * @param max
   * @param min
   * @param cellValue
   * @return
   */
  public static int hashedValue(int max, int min, int cellValue) {
    final int bucketSize = (int) Math.ceil((double) (max - min + 1) / BUCKETS);
    return (cellValue - min) / bucketSize;
  }

  /**
   * Collect Table statistics
   * 
   * @param filepath
   * @param col2Index
   * @return
   * @throws NumberFormatException
   * @throws IOException
   */
  public static TableStats collectTableStats(String filepath,
      final Map<Integer, Integer> col2Index)
      throws NumberFormatException, IOException {

    final TableStats stats = new TableStats();
    FileReader reader = null;
    BufferedReader bufferedReader = null;
    try {
      /*
       * Phase 1: Scan for stats
       */
      reader = new FileReader(filepath);
      bufferedReader = new BufferedReader(reader);
      int cardinality = 0;
      final Map<Integer, Pair<Integer, Integer>> columnStats = new HashMap<>();

      while (bufferedReader.ready() && col2Index != null) {
        cardinality++;

        final String line = bufferedReader.readLine();
        final String[] record = line.split(ApplicationConstants.DATA_DELIMITER);

        // iterate each column id
        for (final Integer columnId : col2Index.keySet()) {
          final int val = Integer.parseInt(record[columnId - 1]);
          // marking entry
          if (columnStats.get(columnId) == null) {
            columnStats.put(columnId, new Pair<Integer, Integer>(val, val));
            continue;
          }

          // checking for max
          if (columnStats.get(columnId).getKey() < val) {
            columnStats.put(columnId, new Pair<Integer, Integer>(val,
                columnStats.get(columnId).getValue()));
          }
          // checking for min
          if (columnStats.get(columnId).getValue() > val) {
            columnStats.put(columnId, new Pair<Integer, Integer>(
                columnStats.get(columnId).getKey(), val));
          }

        } // for
      } // while

      stats.setCardinality(cardinality);
      stats.setColumnStats(columnStats);
    } finally {
      /*
       * close all readers
       */
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (reader != null) {
        reader.close();
      }
    }

    return stats;
  }

  /**
   * index the data from the table using the metrics collected.
   * 
   * @param filepath
   * @param col2Index
   * @param columnStats
   * @param tableName
   * @throws IOException
   */
  public static void indexData(String filepath,
      final Map<Integer, Integer> col2Index,
      final Map<Integer, Pair<Integer, Integer>> columnStats, String tableName)
      throws IOException {
    FileReader reader = null;
    BufferedReader bufferedReader = null;
    try {
      // Phase 2: Scan for bucketing

      reader = new FileReader(filepath);
      bufferedReader = new BufferedReader(reader);
      while (bufferedReader.ready() && col2Index != null) {

        final String line = bufferedReader.readLine();
        final String[] record = line.split(ApplicationConstants.DATA_DELIMITER);

        for (final Integer columnId : col2Index.keySet()) {
          // get hash index bucket
          final int bucket = hashedValue(columnStats.get(columnId).getKey(),
              columnStats.get(columnId).getValue(),
              Integer.parseInt(record[columnId - 1]));

          // add to the bucket
          addToBucket(tableName, col2Index.get(columnId), bucket, line);
        } // for

      } // while
    } finally {

      /*
       * close all readers
       */
      if (bufferedReader != null) {
        bufferedReader.close();
      }
      if (reader != null) {
        reader.close();
      }
    }
  }
}
