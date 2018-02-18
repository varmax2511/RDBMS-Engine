package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * This class contains the implementation of the Scanner operator which is
 * responsible for fetching data from file on disk.
 *
 */
public class ScannerOperator implements Operator {

  private final Table table;
  private final String dataParentPath;
  private Iterator<CSVRecord> recordIterator;
  private final String tableName;
  private Reader reader;
  private int chunkSize = 1;

  public ScannerOperator(Table table, String dataParentPath) {
    Validate.notNull(table);

    // validate table name
    String tableName = table.getName();

    if (null == SchemaManager.getTableSchema(tableName)) {
      throw new IllegalArgumentException(
          "Table with name: " + tableName + "does not exist!");
    }
    this.table = table;
    this.dataParentPath = dataParentPath;
    this.tableName = tableName;
  }

  public void open() throws IOException {
    reader = Files.newBufferedReader(
        Paths.get(this.dataParentPath + this.tableName + ".csv"));
    CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    Iterable<CSVRecord> csvRecords = csvParser.getRecords();
    recordIterator = csvRecords.iterator();
  }

  public Collection<Tuple> getNext() throws IOException {

    List<Tuple> tuples = new ArrayList<>();
    tuples.add(process());
    return tuples;
  }

  public Tuple process() throws IOException {

    // if method invoked first time without connection being opened
    if (null == reader) {
      open();
    }

    // if no records left to iterate
    if (!recordIterator.hasNext()) {
      close();
      return new Tuple(new ArrayList<>());
    }

    List<ColumnCell> columnCells = new ArrayList<>();
    TableSchema tableSchema = SchemaManager.getTableSchema(tableName);
    l1 : for (int i = 0; i < chunkSize; i++) {

      // no value to iterate
      if (!recordIterator.hasNext()) {
        break;
      }

      // fetch a row
      final String[] values = recordIterator.next().get(0).split("\\|");

      l2 : for (int j = 0; j < values.length; j++) {
        ColumnDefinition colDefinition = tableSchema.getColumnDefinitions()
            .get(j);
        ColumnCell colCell = new ColumnCell(values[j],
            colDefinition.getColDataType());
        colCell.setColumnName(colDefinition.getColumnName());
        columnCells.add(colCell);
      } // for
    }
    return new Tuple(columnCells);
  }

  public void close() throws IOException {
    if (null == reader) {
      return;
    }

    reader.close();
  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples)
      throws IOException {
    return getNext();
  }

}
