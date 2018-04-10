package edu.buffalo.www.cse4562.query;

import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.Operator;
import edu.buffalo.www.cse4562.operator.ProjectionOperator;
import edu.buffalo.www.cse4562.operator.RenamingOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.StringUtils;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

public class QuerySelectItemVisitor implements SelectItemVisitor, SqlVisitor {

  private Node root;
  private final List<SelectItem> selectItems;
  private final Class<? extends Operator> clasz;
  private boolean processed = false;
  
  public QuerySelectItemVisitor(List<SelectItem> selectItems,
      Class<? extends Operator> clasz) {
    this.clasz = clasz;
       
    if (clasz == ProjectionOperator.class) {
      root = new ProjectionOperator();
    } else if (clasz == RenamingOperator.class) {
      root = new RenamingOperator();
    }
    this.selectItems = selectItems;
  }

  @Override
  public void visit(AllColumns arg0) {
    if (null == arg0 || this.clasz == RenamingOperator.class) {
      return;
    }

    // enable processing for *
    ((ProjectionOperator) root).setAllColFlag(true);

  }

  @Override
  public void visit(AllTableColumns allTableColumns) {
    if (null == allTableColumns || this.clasz == RenamingOperator.class) {
      return;
    }

    ((ProjectionOperator) root).addAllTableColumns(allTableColumns);
  }

  @Override
  public void visit(SelectExpressionItem expression) {
    if (null == expression) {
      return;
    }

    if (root instanceof ProjectionOperator) {
      ((ProjectionOperator) root).addSelectExpressionItems(expression);
    } else if (root instanceof RenamingOperator
        && !StringUtils.isBlank(expression.getAlias())) {
      ((RenamingOperator) root).addSelectExpressionItems(expression);
    }

  }

  @Override
  public Node getRoot() {
  
    if(processed){
      return root;
    }
    
    for (SelectItem selectItem : selectItems) {
      selectItem.accept(this);
    }
    
    // for rename if no match found, skip
    if(this.clasz == RenamingOperator.class){
      if(CollectionUtils.isEmpty(((RenamingOperator) root).getSelectExpressionItems())){
        root = null;
      }
    }
    
    processed = true;
    return root;
  }

}
