package edu.buffalo.www.cse4562.operator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.buffalo.www.cse4562.model.Tuple;
import edu.buffalo.www.cse4562.model.Tuple.ColumnCell;
import edu.buffalo.www.cse4562.util.Validate;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * This {@link Operator} does the projection of selected columns.
 * 
 * @author varunjai
 *
 */
public class ProjectionOperator implements Operator {

  private final Collection<SelectItem> selectItems;

  public ProjectionOperator(Collection<SelectItem> selectItems) {
    Validate.notEmpty(selectItems);
    this.selectItems = selectItems;
  }

  public Collection<SelectItem> getSelectItems() {
    return selectItems;
  }

  @Override
  public Collection<Tuple> process(Collection<Tuple> tuples) {

    List<Tuple> projectOutput = new ArrayList<>();
    for (Tuple tuple : tuples) {

      if (tuple.isEmpty()) {
        continue;
      }

      List<ColumnCell> columnCells = new ArrayList<>();
      for (ColumnCell columnCell : tuple.getColumnCells()) {
        for (SelectItem selectItem : selectItems) {
          if (selectItem.toString().equals(columnCell.getColumnName())) {
            columnCells.add(columnCell);
          } // if
        } // for
      } // for

      projectOutput.add(new Tuple(columnCells));
    } // for

    return projectOutput;
  }

}
