package edu.buffalo.www.cse4562.operator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import javax.print.DocFlavor.READER;

import org.apache.commons.csv.CSVParser;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
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

  //private Iterator<CSVRecord> recordIterator;
  private FileReader reader;
  private BufferedReader br;
  //private CSVParser csvParser;
  private final Config config;
  //File file;
  //Scanner scnr;
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
    /*reader = Files.newBufferedReader(
        Paths.get(config.getDataParentPath() + config.getTableName()
            + ApplicationConstants.SUPPORTED_FILE_EXTENSION));
    csvParser = new CSVParser(reader, CSVFormat.newFormat('|'));
    final Iterable<CSVRecord> csvRecords = csvParser.getRecords();
    recordIterator = csvRecords.iterator();*/
    reader = new FileReader(config.getDataParentPath() + config.getTableName()
    + ApplicationConstants.SUPPORTED_FILE_EXTENSION);
    br = new BufferedReader(reader);
     //file = new File(config.getDataParentPath() + config.getTableName()
    //+ ApplicationConstants.SUPPORTED_FILE_EXTENSION);
     //scnr = new Scanner(file);
    
  }

  /**
   *
   * @return
   * @throws IOException
   */
  @Override
  public Collection<Tuple> getNext() throws IOException {

    return process();
  }

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
    //String line;
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
      final String[] record = br.readLine().split("\\|");
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

    //try {
      //csvParser.close();
      //br.close();
      //reader.close();
      //scnr.close();
    //} catch (final IOException e) {

      // re-attempt
      //csvParser.close();
      //br.close();
      //reader.close();
      //scnr.close();
    //}

  }

  @Override
  public Collection<Tuple> process(Collection<Collection<Tuple>> tuples)
      throws IOException {
    return getNext();
  }

  @Override
  public boolean hasNext() throws IOException {
    // if method invoked first time without connection being opened
    if (null == reader) {
      return false;
    }
    return br.ready();
//return scnr.hasNextLine();
    //return this.recordIterator.hasNext();
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
    private int chunkSize = 10;
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
