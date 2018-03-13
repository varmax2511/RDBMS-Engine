package edu.buffalo.www.cse4562.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.buffalo.www.cse4562.operator.Operator;

/**
 * <pre>
 * Binary Tree Node where each Node represents the {@link Operator} it holds,
 * the class of the operator and its children.
 * <p/>
 * Each node is linked to an operator. An operator is oblivious of the Tree created
 * for a query and is only responsible for processing the Collection of {@link Tuple}
 * sent it for processing.
 *
 * Each node first calls the {@link #getNext()} method of each of its child nodes
 * and gets a Collection of tuples. It creates a Collection of collection of
 * tuples and invokes its operator's {@link Operator#process(Collection)} method
 * and passes the argument as its child's {@link #getNext()}.
 *
 *
 *    Node(Projection)
 *         |
 *         |
 *      Node(Select)
 *         |
 *         |
 *      Node(Scanner)
 *
 *
 * A node contains a list of children.
 *
 *  Q. Why create two child, why not create an array of children for each node?
 *     An array gives more flexibility than a fixed left and right child to create
 *     an n-ary tree.
 *     It avoids checking left and then right child, rather just iterate array,
 *     makes code simpler. So, its better to implement children as an array
 *
 *
 * A node is extended by an implementing Operator
 * 
 *
 *
 * </pre>
 */
public abstract class Node {

  /*
   * private Node left; private Node right;
   */private boolean isLeaf = true;
  private List<Node> children = new ArrayList<>();

  public void addChild(Node child) {
    this.isLeaf = false;
    children.add(child);
  }

  public List<Node> getChildren() {
    return children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  /**
   * Each operator accepts a Collection of collection of tuples. This is to
   * cater operators which can be binary or more, like Croos-Product which
   * accept two collection of tuples, one from each table.
   * 
   * @param tupleCollection
   * @return
   * @throws Throwable
   */
  public abstract Collection<Tuple> process(
      Collection<Collection<Tuple>> tupleCollection) throws Throwable;

  public void open() throws Throwable {

  }

  public void close() throws Throwable {

  }

  /**
   * Works similar to the hasNext for an iterator. It checks all the way down to
   * the leaf whether it has any more records.
   *
   * @return
   * @throws IOException
   */
  public boolean hasNext() throws IOException {
    return this.children.get(0).hasNext();
  }

  /**
   * This method is invoked to start execution for this node.
   *
   * @return
   * @throws Throwable
   */
  public Collection<Tuple> getNext() throws Throwable {
    // if leaf node
    if (this.isLeaf) {
      return process(null);
    }

    final Collection<Collection<Tuple>> tuples = new ArrayList<>();
    final Iterator<Node> iterator = this.children.iterator();

    // process each child node for this node
    // add all collection of tuples returned by each node in a collection
    // of collection
    while (iterator.hasNext()) {
      tuples.add(iterator.next().getNext());
    }

    return process(tuples);
  }
}
