package edu.buffalo.www.cse4562.query;

import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.LimitOperator;
import edu.buffalo.www.cse4562.operator.OrderByOperator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
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
  private ProjectionOperator node;
  private Node currentNode;
  @Override
  public void visit(PlainSelect plainSelect) {

    // null check
    if (null == plainSelect) {
      return;
    }

    final Limit limit = plainSelect.getLimit();
    final List<OrderByElement> orderByElements = plainSelect
        .getOrderByElements();
    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    final FromItem fromItem = plainSelect.getFromItem();
    final Expression where = plainSelect.getWhere();
    final List<Join> joins = plainSelect.getJoins();

    // limit
    if (limit != null) {
      processLimit(limit);
    }

    // order by
    if (!CollectionUtils.isEmpty(orderByElements)) {
      processOrderBy(orderByElements);
    }
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
      procesFrom(fromItem, currentNode);
      return;
    } // if

    // handle Join items
    // TODO: This is a crude implementation and needs serious rework
    /*
     * Add a cross product
     */
    processCross(fromItem, joins);

  }

  private void processLimit(final Limit limit) {
    final Node node = new LimitOperator(limit.getRowCount());

    if (root == null) {
      root = node;
      currentNode = root;
      return;
    }

    currentNode.addChild(node);
    currentNode = node;
  }

  private void processOrderBy(final List<OrderByElement> orderByElements) {
    final Node node = new OrderByOperator(orderByElements);

    if (root == null) {
      root = node;
      currentNode = root;
      return;
    }

    currentNode.addChild(node);
    currentNode = node;

  }

  /**
   *
   * @param fromItem
   * @param joins
   */
  private void processCross2(final FromItem fromItem, final List<Join> joins) {
    final CrossProductOperator crossProductOperator = new CrossProductOperator();
    final Node node = crossProductOperator;
    currentNode.addChild(node);
    currentNode = node;

    procesFrom(fromItem, currentNode);

    // process each Join From Item
    final Iterator<Join> joinItr = joins.iterator();
    while (joinItr.hasNext()) {
      final FromItem joinFromItem = joinItr.next().getRightItem();
      procesFrom(joinFromItem, currentNode);
    } // while
  }

  /**
   *
   * @param fromItem
   * @param joins
   * @throws IllegalAccessException
   */
  private void processCross(final FromItem fromItem, final List<Join> joins) {

    final QueryFromItemVisitor fromItemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromItemVisitor);
    if (fromItemVisitor.getRoot() == null) {
      return;
    }

    Node leftNode = fromItemVisitor.getRoot();

    // process each Join From Item
    final Iterator<Join> joinItr = joins.iterator();
    while (joinItr.hasNext()) {
      final FromItem joinFromItem = joinItr.next().getRightItem();
      joinFromItem.accept(fromItemVisitor);
      if (fromItemVisitor.getRoot() == null) {
        continue;
      }
      
      final Node cNode = new CrossProductOperator();
      cNode.addChild(leftNode);
      cNode.addChild(fromItemVisitor.getRoot());
      leftNode = cNode;
    } // while

    currentNode.addChild(leftNode);
  }

  /**
   *
   * @param fromItem
   */
  private void procesFrom(final FromItem fromItem, Node current) {
    // process FROM
    final QueryFromItemVisitor fromItemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromItemVisitor);
    if (fromItemVisitor.getRoot() != null) {
      final Node node = fromItemVisitor.getRoot();
      current.addChild(node);
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
        Node node = whereVisitor.getRoot();
        currentNode.addChild(node);
        currentNode = node;

        while (!CollectionUtils.isEmpty(node.getChildren())) {
          node = node.getChildren().get(0);
          currentNode = node;
        }

      } // if
    }
  }

  /**
   *
   * @param selectItems
   */
  private void processProject(final List<SelectItem> selectItems) {
    node = new ProjectionOperator();
    final Iterator<SelectItem> selectItemItr = selectItems.iterator();
    while (selectItemItr.hasNext()) {
      selectItemItr.next().accept(this);
    }

    if (root == null) {
      root = node;
      currentNode = root;
      return;
    }
    currentNode.addChild(node);
    currentNode = node;

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
    node.setAllColFlag(true);
  }

  @Override
  public void visit(AllTableColumns allTableColumns) {
    if (null == allTableColumns) {
      return;
    }

    node.addAllTableColumns(allTableColumns);
  }

  @Override
  public void visit(SelectExpressionItem expression) {
    if (null == expression) {
      return;
    }

    node.addSelectExpressionItems(expression);
  }

  @Override
  public Node getRoot() {
    return root;
  }

}
