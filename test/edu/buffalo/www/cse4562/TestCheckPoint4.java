/**
 * 
 */
/*package edu.buffalo.www.cse4562;

import static org.junit.Assert.assertEquals;

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
/*public class TestCheckPoint4 {

  @Test
  public void testPreComputation() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PART (P_PARTKEY   INTEGER PRIMARY KEY,P_NAME      STRING,P_MFGR      STRING,P_BRAND     STRING,P_TYPE      STRING,P_SIZE      INTEGER,P_CONTAINER   STRING,P_RETAILPRICE DOUBLE,P_COMMENT   STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE SUPPLIER (S_SUPPKEY   INTEGER PRIMARY KEY,  S_NAME      STRING,  S_ADDRESS   STRING,  S_NATIONKEY   INTEGER REFERENCES NATION,  S_PHONE     STRING,  S_ACCTBAL   DOUBLE,  S_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PARTSUPP (PS_PARTKEY    INTEGER REFERENCES PART,  PS_SUPPKEY    INTEGER REFERENCES SUPPLIER,  PS_AVAILQTY   INTEGER,  PS_SUPPLYCOST DOUBLE,  PS_COMMENT    STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER (C_CUSTKEY   INTEGER PRIMARY KEY,  C_NAME      STRING,  C_ADDRESS   STRING,  C_NATIONKEY   INTEGER REFERENCES NATION,  C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER PRIMARY KEY,  O_CUSTKEY   INTEGER REFERENCES CUSTOMER,  O_ORDERSTATUS STRING,  O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK     STRING,  O_SHIPPRIORITY  INTEGER,  O_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE NATION (  N_NATIONKEY   INTEGER PRIMARY KEY,  N_NAME      STRING,  N_REGIONKEY   INTEGER REFERENCES REGION,  N_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE REGION (  R_REGIONKEY INTEGER PRIMARY KEY,  R_NAME    STRING,  R_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

  }

  @Test
  public void testQ1() throws Throwable {

    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER PRIMARY KEY,  O_CUSTKEY   INTEGER REFERENCES CUSTOMER,  O_ORDERSTATUS STRING,  O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK     STRING,  O_SHIPPRIORITY  INTEGER,  O_COMMENT   STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT SUM(LINEITEM.L_EXTENDEDPRICE+ (1 + LINEITEM.L_DISCOUNT)) AS REVENUE FROM LINEITEM, ORDERS WHERE (LINEITEM.L_DISCOUNT < 0.07 OR LINEITEM.L_QUANTITY < 26) AND LINEITEM.L_ORDERKEY = ORDERS.O_ORDERKEY AND LINEITEM.L_RETURNFLAG = 'R';"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(20, tuples.size());

    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

  @Test
  public void testQ2() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PARTSUPP (PS_PARTKEY    INTEGER REFERENCES PART,  PS_SUPPKEY    INTEGER REFERENCES SUPPLIER,  PS_AVAILQTY   INTEGER,  PS_SUPPLYCOST DOUBLE,  PS_COMMENT    STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT L.L_SHIPDATE, L.L_COMMITDATE, L.L_RECEIPTDATE, PU.PS_SUPPLYCOST FROM LINEITEM L, PARTSUPP PU WHERE PU.PS_PARTKEY = L.L_PARTKEY AND PU.PS_SUPPKEY = L.L_SUPPKEY AND PU.PS_SUPPLYCOST < 3 AND PU.PS_SUPPKEY < 5000 AND L.L_ORDERKEY < 10000 LIMIT 100;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(100, tuples.size());

    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

  @Test
  public void testQ3() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE PART (P_PARTKEY   INTEGER PRIMARY KEY,P_NAME      STRING,P_MFGR      STRING,P_BRAND     STRING,P_TYPE      STRING,P_SIZE      INTEGER,P_CONTAINER   STRING,P_RETAILPRICE DOUBLE,P_COMMENT   STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT L_LINESTATUS, SUM(L_EXTENDEDPRICE+ (1 - L_DISCOUNT)) AS REVENUE FROM LINEITEM, PART WHERE L_QUANTITY >= 2 AND L_QUANTITY <= 6 AND P_SIZE > 1 AND P_SIZE < 6 AND L_SHIPINSTRUCT = 'DELIVER IN PERSON' AND P_PARTKEY = L_PARTKEY GROUP BY L_LINESTATUS ORDER BY L_LINESTATUS;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(100, tuples.size());

    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

  @Test
  public void testQ4() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE CUSTOMER (C_CUSTKEY   INTEGER PRIMARY KEY,  C_NAME      STRING,  C_ADDRESS   STRING,  C_NATIONKEY   INTEGER REFERENCES NATION,  C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER PRIMARY KEY,  O_CUSTKEY   INTEGER REFERENCES CUSTOMER,  O_ORDERSTATUS STRING,  O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK     STRING,  O_SHIPPRIORITY  INTEGER,  O_COMMENT   STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT LINEITEM.L_ORDERKEY, SUM(LINEITEM.L_EXTENDEDPRICE*(1-LINEITEM.L_DISCOUNT)) AS REVENUE, ORDERS.O_ORDERDATE, ORDERS.O_SHIPPRIORITY FROM CUSTOMER, ORDERS, LINEITEM WHERE CUSTOMER.C_MKTSEGMENT = 'BUILDING' AND CUSTOMER.C_CUSTKEY = ORDERS.O_CUSTKEY AND LINEITEM.L_ORDERKEY = ORDERS.O_ORDERKEY AND (LINEITEM.L_DISCOUNT < 0.08 OR LINEITEM.L_QUANTITY < 34) GROUP BY ORDERS.O_ORDERDATE, ORDERS.O_SHIPPRIORITY, LINEITEM.L_ORDERKEY ORDER BY REVENUE DESC, ORDERS.O_ORDERDATE LIMIT 100;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(100, tuples.size());

    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

  @Test
  public void testQ5() throws Throwable {
    CCJSqlParser parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE SUPPLIER (S_SUPPKEY   INTEGER PRIMARY KEY,  S_NAME      STRING,  S_ADDRESS   STRING,  S_NATIONKEY   INTEGER REFERENCES NATION,  S_PHONE     STRING,  S_ACCTBAL   DOUBLE,  S_COMMENT   STRING);"));
    final QueryVisitor queryVisitor = new QueryVisitor();

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE LINEITEM (L_ORDERKEY    INTEGER REFERENCES ORDERS,  L_PARTKEY   INTEGER REFERENCES PART,  L_SUPPKEY   INTEGER REFERENCES SUPPLIER,  L_LINENUMBER  INTEGER,  L_QUANTITY    DOUBLE,  L_EXTENDEDPRICE DOUBLE,  L_DISCOUNT    DOUBLE,  L_TAX     DOUBLE,  L_RETURNFLAG  STRING,  L_LINESTATUS  STRING,  L_SHIPDATE    DATE,  L_COMMITDATE  DATE,  L_RECEIPTDATE DATE,  L_SHIPINSTRUCT  STRING,  L_SHIPMODE    STRING,  L_COMMENT   STRING,  PRIMARY KEY (L_ORDERKEY, L_LINENUMBER));"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT S_SUPPKEY, L_RETURNFLAG, L_LINESTATUS, SUM(L_QUANTITY) AS SUM_QTY, SUM(L_EXTENDEDPRICE) AS SUM_BASE_PRICE, SUM(L_EXTENDEDPRICE*(1-L_DISCOUNT)) AS SUM_DISC_PRICE, SUM(L_EXTENDEDPRICE*(1-L_DISCOUNT)*(1+L_TAX)) AS SUM_CHARGE, AVG(L_QUANTITY) AS AVG_QTY, AVG(L_EXTENDEDPRICE) AS AVG_PRICE, AVG(L_DISCOUNT) AS AVG_DISC FROM LINEITEM, SUPPLIER WHERE L_SUPPKEY = S_SUPPKEY AND L_QUANTITY > 25 GROUP BY L_RETURNFLAG, L_LINESTATUS,S_SUPPKEY ORDER BY L_RETURNFLAG, L_LINESTATUS, S_SUPPKEY LIMIT 300;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    Node root = queryVisitor.getRoot();
    Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(300, tuples.size());

    for (Tuple tuple : tuples) {
      TuplePrinter.printTuple(tuple);
      System.out.println();
    }
  }

}*/
