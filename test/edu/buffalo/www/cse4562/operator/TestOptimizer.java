/*package edu.buffalo.www.cse4562.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collection;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.optimizer.Optimizer;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import net.sf.jsqlparser.parser.CCJSqlParser;

public class TestOptimizer {

  @Test
  public void testOptimizer1() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM R,S WHERE R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    //final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(queryVisitor.getRoot());
    assertEquals(50, tuples.size());

  }

  @Test
  public void testOptimizer2() throws Throwable {
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
    final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(26, tuples.size());

  }

  @Test
  public void testOptimizer3() throws Throwable {
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
    //final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(queryVisitor.getRoot());
    assertEquals(17, tuples.size());

  }

  @Test
  public void testOptimizer4() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader("SELECT * FROM R WHERE A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(5, tuples.size());

  }
  
  @Test
  public void testProjectionPushDown1() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader("SELECT A,B FROM R WHERE A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    //final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(queryVisitor.getRoot());
    assertEquals(5, tuples.size());

  }
  
  @Test
  public void testProjectionPushDown2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader("SELECT B FROM R WHERE A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(5, tuples.size());

  }
  
  
  @Test
  public void testProjectionPushDown3() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT R.*,S.A FROM R,S WHERE R.A = S.D AND R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    //final Node root = Optimizer.optimizeTree(queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(queryVisitor.getRoot());
    assertEquals(17, tuples.size());

  }
}*/
