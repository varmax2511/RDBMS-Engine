package edu.buffalo.www.cse4562.model;

import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.operator.Operator;
import edu.buffalo.www.cse4562.util.Validate;

/**
 * Binary Tree Node where each Node represents the {@link Operator} it holds,
 * the class of the operator and its children.
 *
 */
public class Node {

  private Class<? extends Operator> operatorType;
  private Operator operator;
  private Node left;
  private Node right;
  private boolean isLeaf = true;

  /**
   * 
   * @param operator
   *          !null
   * @param operatorType
   *          !null
   * @throws IllegalArgumentException
   */
  public Node(Operator operator, Class<? extends Operator> operatorType) {
    // null check
    Validate.notNull(operator);
    Validate.notNull(operatorType);

    this.operator = operator;
    this.operatorType = operatorType;

  }

  public Class<? extends Operator> getOperatorType() {
    return operatorType;
  }
  public void setOperatorType(Class<? extends Operator> operatorType) {
    this.operatorType = operatorType;
  }
  public Operator getOperator() {
    return operator;
  }
  public void setOperator(Operator operator) {
    this.operator = operator;
  }
  public Node getLeft() {
    return left;
  }
  public void setLeft(Node left) {
    this.isLeaf = false;
    this.left = left;
  }
  public Node getRight() {
    return right;
  }
  public void setRight(Node right) {
    this.isLeaf = false;
    this.right = right;
  }
  
  public Collection<Tuple> getNext() throws Throwable{
    if(this.isLeaf){
      return this.operator.process(null);
    }
    return this.operator.process(this.left.getNext());
  }
}
