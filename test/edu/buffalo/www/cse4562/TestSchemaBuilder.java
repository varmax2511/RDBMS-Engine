package edu.buffalo.www.cse4562;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.Pair;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.query.QueryVisitor;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;

/**
 * Test class for schema builder
 * 
 * @author varunjai
 *
 */
public class TestSchemaBuilder {

  @Test
  public void testBasicSchema() throws ParseException {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT G FROM (SELECT A+B AS G FROM R);"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    List<Pair<Integer, Integer>> builtSchema = root.getBuiltSchema();
    assertEquals(1, builtSchema.size());
  }

  @Test
  public void testBasicSchema2() throws ParseException {
    CCJSqlParser parser = new CCJSqlParser(
        new StringReader("CREATE TABLE R(A int, B int, C int);"));

    final QueryVisitor queryVisitor = new QueryVisitor();
    parser.Statement().accept(queryVisitor);

    assertTrue(SchemaManager.getTableSchema("R") != null);

    parser = new CCJSqlParser(
        new StringReader("SELECT G FROM (SELECT A+B AS G FROM R);"));
    parser.Statement().accept(queryVisitor);

    // get the tree
    final Node root = queryVisitor.getRoot();
    List<Pair<Integer, Integer>> builtSchema = root.getBuiltSchema();
    assertEquals(1, builtSchema.size());

  }

  @Test
  public void testSchema() throws ParseException {
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
    List<Pair<Integer, Integer>> builtSchema = root.getBuiltSchema();
    assertEquals(2, builtSchema.size());
    assertTrue(
        SchemaManager.getTableName(builtSchema.get(0).getKey()).equals("P")
            && SchemaManager.getColumnNameById(builtSchema.get(0).getKey(),
                builtSchema.get(0).getValue()).equals("A"));
    assertTrue(
        SchemaManager.getTableName(builtSchema.get(1).getKey()).equals("P")
            && SchemaManager.getColumnNameById(builtSchema.get(1).getKey(),
                builtSchema.get(1).getValue()).equals("B"));

  }

}
