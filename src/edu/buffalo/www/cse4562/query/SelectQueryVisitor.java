package edu.buffalo.www.cse4562.query;

import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.aggregator.AverageAggregate;
import edu.buffalo.www.cse4562.aggregator.CountAggregate;
import edu.buffalo.www.cse4562.aggregator.MaxAggregate;
import edu.buffalo.www.cse4562.aggregator.MinAggregate;
import edu.buffalo.www.cse4562.aggregator.SumAggregate;
import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.CrossProductOperator;
import edu.buffalo.www.cse4562.operator.GroupByOperator;
import edu.buffalo.www.cse4562.operator.LimitOperator;
import edu.buffalo.www.cse4562.operator.OrderByOperator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.RenamingOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
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
      SelectVisitor {

  private Node root;
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
    final List<Column> groupByColumnReferences = plainSelect
        .getGroupByColumnReferences();

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
    processRename(selectItems);
    processProject(selectItems);

    // Group by
    if (!CollectionUtils.isEmpty(groupByColumnReferences)) {
      processGroupBy(groupByColumnReferences);
    }

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
    // processCross(fromItem, joins);
    currentNode.addChild(processCross(fromItem, joins.iterator()));

  }

  private void processGroupBy(final List<Column> groupByColumnReferences) {
    final Node node = new GroupByOperator(groupByColumnReferences);

    if (root == null) {
      root = node;
      currentNode = root;
      return;
    }

    currentNode.addChild(node);
    currentNode = node;

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

  private Node processCross(final FromItem fromItem,
      final Iterator<Join> joinItr) {

    final QueryFromItemVisitor fromItemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromItemVisitor);

    final Node cNode = new CrossProductOperator();
    cNode.addChild(fromItemVisitor.getRoot());

    final FromItem fromItemNext = joinItr.next().getRightItem();

    // no more items
    if (!joinItr.hasNext()) {
      fromItemNext.accept(fromItemVisitor);
      cNode.addChild(fromItemVisitor.getRoot());
      return cNode;
    }

    cNode.addChild(processCross(fromItemNext, joinItr));
    return cNode;
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
 private void processRename(final List<SelectItem> selectItems) {

   final QuerySelectItemVisitor selectItemVisitor = new QuerySelectItemVisitor(
       selectItems, RenamingOperator.class);
  
   Node node = selectItemVisitor.getRoot();
   
   if(node == null){
     return;
   }
   
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
   * @param selectItems
   */
  private void processProject(final List<SelectItem> selectItems) {

    final QuerySelectItemVisitor selectItemVisitor = new QuerySelectItemVisitor(
        selectItems, ProjectionOperator.class);
    Node node = selectItemVisitor.getRoot(); 
    if (root == null) {
      root = node;
      currentNode = root;
      for(SelectExpressionItem expressionItem :((ProjectionOperator)node).getSelectExpressionItems()) {
        if(expressionItem.getExpression() instanceof Function) {
          processAggregate((Function)expressionItem.getExpression());
        }
      }
      return;
    }
    currentNode.addChild(node);
    currentNode = node;

    for(SelectExpressionItem expressionItem :((ProjectionOperator)node).getSelectExpressionItems()) {
      if(expressionItem.getExpression() instanceof Function) {
        processAggregate((Function)expressionItem.getExpression());
      }
    }

  }

  private void processAggregate(Function function) {
    Node node = null;
    switch (function.getName().toUpperCase()) {
      case "SUM" :
        node = new SumAggregate(function);
        break;
      case "COUNT" :
        node = new CountAggregate(function);
        break;
      case "AVG":
        node = new AverageAggregate(function);
        break;
      case "MIN":
        node = new MinAggregate(function);
        break;
      case "MAX":
        node = new MaxAggregate(function);
        break;
      default: System.err.println("This function is not handled: "+function.getName());
      
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
  public Node getRoot() {
    return root;
  }

}
