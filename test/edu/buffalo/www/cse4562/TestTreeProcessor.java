package edu.buffalo.www.cse4562;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;

/**
 * Test cases for {@link TreeProcessor}.
 * 
 * @author varunjai
 *
 */
public class TestTreeProcessor {

  @Test
  public void testCreateQuery() throws ParseException {
    final CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

  }

  /**
   * Test simple select query w/o where clause.
   * 
   * @throws Throwable
   */
  @Test
  public void testSimpleSelect() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader("SELECT A + B FROM R;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(10, TreeProcessor.processTree(root).size());

  }

  /**
   * Test simple select query w/o where clause.
   * 
   * @throws Throwable
   */
  @Test
  public void testSimpleSelectWhere() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT A,B FROM R WHERE A > 4 AND B > 1;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(1, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSimpleTableAlias() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader(
        "SELECT P.A, P.B FROM R P WHERE P.A > 4 AND P.B > 1;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(1, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testRenamingAlias() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT A+B AS C FROM R WHERE A=4;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(4, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSubQueryWithAlias() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader(
        "SELECT P.A, P.B FROM (SELECT A, B FROM R WHERE B>1) P WHERE P.A=4;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(3, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSubQueryWithAlias2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT Q.D FROM (SELECT B AS D FROM R) Q;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(10, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSubQueryWithAlias3() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT Q.D FROM (SELECT A+B AS D FROM R) Q;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(10, TreeProcessor.processTree(root).size());

  }
  
  @Test
  public void testSimpleJoin() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    
    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D int, E int, F DATE);"));
    parser.Statement().accept(queryVisitor);
    
    assertTrue(SchemaManager.getTableSchema("S") != null);
    
    // QUERY 1
    parser = new CCJSqlParser(
        new StringReader("SELECT r.A, s.D FROM R r,S s WHERE r.A=s.D;"));
    parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();

    assertEquals(10, TreeProcessor.processTree(root).size());

    // QUERY 2
    parser = new CCJSqlParser(
        new StringReader("SELECT R.A, S.D FROM R,S WHERE R.A=S.D;"));
    parser.Statement().accept(queryVisitor);

    
    // get the tree
    root = queryVisitor.getRoot();

    assertEquals(10, TreeProcessor.processTree(root).size());

  }

}
