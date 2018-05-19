package edu.buffalo.www.cse4562.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.util.ApplicationConstants;

/**
 * To start: ---------- Create a file for each table, iterate each line in the
 * table. Get the current offset for the file and store the data. The position
 * for this record is offset + 1
 *
 *
 *
 * @author varunjai
 *
 */
public class RandomIndex {

  /**
   * 
   * @param filepath
   * @param col2Index
   * @param columnStats
   * @param tableName
   * @throws IOException
   */
  public static Map<Integer, Map<Integer, Map<Integer, Long>>> indexData(String filepath,
      final Map<Integer, Integer> col2Index,
      final Map<Integer, Pair<Integer, Integer>> columnStats, String tableName)
      throws IOException {

    final int tableId = SchemaManager.getTableId(tableName);
    final Map<Integer, Map<Integer, Map<Integer, Long>>> tableId2ColId2Offset = new HashMap<>();
    for (final int columnId : col2Index.keySet()) {
      if(tableId2ColId2Offset.containsKey(tableId)) {
        tableId2ColId2Offset.get(tableId).putAll(indexDatByCol(tableName, columnId));
        continue;
      }
      tableId2ColId2Offset.put(tableId, indexDatByCol(tableName, columnId));
    } // for

    return tableId2ColId2Offset;
  }

  /**
   * 
   * @param tableName
   * @param columnId
   * @return
   * @throws IOException
   */
  public static Map<Integer, Map<Integer, Long>> indexDatByCol(String tableName, int columnId)
      throws IOException {
    FileReader reader = null;
    BufferedReader bufferedReader = null;
    RandomAccessFile randomAccessFile = null;

    final Map<Integer, Map<Integer, Long>> colId2Id2Offset = new HashMap<>();
    final Map<Integer, Long> id2Offset = new HashMap<>();
    try {
      // Phase 2: Scan for bucketing

      final File file = new File(ApplicationConstants.INDEX_DIR_PATH + tableName
          + "_" + columnId + ".dat");
      randomAccessFile = new RandomAccessFile(file.getAbsolutePath(), "rw");
      // create file
      if (!file.exists()) {
        file.createNewFile();
      }

      reader = new FileReader(
          ApplicationConstants.DATA_DIR_PATH + tableName + ".dat");
      bufferedReader = new BufferedReader(reader);
      while (bufferedReader.ready()) {

        final String line = bufferedReader.readLine();
        final String[] record = line.split(ApplicationConstants.DATA_DELIMITER);
        final long currOffset = randomAccessFile.getFilePointer();

        randomAccessFile.writeBytes(line);
        randomAccessFile.writeBytes("\n");
        id2Offset.put(Integer.parseInt(record[columnId - 1]), currOffset);

      } // while

      colId2Id2Offset.put(columnId, id2Offset);
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
      if (randomAccessFile != null) {
        randomAccessFile.close();
      }
    }
    return colId2Id2Offset;
  }
}
