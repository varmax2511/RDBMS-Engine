package edu.buffalo.www.cse4562.query;

import java.io.IOException;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.TableSchema;
import edu.buffalo.www.cse4562.preprocessor.Preprocessor;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Visit the {@link Statement} and automatically it will find the corresponding
 * query type.
 * 
 * @author varunjai
 *
 */
public class QueryVisitor implements StatementVisitor, SqlVisitor {

  private Node root;

  @Override
  public void visit(Select select) {
    SelectQueryVisitor selectQueryVisitor = new SelectQueryVisitor();
    select.getSelectBody().accept(selectQueryVisitor);
    root = selectQueryVisitor.getRoot();
  }

  @Override
  public void visit(Delete arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Update arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Insert arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Replace arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Drop arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(Truncate arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visit(CreateTable createStatement) {

    final String tableName = createStatement.getTable().getName();
    // add to Schema Manager
    TableSchema tableSchema = new TableSchema(tableName, createStatement.getColumnDefinitions());
    SchemaManager.addTableSchema(tableName,tableSchema);
    try {
      Preprocessor.preprocess(tableSchema,createStatement.getIndexes());
      
    } catch (IOException e) {
      System.err.println("IO Exception while preprocessing!");
    }
  }

  @Override
  public Node getRoot() {
    return root;
  }

}
