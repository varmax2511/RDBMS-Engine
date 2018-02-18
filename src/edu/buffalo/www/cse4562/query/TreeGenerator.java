package edu.buffalo.www.cse4562.query;

import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.ScannerOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;
/**
 * This parser is responsible for evaluating a query and constructing a tree out
 * of it.
 *
 * @author varunjai
 *
 */
public class TreeGenerator {

  private final Config config;
  /**
   * 
   * @param config
   *          !null.
   */
  public TreeGenerator(Config config) {
    Validate.notNull(config);
    this.config = config;
  }

  /**
   * Evaluate the select Query.
   * 
   * @param selectQuery
   *          !null.
   * @return
   */
  public Node evaluateSelect(Select selectQuery) {

    final SelectBody selectBody = selectQuery.getSelectBody();
    Node root = null;
    if (selectBody instanceof PlainSelect) {
      return evaluatePlainSelect((PlainSelect) selectBody);

    } else if (selectBody instanceof Union) {
      // add union operations here
    }

    return root;
  }

  private Node evaluateUnion() {
    return null;
  }

  /**
   * This method processes a {@link PlainSelect}
   * 
   * @param plainSelect
   * @return
   */
  private Node evaluatePlainSelect(PlainSelect plainSelect) {

    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    final FromItem fromItem = plainSelect.getFromItem();
    final Expression where = plainSelect.getWhere();
    // project
    /*
     * This needs to be worked out for later use of aggregate operations
     */
    final Node root = new Node(new ProjectionOperator(selectItems),
        ProjectionOperator.class);
    Node currentNode = root;

    // WHERE clause
    if (null != where) {
      Node whereNode = evaluateWhere(where);
      currentNode.addChild(whereNode);
      currentNode = whereNode;
    }

    // nested query
    if (fromItem instanceof SubSelect) {
      Node subSelectNode = evaluateSubSelect((SubSelect) fromItem);
      currentNode.addChild(subSelectNode);
      currentNode = subSelectNode;
    } else {
      // Scanner
      Table table = (Table) fromItem;
      final Node node = new Node(
          new ScannerOperator(table.getName(), config.getDataDirPath()),
          ScannerOperator.class);
      currentNode.addChild(node);
      currentNode = node;
    }

    return root;
  }

  /**
   * 
   * @param where
   * @return
   */
  private Node evaluateWhere(Expression where) {

    final Node node = new Node(new SelectionOperator(where),
        SelectionOperator.class);

    return node;
  }

  private Node evaluateSubSelect(SubSelect subSelect) {
    return null;
  }

  /**
   * Configuration class for {@link TreeGenerator}.
   * 
   * @author varunjai
   *
   */
  public static class Config {
    private String dataDirPath = ApplicationConstants.DATA_DIR_PATH;;

    /**
     * 
     * @param dataDirPath
     */
    public Config(String dataDirPath) {
      this.dataDirPath = dataDirPath;
    }

    public String getDataDirPath() {
      return dataDirPath;
    }

    public void setDataDirPath(String dataDirPath) {
      this.dataDirPath = dataDirPath;
    }

  }
}
