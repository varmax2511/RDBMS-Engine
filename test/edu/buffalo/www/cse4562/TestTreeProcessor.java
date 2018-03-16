package edu.buffalo.www.cse4562;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import edu.buffalo.www.cse4562.util.TuplePrinter;
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
    // build schema
    //

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
  public void testRenamingAlias4() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT A AS C FROM R WHERE A=4;"));
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
    // build schema

    assertEquals(4, TreeProcessor.processTree(root).size());

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
    // build schema

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
    // build schema

    assertEquals(26, TreeProcessor.processTree(root).size());

    // QUERY 2
    parser = new CCJSqlParser(
        new StringReader("SELECT R.A, S.D FROM R,S WHERE R.A=S.D;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    root = queryVisitor.getRoot();
    // build schema

    assertEquals(26, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSimpleJoin1() throws Throwable {
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
    parser = new CCJSqlParser(new StringReader("SELECT * FROM R r,S s;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();

    assertEquals(100, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSimpleDateEvalution() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D int, E int, F DATE);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("S") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT * FROM S WHERE F = '1995-04-01';"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(1, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testSimpleDateEvalution2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE S(D int, E int, F DATE);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("S") != null);

    parser = new CCJSqlParser(new StringReader(
        "SELECT D, E FROM S WHERE F >= '1994-01-01' AND F < '1994-01-03';"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(9, TreeProcessor.processTree(root).size());

  }
  @Test
  public void testSimpleDateEvalution3() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY INTEGER NOT NULL, L_PARTKEY INTEGER NOT NULL, L_SUPPKEY INTEGER NOT NULL, L_LINENUMBER INTEGER,"
            + "L_QUANTITY DOUBLE, L_EXTENDEDPRICE DOUBLE,"
            + "L_DISCOUNT DOUBLE, L_TAX DOUBLE,"
            + "L_RETURNFLAG STRING, L_LINESTATUS STRING,"
            + "L_SHIPDATE DATE, L_COMMITDATE DATE,"
            + "L_RECEIPTDATE DATE, L_SHIPINSTRUCT STRING,"
            + "L_SHIPMODE STRING, L_COMMENT STRING" + ");"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("LINEITEM") != null);

    parser = new CCJSqlParser(new StringReader("SELECT "
        + "L_EXTENDEDPRICE, L_DISCOUNT, L_QUANTITY FROM LINEITEM WHERE L_SHIPDATE >= '1995-01-01' AND L_SHIPDATE < '2004-01-03' AND L_DISCOUNT > 0.05 AND L_DISCOUNT < 0.37 AND L_QUANTITY > 49 ORDER BY L_EXTENDEDPRICE LIMIT 20;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();

    assertEquals(2, TreeProcessor.processTree(root).size());

  }

  @Test
  public void testQueryEvalution4() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER (C_CUSTKEY   INTEGER, C_NAME  STRING, C_ADDRESS   STRING, C_NATIONKEY   INTEGER NOT NULL, C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE NATION (N_NATIONKEY   INTEGER, N_NAME  STRING,  N_REGIONKEY   INTEGER NOT NULL, N_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE REGION (R_REGIONKEY INTEGER, R_NAME STRING, R_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER C, NATION N, REGION R WHERE N.N_NATIONKEY = C.C_NATIONKEY AND N.N_NATIONKEY < 3 AND C.C_MKTSEGMENT = 'FURNITURE' AND C.C_ACCTBAL > 9995;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    assertEquals(10, TreeProcessor.processTree(root).size());

    for(Tuple tuple :TreeProcessor.processTree(root)){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
    
    /*parser = new CCJSqlParser(new StringReader(
        "SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER C, NATION N, REGION R WHERE N.N_NATIONKEY = C.C_NATIONKEY AND N.N_NATIONKEY >= 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    root = queryVisitor.getRoot();

    assertEquals(25, TreeProcessor.processTree(root).size());*/

  }
  
  @Test
  public void testQueryEvalution6() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER1 (C_CUSTKEY   INTEGER, C_NAME  STRING, C_ADDRESS   STRING, C_NATIONKEY   INTEGER NOT NULL, C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE NATION1 (N_NATIONKEY   INTEGER, N_NAME  STRING,  N_REGIONKEY   INTEGER NOT NULL, N_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE REGION1 (R_REGIONKEY INTEGER, R_NAME STRING, R_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER1 C, NATION1 N, REGION1 R WHERE N.N_NATIONKEY = C.C_NATIONKEY AND N.N_NATIONKEY < 3 AND C.C_MKTSEGMENT = 'FURNITURE' AND C.C_ACCTBAL > 9995;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    assertEquals(82, TreeProcessor.processTree(root).size());

    for(Tuple tuple :TreeProcessor.processTree(root)){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
    
    /*parser = new CCJSqlParser(new StringReader(
        "SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER C, NATION N, REGION R WHERE N.N_NATIONKEY = C.C_NATIONKEY AND N.N_NATIONKEY >= 3;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    root = queryVisitor.getRoot();

    assertEquals(25, TreeProcessor.processTree(root).size());*/

  }

  @Test
  public void testQueryEvalution5() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PLAYERS (ID STRING, FIRSTNAME STRING, LASTNAME STRING, FIRSTSEASON INTEGER, LASTSEASON INTEGER, WEIGHT INTEGER, BIRTHDATE DATE);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT P1_FIRSTNAME, P1_LASTNAME, P2_FIRSTNAME, P2_LASTNAME FROM (SELECT P1.FIRSTNAME AS P1_FIRSTNAME, P1.LASTNAME AS P1_LASTNAME, P2.FIRSTNAME AS P2_FIRSTNAME, P2.LASTNAME AS P2_LASTNAME, P1.FIRSTSEASON AS P1_FIRSTSEASON, P1.LASTSEASON AS P1_LASTSEASON, P2.FIRSTSEASON AS P2_FIRSTSEASON, P2.LASTSEASON AS P2_LASTSEASON FROM PLAYERS P1, PLAYERS P2 WHERE P1.ID<>P2.ID) SUB_Q WHERE P1_FIRSTSEASON<P2_FIRSTSEASON AND P1_LASTSEASON>P2_LASTSEASON;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    assertEquals(100, TreeProcessor.processTree(root).size());

    for(Tuple tuple :TreeProcessor.processTree(root)){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

}
