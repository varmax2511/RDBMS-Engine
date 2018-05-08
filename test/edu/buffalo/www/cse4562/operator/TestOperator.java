/*package edu.buffalo.www.cse4562.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collection;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import net.sf.jsqlparser.parser.CCJSqlParser;

/**
 * 
 * @author varunjai
 *
 */
/*public class TestOperator {

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

  }

  @Test
  public void testMultiCross() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE T(G STRING, H INTEGER, I DATE, J DOUBLE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT R.A, S.E, T.I FROM R,S,T WHERE R.A=S.D AND R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(340, tuples.size());
  }

  @Test
  public void testCross5() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE F(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT R.*, F.E, F.F FROM F,R WHERE R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(25, tuples.size());

  }

  @Test
  public void testCross6() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE F(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT R.*, F.E, F.F FROM R,F WHERE R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(25, tuples.size());

  }

  @Test
  public void testCross7() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("CREATE TABLE F(D INTEGER, E INTEGER, F DATE);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT R.*, F.E, F.F FROM F, R WHERE F.D == R.A AND R.A > 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = (queryVisitor.getRoot());
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(9, tuples.size());

  }

  @Test
  public void testCount() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(new StringReader("SELECT COUNT(A) FROM R;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(1, tuples.size());

  }

  @Test
  public void testAggregateWithGroupBy() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT SUM(B) FROM R WHERE A=4 GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(1, tuples.size());
    System.out.println("count");
    for (Tuple tuple : tuples) {
      Collection<ColumnCell> cells = tuple.getColumnCells();
      for (ColumnCell cell : cells)
        assertEquals(90, cell.getCellValue().toLong());
    }
    
    parser = new CCJSqlParser(
        new StringReader("SELECT A,COUNT(*) FROM R GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root1 = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples1 = TreeProcessor.processTree(root1);
    assertEquals(5, tuples1.size());
    for (Tuple tuple : tuples1) {
      TuplePrinter.printTuple(tuple);
    }
    parser = new CCJSqlParser(
        new StringReader("SELECT A,AVG(B) FROM R GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root2 = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples2 = TreeProcessor.processTree(root2);
    assertEquals(5, tuples2.size());
    System.out.println("avg");

    for (Tuple tuple : tuples2) {
      TuplePrinter.printTuple(tuple);
    }
    parser = new CCJSqlParser(
        new StringReader("SELECT A,MIN(B) FROM R GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root3 = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples3 = TreeProcessor.processTree(root3);
    assertEquals(5, tuples3.size());
    System.out.println("min");

    for (Tuple tuple : tuples3) {
      TuplePrinter.printTuple(tuple);
    }
    parser = new CCJSqlParser(
        new StringReader("SELECT A,MAX(B) FROM R GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root4 = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples4 = TreeProcessor.processTree(root4);
    assertEquals(5, tuples4.size());
    System.out.println("max");

    for (Tuple tuple : tuples4) {
      TuplePrinter.printTuple(tuple);
    }
  }

  @Test
  public void testAggregateWithoutGroupBy() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT SUM(B) FROM R WHERE A=4;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(1, tuples.size());
    for (Tuple tuple : tuples) {
      Collection<ColumnCell> cells = tuple.getColumnCells();
      for (ColumnCell cell : cells)
        assertEquals(90, cell.getCellValue().toLong());
    }
  }

  @Test
  public void testGroupBy() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT A,B,SUM(B) FROM R GROUP BY A,B;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(10, tuples.size());
    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
    }
  }

  @Test
  public void testGroupBy2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT A,COUNT(*) FROM R GROUP BY A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    // build schema

    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(5, tuples.size());
    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
    }
  }

}
*/