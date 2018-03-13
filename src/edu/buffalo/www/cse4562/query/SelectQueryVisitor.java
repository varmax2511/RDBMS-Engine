package edu.buffalo.www.cse4562.query;

import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;
/**
 * Visit and process a select query
 *
 * @author varunjai
 *
 */
public class SelectQueryVisitor
    implements
      SqlVisitor,
      SelectVisitor,
      SelectItemVisitor {

  private Node root;
  private ProjectionOperator projectionOpr;
  private Node currentNode;
  @Override
  public void visit(PlainSelect plainSelect) {

    // null check
    if (null == plainSelect) {
      return;
    }

    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    final FromItem fromItem = plainSelect.getFromItem();
    final Expression where = plainSelect.getWhere();
    final List<Join> joins = plainSelect.getJoins();

    // project
    /*
     * TODO: This needs to be worked out for later use of union, aggregate
     * operations
     */
    processProject(selectItems);

    // process WHERE
    processWhere(where);

    // if no Joins, simple FROM
    if (joins == null || CollectionUtils.isEmpty(joins)) {
      procesFrom(fromItem);
      return;
    } // if

    // handle Join items
    // TODO: This is a crude implementation and needs serious rework
    /*
     * Add a cross product
     */
    processCross(fromItem, joins);

  }

  /**
   * 
   * @param fromItem
   * @param joins
   */
  private void processCross(final FromItem fromItem, final List<Join> joins) {
    final CrossProductOperator crossProductOperator = new CrossProductOperator();
    final Node crossNode = new Node(crossProductOperator,
        CrossProductOperator.class);
    currentNode.addChild(crossNode);
    currentNode = crossNode;

    procesFrom(fromItem);

    // process each Join From Item
    final Iterator<Join> joinItr = joins.iterator();
    while (joinItr.hasNext()) {
      final FromItem joinFromItem = joinItr.next().getRightItem();
      procesFrom(joinFromItem);
    } // while
  }

  /**
   * 
   * @param fromItem
   */
  private void procesFrom(final FromItem fromItem) {
    // process FROM
    final QueryFromItemVisitor fromItemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromItemVisitor);
    if (fromItemVisitor.getRoot() != null) {
      final Node node = fromItemVisitor.getRoot();
      currentNode.addChild(node);
    } // if
  }

  /**
   * 
   * @param where
   */
  private void processWhere(final Expression where) {
    if (null != where) {
      final QueryExpressionVisitor whereVisitor = new QueryExpressionVisitor();
      where.accept(whereVisitor);
      if (null != whereVisitor.getRoot()) {
        final Node node = whereVisitor.getRoot();
        currentNode.addChild(node);
        currentNode = node;
      } // if
    }
  }

  /**
   * 
   * @param selectItems
   */
  private void processProject(final List<SelectItem> selectItems) {
    projectionOpr = new ProjectionOperator();
    final Iterator<SelectItem> selectItemItr = selectItems.iterator();
    while (selectItemItr.hasNext()) {
      selectItemItr.next().accept(this);
    }

    root = new Node(projectionOpr, ProjectionOperator.class);
    currentNode = root;
  }

  @Override
  public void visit(Union union) {
    if (null == union) {
      return;
    }
  }

  @Override
  public void visit(AllColumns arg0) {
    if (null == arg0) {
      return;
    }

    // enable processing for *
    projectionOpr.setAllColFlag(true);
  }

  @Override
  public void visit(AllTableColumns allTableColumns) {
    if (null == allTableColumns) {
      return;
    }

    projectionOpr.addAllTableColumns(allTableColumns);
  }

  @Override
  public void visit(SelectExpressionItem expression) {
    if (null == expression) {
      return;
    }

    projectionOpr.addSelectExpressionItems(expression);
  }

  @Override
  public Node getRoot() {
    return root;
  }

}
