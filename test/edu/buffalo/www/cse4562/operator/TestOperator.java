package edu.buffalo.www.cse4562.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collection;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import net.sf.jsqlparser.parser.CCJSqlParser;

/**
 * 
 * @author varunjai
 *
 */
public class TestOperator {

  @Test
  public void testOrderBy() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM R ORDER BY A DESC;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(10, tuples.size());

    for (final Tuple tuple : tuples) {
      System.out.println(tuple.toString());
    }
  }

  @Test
  public void testLimit() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM R ORDER BY A DESC LIMIT 2;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(2, tuples.size());
  }

  @Test
  public void testCross() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM R,S WHERE R.A = S.D AND R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(17, tuples.size());

    for (final Tuple tuple : tuples) {
      System.out.println(tuple.toString());
    }

  }

  @Test
  public void testCross2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM R,S WHERE R.A = S.D;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(26, tuples.size());

    for (final Tuple tuple : tuples) {
      System.out.println(tuple.toString());
    }

  }

  @Test
  public void testCross3() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT R.*, S.E, S.F FROM R,S WHERE R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(50, tuples.size());

    for (final Tuple tuple : tuples) {
      System.out.println(tuple.toString());
    }
  }

  @Test
  public void testCross4() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT R.*, S.* FROM R,S WHERE R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(50, tuples.size());

    for (final Tuple tuple : tuples) {
      System.out.println(tuple.toString());
    }
  }

}
