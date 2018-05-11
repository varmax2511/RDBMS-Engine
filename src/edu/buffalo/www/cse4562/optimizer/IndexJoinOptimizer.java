/**
 * 
 */
package edu.buffalo.www.cse4562.optimizer;

import java.util.List;

import edu.buffalo.www.cse4562.model.Node;
import edu.buffalo.www.cse4562.model.ScannerContainer;
import edu.buffalo.www.cse4562.model.SchemaManager;
import edu.buffalo.www.cse4562.operator.IndexNestedJoinOperator;
import edu.buffalo.www.cse4562.operator.JoinOperator;
import edu.buffalo.www.cse4562.util.CollectionUtils;
import edu.buffalo.www.cse4562.util.ExpressionDecoder;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;

/**
 * @author Sneha Mehta
 *
 */
public class IndexJoinOptimizer {
  static Integer rightTableId;
  static Integer leftTableId;
  static Integer leftColumnId;
  static String tableName;
  static Integer rightColumnId;
  static Integer indexNo;
  public static void generateIndexJoin(Node root) {
     rightTableId=null;
     rightColumnId=null;
     leftTableId=null;
     leftColumnId=null;
     indexNo=null;
    if(root instanceof JoinOperator) {
      Expression expression = ((JoinOperator) root).getExpression();
      if(indexedColumnsExpression(root,expression) && !containsComplexChildren(root.getChildren().get(1))) {
        //make it INLJ
        indexNo = SchemaManager.getIndexValueForColumnId(rightTableId, rightColumnId);
        ScannerContainer scannerContainer = new ScannerContainer(IndexNestedJoinOperator.class, ScannerContainer.class, tableName, rightColumnId, indexNo);
        IndexNestedJoinOperator indexJoin = new IndexNestedJoinOperator(expression, scannerContainer, leftTableId, leftColumnId);
        indexJoin.setParent(root.getParent());
        indexJoin.setChildren(root.getChildren());
        for(Node child: indexJoin.getChildren()) {
          child.setParent(indexJoin);
        }
        indexJoin.setSchema(root.getBuiltSchema());
        int count=0;
        List<Node> parentsChildren = root.getParent().getChildren();
        for(Node node : parentsChildren) {
          if(node.equals(root)) {
            parentsChildren.set(count, indexJoin);
            break;
          }
          count++;
        }
      }
    }
    for(Node node :root.getChildren()) {
      generateIndexJoin(node);
    }
  }

  private static boolean containsComplexChildren(Node root) {
    if(CollectionUtils.isEmpty(root.getChildren())) 
      return false;
    if(root.getChildren().size() == 2) {
      return true;
    }
      return containsComplexChildren(root.getChildren().get(0));
  }

  private static boolean indexedColumnsExpression(Node joinNode,Expression expression) {
    ExpressionDecoder decoder = new ExpressionDecoder(expression);
      leftTableId = joinNode.getChildren().get(0).getBuiltSchema().get(0).getKey();
     rightTableId = joinNode.getChildren().get(1).getBuiltSchema().get(0).getKey();
     tableName = SchemaManager.getTableName(rightTableId);
     boolean result = false;
    for(Column column: decoder.getDecodedColumns()) {
      Validate.notNull(column.getTable());
      if(SchemaManager.getTableId(column.getTable().getName()) == rightTableId) {
        rightColumnId = SchemaManager.getColumnIdByTableId(rightTableId, column.getColumnName());
        result= SchemaManager.isColumnIndexed(rightTableId, rightColumnId);
      }
      else if(SchemaManager.getTableId(column.getTable().getName()) == leftTableId) {
        leftColumnId = SchemaManager.getColumnIdByTableId(leftTableId, column.getColumnName());
    }
    }
    return result;
  }

}
