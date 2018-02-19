package edu.buffalo.www.cse4562.query;

import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
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

  @Override
  public void visit(PlainSelect plainSelect) {

    // null check
    if (null == plainSelect) {
      return;
    }

    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    final FromItem fromItem = plainSelect.getFromItem();
    final Expression where = plainSelect.getWhere();

    // project
    /*
     * TODO: This needs to be worked out for later use of union, aggregate
     * operations
     */
    projectionOpr = new ProjectionOperator();
    final Iterator<SelectItem> selectItemItr = selectItems.iterator();
    while (selectItemItr.hasNext()) {
      selectItemItr.next().accept(this);
    }

    root = new Node(projectionOpr, ProjectionOperator.class);

    // process WHERE
    /*
     * QueryExpressionVisitor whereVisitor = new QueryExpressionVisitor();
     * where.accept(whereVisitor);
     */

    // process FROM
    final QueryFromItemVisitor fromIemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromIemVisitor);
    if (fromIemVisitor.getRoot() != null) {
      root.addChild(fromIemVisitor.getRoot());
    }
  }

  @Override
  public void visit(Union union) {
    System.out.println("union");
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

    projectionOpr.addExpression(expression);
  }

  @Override
  public Node getRoot() {
    return root;
  }

}
