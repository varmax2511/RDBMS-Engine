package edu.buffalo.www.cse4562.query;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.operator.ScannerOperator;
import edu.buffalo.www.cse4562.operator.SubSelectOperator;
import edu.buffalo.www.cse4562.util.ApplicationConstants;
import edu.buffalo.www.cse4562.util.StringUtils;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;

public class QueryFromItemVisitor implements SqlVisitor, FromItemVisitor {

  private Node root;
  @Override
  public void visit(Table table) {
    // if table
    root = new ScannerOperator(new ScannerOperator.Config(table.getName(),
            table.getAlias(), ApplicationConstants.DATA_DIR_PATH));

  }

  @Override
  public void visit(SubSelect subSelect) {

    // if sub-select
    SelectQueryVisitor selectQueryVisitor = new SelectQueryVisitor();
    // we are only sending the subselect not the alias that may be associated
    // with it.
    subSelect.getSelectBody().accept(selectQueryVisitor);

    if (StringUtils.isBlank(subSelect.getAlias())) {
      root = selectQueryVisitor.getRoot();
      return;
    }

    // if an alias is present pass, it SubSelect operator
    root = new SubSelectOperator(subSelect.getAlias());
    root.addChild(selectQueryVisitor.getRoot());
  }

  @Override
  public void visit(SubJoin subJoin) {

  }

  @Override
  public Node getRoot() {
    return root;
  }

}
