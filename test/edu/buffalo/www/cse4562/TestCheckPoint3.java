/**
 * 
 */
/*package edu.buffalo.www.cse4562;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Collection;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import edu.buffalo.www.cse4562.util.TreeProcessor;
import edu.buffalo.www.cse4562.util.TuplePrinter;
import net.sf.jsqlparser.parser.CCJSqlParser;

/**
 * @author Sneha Mehta
 *
 */
/*public class TestCheckPoint3 {
  
  @Test
  public void testQ1() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER (C_CUSTKEY   INTEGER, C_NAME  STRING, C_ADDRESS   STRING, C_NATIONKEY   INTEGER NOT NULL, C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE NATION (N_NATIONKEY   INTEGER, N_NAME  STRING,  N_REGIONKEY   INTEGER NOT NULL, N_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);
    
    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER, O_CUSTKEY   INTEGER NOT NULL, O_ORDERSTATUS STRING, O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK STRING, O_SHIPPRIORITY  INTEGER, O_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY INTEGER NOT NULL, L_PARTKEY INTEGER NOT NULL, L_SUPPKEY INTEGER NOT NULL, L_LINENUMBER INTEGER,"
            + "L_QUANTITY DOUBLE, L_EXTENDEDPRICE DOUBLE,"
            + "L_DISCOUNT DOUBLE, L_TAX DOUBLE,"
            + "L_RETURNFLAG STRING, L_LINESTATUS STRING,"
            + "L_SHIPDATE DATE, L_COMMITDATE DATE,"
            + "L_RECEIPTDATE DATE, L_SHIPINSTRUCT STRING,"
            + "L_SHIPMODE STRING, L_COMMENT STRING" + ");"));
    parser.Statement().accept(queryVisitor);

    
    parser = new CCJSqlParser(new StringReader("SELECT CUSTOMER.C_CUSTKEY, SUM(LINEITEM.L_EXTENDEDPRICE * (1 + LINEITEM.L_DISCOUNT)) AS REVENUE, CUSTOMER.C_ACCTBAL FROM CUSTOMER, ORDERS, LINEITEM, NATION WHERE CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY AND (LINEITEM.L_DISCOUNT < .06 OR LINEITEM.L_QUANTITY < 24) AND LINEITEM.L_ORDERKEY = ORDERS.O_ORDERKEY AND LINEITEM.L_RETURNFLAG = 'R' AND CUSTOMER.C_NATIONKEY = NATION.N_NATIONKEY GROUP BY CUSTOMER.C_CUSTKEY, CUSTOMER.C_ACCTBAL ORDER BY REVENUE ASC LIMIT 20;"));
    parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(20, tuples.size());

    for(Tuple tuple :tuples){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
}
  
  @Test
  public void testQ2() throws Throwable {

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

    parser = new CCJSqlParser(new StringReader("SELECT SUM(L_EXTENDEDPRICE*L_DISCOUNT) AS REVENUE FROM LINEITEM WHERE L_DISCOUNT > 0.04 AND L_DISCOUNT < 0.08;"));
    parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(1, tuples.size());

    for(Tuple tuple :tuples){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
}
  
  @Test
  public void testQ3() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER (C_CUSTKEY   INTEGER, C_NAME  STRING, C_ADDRESS   STRING, C_NATIONKEY   INTEGER NOT NULL, C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER, O_CUSTKEY   INTEGER NOT NULL, O_ORDERSTATUS STRING, O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK STRING, O_SHIPPRIORITY  INTEGER, O_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY INTEGER NOT NULL, L_PARTKEY INTEGER NOT NULL, L_SUPPKEY INTEGER NOT NULL, L_LINENUMBER INTEGER,"
            + "L_QUANTITY DOUBLE, L_EXTENDEDPRICE DOUBLE,"
            + "L_DISCOUNT DOUBLE, L_TAX DOUBLE,"
            + "L_RETURNFLAG STRING, L_LINESTATUS STRING,"
            + "L_SHIPDATE DATE, L_COMMITDATE DATE,"
            + "L_RECEIPTDATE DATE, L_SHIPINSTRUCT STRING,"
            + "L_SHIPMODE STRING, L_COMMENT STRING" + ");"));
    parser.Statement().accept(queryVisitor);

    
    parser = new CCJSqlParser(new StringReader("SELECT LINEITEM.L_ORDERKEY, SUM(LINEITEM.L_EXTENDEDPRICE*(1-LINEITEM.L_DISCOUNT)) AS REVENUE, ORDERS.O_ORDERDATE, ORDERS.O_SHIPPRIORITY FROM CUSTOMER, ORDERS, LINEITEM WHERE CUSTOMER.C_MKTSEGMENT = 'BUILDING' AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY AND LINEITEM.L_ORDERKEY = ORDERS.O_ORDERKEY AND (LINEITEM.L_DISCOUNT < .06 OR LINEITEM.L_QUANTITY < 24) GROUP BY ORDERS.O_ORDERDATE, ORDERS.O_SHIPPRIORITY, LINEITEM.L_ORDERKEY ORDER BY REVENUE DESC, ORDERS.O_ORDERDATE;"));
    parser.Statement().accept(queryVisitor);
    
 // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(104, tuples.size());

    for(Tuple tuple :tuples){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }

}
  
  @Test
  public void testQ4() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER, O_CUSTKEY   INTEGER NOT NULL, O_ORDERSTATUS STRING, O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK STRING, O_SHIPPRIORITY  INTEGER, O_COMMENT STRING);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY INTEGER NOT NULL, L_PARTKEY INTEGER NOT NULL, L_SUPPKEY INTEGER NOT NULL, L_LINENUMBER INTEGER,"
            + "L_QUANTITY DOUBLE, L_EXTENDEDPRICE DOUBLE,"
            + "L_DISCOUNT DOUBLE, L_TAX DOUBLE,"
            + "L_RETURNFLAG STRING, L_LINESTATUS STRING,"
            + "L_SHIPDATE DATE, L_COMMITDATE DATE,"
            + "L_RECEIPTDATE DATE, L_SHIPINSTRUCT STRING,"
            + "L_SHIPMODE STRING, L_COMMENT STRING" + ");"));
    parser.Statement().accept(queryVisitor);

    
    parser = new CCJSqlParser(new StringReader("SELECT L_LINESTATUS, O_ORDERKEY, SUM(L_QUANTITY) AS SUM_QTY, SUM(L_EXTENDEDPRICE) AS SUM_BASE_PRICE, SUM(L_EXTENDEDPRICE*(1-L_DISCOUNT)) AS SUM_DISC_PRICE, SUM(L_EXTENDEDPRICE*(1-L_DISCOUNT)*(1+L_TAX)) AS SUM_CHARGE, AVG(L_QUANTITY) AS AVG_QTY, AVG(L_EXTENDEDPRICE) AS AVG_PRICE, AVG(L_DISCOUNT) AS AVG_DISC FROM LINEITEM,ORDERS GROUP BY L_LINESTATUS, O_ORDERKEY ORDER BY L_LINESTATUS;"));
    parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();
    assertEquals(15048, TreeProcessor.processTree(root).size());
}
 
  @Test
  public void testQ5() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PLAYERS (ID STRING, FIRSTNAME STRING, LASTNAME STRING, FIRSTSEASON INTEGER, LASTSEASON INTEGER, WEIGHT INTEGER, BIRTHDATE DATE);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader("SELECT SUM(SUB_Q.P1_WEIGHT*1.0) FROM (SELECT P1.FIRSTNAME AS P1_FIRSTNAME, P1.LASTNAME AS P1_LASTNAME, P2.FIRSTNAME AS P2_FIRSTNAME, P2.LASTNAME AS P2_LASTNAME, P1.FIRSTSEASON AS P1_FIRSTSEASON, P1.LASTSEASON AS P1_LASTSEASON, P2.FIRSTSEASON AS P2_FIRSTSEASON, P2.LASTSEASON AS P2_LASTSEASON, P1.WEIGHT AS P1_WEIGHT FROM PLAYERS P1, PLAYERS P2 WHERE P1.ID = P2.ID AND P1.FIRSTSEASON < 2007) SUB_Q;"));
    parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(1, tuples.size());

    for(Tuple tuple :tuples){
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

}*/
