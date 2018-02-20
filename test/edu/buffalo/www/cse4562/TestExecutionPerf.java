package edu.buffalo.www.cse4562;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;

public class TestExecutionPerf {

  @Test
  public void testTableCreationPerf() throws ParseException {

    long startTime = System.currentTimeMillis();
    final CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);
    long endTime = System.currentTimeMillis();
    
    assertTrue(endTime - startTime < 50);
  }
  
  @Test
  public void testPlainSelectPerf() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);
    
    long startTime = System.currentTimeMillis();
    parser = new CCJSqlParser(new StringReader("SELECT A + B FROM R;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    TreeProcessor.processTree(root);
    long endTime = System.currentTimeMillis();

    assertTrue(endTime - startTime < 200);
  }
  
  @Test
  public void testPlainSelectLargePerf() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE X(A int, B int, C decimal, D varchar);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("X") != null);
    
    long startTime = System.currentTimeMillis();
    parser = new CCJSqlParser(new StringReader("SELECT A + B FROM X;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    TreeProcessor.processTree(root);
    long endTime = System.currentTimeMillis();
    assertTrue(endTime - startTime < 500);
  }
}
