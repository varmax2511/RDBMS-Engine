/*package edu.buffalo.www.cse4562;

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
import net.sf.jsqlparser.parser.ParseException;

public class TestCheckpt2 {

  @Test
  public void testQ4() throws Throwable {

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
        "CREATE TABLE LINEITEM (L_ORDERKEY INTEGER NOT NULL, L_PARTKEY INTEGER NOT NULL, L_SUPPKEY INTEGER NOT NULL, L_LINENUMBER INTEGER,"
            + "L_QUANTITY DOUBLE, L_EXTENDEDPRICE DOUBLE,"
            + "L_DISCOUNT DOUBLE, L_TAX DOUBLE,"
            + "L_RETURNFLAG STRING, L_LINESTATUS STRING,"
            + "L_SHIPDATE DATE, L_COMMITDATE DATE,"
            + "L_RECEIPTDATE DATE, L_SHIPINSTRUCT STRING,"
            + "L_SHIPMODE STRING, L_COMMENT STRING" + ");"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "CREATE TABLE ORDERS (O_ORDERKEY    INTEGER, O_CUSTKEY   INTEGER NOT NULL, O_ORDERSTATUS STRING, O_TOTALPRICE  DOUBLE,  O_ORDERDATE   DATE,  O_ORDERPRIORITY STRING,  O_CLERK STRING, O_SHIPPRIORITY  INTEGER, O_COMMENT STRING);"));
    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(new StringReader(
        "SELECT D.C_NAME, D.N_NAME FROM (SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER C, NATION N, REGION R WHERE R.R_REGIONKEY = N.N_REGIONKEY AND N.N_NATIONKEY = C.C_NATIONKEY AND R.R_NAME = 'AMERICA') D, ORDERS O, LINEITEM L WHERE O.O_ORDERKEY = L.L_ORDERKEY AND O.O_CUSTKEY = D.C_CUSTKEY AND L.L_SHIPDATE < '1992-01-05' ORDER BY D.C_NAME LIMIT 5;"));
    parser.Statement().accept(queryVisitor);
    //parser = new CCJSqlParser(new StringReader("SELECT C.*, N.N_NAME, R.R_NAME FROM CUSTOMER C, NATION N, REGION R WHERE R.R_REGIONKEY = N.N_REGIONKEY AND N.N_NATIONKEY = C.C_NATIONKEY AND R.R_NAME = 'AMERICA'"));
    //parser.Statement().accept(queryVisitor);
    
    // get the tree
    Node root = queryVisitor.getRoot();
    assertEquals(15048, TreeProcessor.processTree(root).size());
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
        new StringReader("CREATE TABLE CUSTOMER1 (C_CUSTKEY   INTEGER, C_NAME  STRING, C_ADDRESS   STRING, C_NATIONKEY   INTEGER NOT NULL, C_PHONE     STRING,  C_ACCTBAL   DOUBLE,  C_MKTSEGMENT  STRING,  C_COMMENT   STRING);"));

    parser.Statement().accept(queryVisitor);

    parser = new CCJSqlParser(
        new StringReader("SELECT F.A, F.D FROM (SELECT * FROM R,S WHERE R.A = S.D AND R.A > 3) F, CUSTOMER1 C where C.C_CUSTKEY=F.A;"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    final Collection<Tuple> tuples = TreeProcessor.processTree(root);
    assertEquals(17, tuples.size());

  }

}
*/