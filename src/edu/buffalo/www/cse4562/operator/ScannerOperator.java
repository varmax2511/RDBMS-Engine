package edu.buffalo.www.cse4562.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.print.DocFlavor.READER;

import org.apache.commons.csv.CSVParser;

import edu.buffalo.www.cse4562.model.Container;
import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.ScannerContainer;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.preprocessor.Preprocessor;
import edu.buffalo.www.cse4562.preprocessor.TableStats;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.PrimitiveTypeConverter;
import edu.buffalo.www.cse4562.util.StringUtils;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * This class contains the implementation of the Scanner operator which is
 * responsible for fetching data from file on disk.
 *
 */
public class ScannerOperator extends Node implements UnaryOperator {

  private FileReader reader;
  private BufferedReader br;
  private final Config config;
  private RandomAccessFile indexedFile;
  int i=0;
  /**
   *
   * @param config
   *          !null.
   */
  public ScannerOperator(Config config) {
    Validate.notNull(config);
    this.config = config;
  }

  @Override
  public void open() throws IOException {
    reader = new FileReader(config.getDataParentPath() + config.getTableName()
        + ApplicationConstants.SUPPORTED_FILE_EXTENSION);
    br = new BufferedReader(reader);
  }

  /**
   *
   * @return
   * @throws IOException
   */
  @Override
  public Collection<Tuple> getNext(Container container) throws IOException {
    // check for container and make index scan
    if (container != null && container instanceof ScannerContainer) {
      //return processIndexScan((ScannerContainer) container);
     // System.out.println("index scan"+ i++);
      return processIndexScanRandomAccess((ScannerContainer)container);
    }
    return process();
  }

  private Collection<Tuple> processIndexScanRandomAccess(ScannerContainer container) throws IOException
  {
    if(indexedFile == null) {
      indexedFile = new RandomAccessFile(getIndexPath(container),"rw");
      }
    indexedFile.seek(container.getValue());
    final String line = indexedFile.readLine();
    String[] record = line.split(ApplicationConstants.DATA_DELIMITER);
    final List<ColumnCell> columnCells = new ArrayList<>();
    Collection<Tuple> tuples = new ArrayList<>();

    final TableSchema tableSchema = SchemaManager
        .getTableSchema(config.getTableName());
    Integer tableId = SchemaManager.getTableId(config.getTableName());
    
      for (int j = 0; j < record.length; j++) {
        final ColumnDefinition colDefinition = tableSchema
            .getColumnDefinitions().get(j);

        final ColumnCell colCell = new ColumnCell(
            PrimitiveTypeConverter.getPrimitiveValueByColDataType(
                colDefinition.getColDataType(), record[j]));

        colCell.setTableId(tableId);
        colCell.setColumnId(SchemaManager.getColumnIdByTableId(tableId,
            colDefinition.getColumnName()));
        columnCells.add(colCell);

      } // for

      tuples.add(new Tuple(columnCells));
    
    return tuples;
  }
  
