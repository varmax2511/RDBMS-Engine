package edu.buffalo.www.cse4562.operator;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.print.DocFlavor.READER;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.PrimitiveTypeConverter;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * This class contains the implementation of the Scanner operator which is
 * responsible for fetching data from file on disk.
 *
 */
public class ScannerOperator implements Operator, TupleIterator {

  private final String dataParentPath;
  private Iterator<CSVRecord> recordIterator;
  private final String tableName;
  private Reader reader;
  private CSVParser csvParser;
  private int chunkSize = 1;

  /**
   *
   * @param table
   *          !null
   * @param dataParentPath
   *          !blank
   */
  public ScannerOperator(String tableName, String dataParentPath) {
    Validate.notBlank(tableName);

    if (null == SchemaManager.getTableSchema(tableName)) {
      throw new IllegalArgumentException(
          "Table with name: " + tableName + "does not exist!");
    }

    this.dataParentPath = dataParentPath;
    this.tableName = tableName;
  }

  @Override
  public void open() throws IOException {
    reader = Files.newBufferedReader(
        Paths.get(this.dataParentPath + this.tableName + ".csv"));
    csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    final Iterable<CSVRecord> csvRecords = csvParser.getRecords();
    recordIterator = csvRecords.iterator();
  }

  /**
   *
   * @return
   * @throws IOException
   */
  @Override
  public Collection<Tuple> getNext() throws IOException {

    final List<Tuple> tuples = new ArrayList<>();
    tuples.add(process());
    return tuples;
  }

  /**
   * process the request.
   *
   * @return
   * @throws IOException
   */
  private Tuple process() throws IOException {

    // if method invoked first time without connection being opened
    if (null == reader) {
      open();
    }

    // if no records left to iterate
    if (!recordIterator.hasNext()) {
      close();
      return new Tuple(new ArrayList<>());
    }

    final List<ColumnCell> columnCells = new ArrayList<>();
    final TableSchema tableSchema = SchemaManager.getTableSchema(tableName);
    final Integer tableId = SchemaManager.getTableId(tableName);
    l1 : for (int i = 0; i < chunkSize; i++) {

      // no value to iterate
      if (!recordIterator.hasNext()) {
        close();
        break;
      }

      // fetch a row
      final String[] values = recordIterator.next().get(0).split("\\|");

      l2 : for (int j = 0; j < values.length; j++) {
        final ColumnDefinition colDefinition = tableSchema
            .getColumnDefinitions().get(j);

        // create a ColumnCell, convert value to Primitive Value
        final ColumnCell colCell = new ColumnCell(
            PrimitiveTypeConverter.getPrimitiveValueByColDataType(
                colDefinition.getColDataType(), values[j]));
        colCell.setTableId(tableId);
        colCell.setColumnId(SchemaManager.getColumnIdByTableId(tableId,
            colDefinition.getColumnName()));
        columnCells.add(colCell);
      } // for
    } // for

    return new Tuple(columnCells);
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

    try {
      csvParser.close();
      reader.close();
    } catch (final IOException e) {

      // re-attempt
      csvParser.close();
      reader.close();
    }

  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples)
      throws IOException {
    return getNext();
  }

  @Override
  public boolean hasNext() throws IOException {
    // if method invoked first time without connection being opened
    if (null == reader) {
      open();
    }
    return this.recordIterator.hasNext();
  }

}
