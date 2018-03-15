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
public class TestOrderBy {

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
}