  private String getIndexPath(ScannerContainer container) {
    // TODO Auto-generated method stub
    return ApplicationConstants.INDEX_DIR_PATH + container.getTableName() + "_" +container.getColumnId() + ApplicationConstants.SUPPORTED_FILE_EXTENSION;
  }
/*
  private Collection<Tuple> processIndexScan(ScannerContainer container)
      throws NumberFormatException, IOException {
    TableSchema tableSchema = SchemaManager
        .getTableSchema(container.getTableName());
    TableStats tableStats = tableSchema.getTableStats();
    int max = tableStats.getColumnStats().get(container.getColumnId()).getKey();
    int min = tableStats.getColumnStats().get(container.getColumnId())
        .getValue();
    int tableId = SchemaManager.getTableId(container.getTableName());
    int bucketNo = Preprocessor.hashedValue(max, min, container.getValue());
    Collection<Tuple> tuples = new ArrayList<>();
    File file = new File(getTablePath(container.getIndexNo(), bucketNo,
        container.getTableName()));
    if (!file.exists())
      return tuples;
    reader = new FileReader(getTablePath(container.getIndexNo(), bucketNo,
        container.getTableName()));
    br = new BufferedReader(reader);
    String line;
    while ((line = br.readLine()) != null) {
      String[] record = line.split("\\|");
      if (Integer.parseInt(record[container.getColumnId() - 1]) == container
          .getValue()) {
        final List<ColumnCell> columnCells = new ArrayList<>();

        for (int j = 0; j < record.length; j++) {
          final ColumnDefinition colDefinition = tableSchema
              .getColumnDefinitions().get(j);

          final ColumnCell colCell = new ColumnCell(
              PrimitiveTypeConverter.getPrimitiveValueByColDataType(
                  colDefinition.getColDataType(), record[j]));

          colCell.setTableId(tableId);
          colCell.setColumnId(SchemaManager.getColumnIdByTableId(tableId,
              colDefinition.getColumnName()));
          columnCells.add(colCell);

        } // for

        tuples.add(new Tuple(columnCells));
      }
    }
    return tuples;
  }
  private static String getTablePath(int indexNo, int bucketNo,
      String tableName) {
    return ApplicationConstants.INDEX_DIR_PATH + indexNo + "_" + bucketNo + "_"
        + tableName + ApplicationConstants.SUPPORTED_FILE_EXTENSION;
  }
  */
  /**
   * process the request.
   *
   * @return
   * @throws IOException
   */
  private Collection<Tuple> process() throws IOException {

    // if method invoked first time without connection being opened
    /*
     * if (null == reader) { open(); }
     */

    // if no records left to iterate
    if (!br.ready()) {
      close();
      return new ArrayList<>();
    }

    final TableSchema tableSchema = SchemaManager
        .getTableSchema(config.getTableName());
    Integer tableId = SchemaManager.getTableId(config.getTableName());
    /*
     * This is use case for queries like SELECT P.A, P.B FROM R AS P WHERE P.A >
     * 4 AND P.B > 1; We will point the same reference of table schema and not a
     * copy of it.
     */
    if (!StringUtils.isBlank(config.getAlias())) {
      // SchemaManager.addTableSchema(config.getAlias(), tableSchema);
      tableId = SchemaManager.getTableId(config.getAlias());
    }

    final List<Tuple> tuples = new ArrayList<>();
    for (int i = 0; i < config.getChunkSize(); i++) {

      // no value to iterate
      if (!br.ready()) {
        close();
        break;
      }

      // fetch a row
      final String[] record = br.readLine()
          .split(ApplicationConstants.DATA_DELIMITER);
      final List<ColumnCell> columnCells = new ArrayList<>();

      for (int j = 0; j < record.length; j++) {
        final ColumnDefinition colDefinition = tableSchema
            .getColumnDefinitions().get(j);

        final ColumnCell colCell = new ColumnCell(
            PrimitiveTypeConverter.getPrimitiveValueByColDataType(
                colDefinition.getColDataType(), record[j]));

        colCell.setTableId(tableId);
        colCell.setColumnId(SchemaManager.getColumnIdByTableId(tableId,
            colDefinition.getColumnName()));
        columnCells.add(colCell);

      } // for

      tuples.add(new Tuple(columnCells));
    } // for

    return tuples;
  }

  /**
   * Close the {@link CSVParser} and {@link READER}.
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    if (null == reader) {
      return;
    }

    // try {
    // csvParser.close();
    // br.close();
    // reader.close();
    // scnr.close();
    // } catch (final IOException e) {

    // re-attempt
    // csvParser.close();
    // br.close();
    // reader.close();
    // scnr.close();
    // }
    if(indexedFile!=null)
    indexedFile.close();

  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws IOException {
    return getNext(null);
  }

  @Override
  public boolean hasNext() throws IOException {

    // if method invoked first time without connection being opened
    if (null == reader) {
      return false;
    }
    
    
    return br.ready();
    // return scnr.hasNextLine();
    // return this.recordIterator.hasNext();
  }

  @Override
  public List<Pair<Integer, Integer>> getBuiltSchema() {

    // build schema if not yet built
    if (CollectionUtils.isEmpty(builtSchema)) {
      buildSchema();
    } // if
    return builtSchema;
  }

  /**
   * Populate the builtSchema from the table name and alias information.
   */
  private void buildSchema() {
    final TableSchema tableSchema = SchemaManager
        .getTableSchema(config.getTableName());
    Integer tableId = SchemaManager.getTableId(config.getTableName());

    // if alias
    if (!StringUtils.isBlank(config.getAlias())) {
      SchemaManager.addTableSchema(config.getAlias(), tableSchema);
      tableId = SchemaManager.getTableId(config.getAlias());
    }

    // build schema
    for (final ColumnDefinition colDefinition : tableSchema
        .getColumnDefinitions()) {

      builtSchema.add(new Pair<Integer, Integer>(tableId, SchemaManager
          .getColumnIdByTableId(tableId, colDefinition.getColumnName())));
    } // for
  }

  /**
   * Configuration class for {@link ScannerOperator}
   *
   * @author varunjai
   *
   */
  public static class Config {
    private final String dataParentPath;
    private int chunkSize = 50;
    private final String tableName;
    private final String alias;

    /**
     *
     * @param dataParentPath
     * @param tableName
     *          !blank.
     * @param alias
     *          can be blank.
     */
    public Config(String tableName, String alias, String dataParentPath) {
      Validate.notBlank(tableName);

      if (null == SchemaManager.getTableSchema(tableName)) {
        throw new IllegalArgumentException(
            "Table with name: " + tableName + " does not exist!");
      }

      this.dataParentPath = dataParentPath;
      this.tableName = tableName;
      this.alias = alias;
    }

    public int getChunkSize() {
      return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
      this.chunkSize = chunkSize;
    }

    public String getDataParentPath() {
      return dataParentPath;
    }

    public String getTableName() {
      return tableName;
    }

    public String getAlias() {
      return alias;
    }

  }

}
