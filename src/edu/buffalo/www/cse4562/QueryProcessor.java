package edu.buffalo.www.cse4562;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.model.Tuple;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.Select;

/**
 * Process each type of query.
 * 
 * @author varunjai
 *
 */
public class QueryProcessor {

  /**
   * This method is responsible for processing {@link CreateTable} query by
   * storing the {@link TableSchema} in the {@link SchemaManager}.
   * 
   * @param createStatement
   *          !null.
   */
  public static void processCreateQuery(CreateTable createStatement) {
    // null check
    if (createStatement == null || createStatement.getTable() == null) {
      throw new IllegalArgumentException("Invalid Create Query");
    }

    final String tableName = createStatement.getTable().getName();
    // add to Schema Manager
    SchemaManager.addTableSchema(tableName,
        new TableSchema(tableName, createStatement.getColumnDefinitions()));
  }

  public static Collection<Tuple> processSelectQuery(Select statement)
      throws Throwable {
    final Node root = new TreeGenerator().evaluateSelect(statement);
    Collection<Tuple> tuples = root.getNext();
    Collection<Tuple> output = new ArrayList<>();
    while (!tuples.isEmpty()) {
      output.addAll(tuples);
      tuples = root.getNext();
    }

    return output;
  }

}
