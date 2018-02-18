package edu.buffalo.www.cse4562;

import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.ScannerOperator;
import edu.buffalo.www.cse4562.operator.SelectionOperator;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import net.sf.jsqlparser.expression.Expression;
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
  public TreeGenerator(Config config) {
    this.config = config;
  }

  public Node evaluateSelect(Select selectQuery) {

    final SelectBody selectBody = selectQuery.getSelectBody();
    Node root = null;
    if (selectBody instanceof PlainSelect) {
      root = evaluatePlainSelect((PlainSelect) selectBody);

    } else if (selectBody instanceof Union) {
      // add union operations here
    }

    return root;
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

    // select
    if (where != null) {
      final Node node = new Node(new SelectionOperator(where),
          SelectionOperator.class);
      currentNode.addChild(node);
      currentNode = node;
    }

    // nested query
    if (fromItem instanceof SubSelect) {
      Node node = evaluateSelect((Select) fromItem);
      currentNode.addChild(node);
      currentNode = node;;
    } else {
      // Scanner
      final Node node = new Node(
          new ScannerOperator((Table) fromItem, config.getDataDirPath()),
          ScannerOperator.class);
      currentNode.addChild(node);
      currentNode = node;
    }

    return root;
  }

  private Node evaluateUnion() {
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