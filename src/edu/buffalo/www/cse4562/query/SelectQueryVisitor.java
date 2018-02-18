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

public class SelectQueryVisitor
    implements
      SqlVisitor,
      SelectVisitor,
      SelectItemVisitor {

  private Node root;
  private ProjectionOperator projectionOpr;

  @Override
  public void visit(PlainSelect plainSelect) {

    if (null == plainSelect) {
      return;
    }

    final List<SelectItem> selectItems = plainSelect.getSelectItems();
    FromItem fromItem = plainSelect.getFromItem();
    Expression where = plainSelect.getWhere();

    // project
    /*
     * This needs to be worked out for later use of union, aggregate operations
     */

    projectionOpr = new ProjectionOperator(selectItems);
    Iterator<SelectItem> selectItemItr = selectItems.iterator();
    while (selectItemItr.hasNext()) {
      selectItemItr.next().accept(this);
    }
   
    root = new Node(projectionOpr, ProjectionOperator.class);
    
    
    // process WHERE 
    /*QueryExpressionVisitor whereVisitor = new QueryExpressionVisitor();
    where.accept(whereVisitor);
    */
    
    
    // process FROM
    QueryFromItemVisitor fromIemVisitor = new QueryFromItemVisitor();
    fromItem.accept(fromIemVisitor);
    if(fromIemVisitor.getRoot() != null){
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
  public void visit(AllTableColumns arg0) {
    if (null == arg0) {
      return;
    }
  }

  @Override
  public void visit(SelectExpressionItem arg0) {
    
  }

  @Override
  public Node getRoot() {
    return root;
  }

}
