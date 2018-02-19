package edu.buffalo.www.cse4562.query;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ScannerOperator;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

public class QueryFromItemVisitor implements SqlVisitor, FromItemVisitor {

  private Node root;
  @Override
  public void visit(Table table) {
    // if table
    root = new Node(new ScannerOperator(table.getName(),
        ApplicationConstants.DATA_DIR_PATH), ScannerOperator.class);
    
  }

  @Override
  public void visit(SubSelect subSelect) {
    // if subselect
    SelectQueryVisitor selectQueryVisitor = new SelectQueryVisitor();
    subSelect.getSelectBody().accept(selectQueryVisitor);
    root = selectQueryVisitor.getRoot();
  }

  @Override
  public void visit(SubJoin arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public Node getRoot() {
    return root;
  }

}
